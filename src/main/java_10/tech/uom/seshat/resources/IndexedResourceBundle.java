/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership. You may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.uom.seshat.resources;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;


/**
 * {@link ResourceBundle} implementation accepting integers instead of strings for resource keys.
 * Using integers allow implementations to avoid adding large string constants into their
 * {@code .class} files and runtime images. Developers still have meaningful labels in their
 * code (e.g. {@code MismatchedDimension}) through a set of constants defined in {@code Keys}
 * inner classes, with the side-effect of compile-time safety. Because integer constants are
 * inlined right into class files at compile time, the declarative classes is not loaded at run time.
 *
 * <p>Localized resources are fetched by calls to {@link #getString(short)}.
 * Arguments can optionally be provided by calls to {@link #getString(short, Object) getString(short, Object, ...)}.
 * If arguments are present, then the string will be formatted using {@link MessageFormat},
 * completed by some special cases handled by this class. Roughly speaking:</p>
 *
 * <ul>
 *   <li>{@link Number} and {@link java.util.Date} instances
 *       are localized using the current {@code ResourceBundle} locale.</li>
 *   <li>{@link Class} and {@link Throwable} instances are summarized.</li>
 * </ul>
 *
 * <h2>Thread safety</h2>
 * The same {@code IndexedResourceBundle} instance can be safely used by many threads without synchronization
 * on the part of the caller. Subclasses should make sure that any overridden methods remain safe to call from
 * multiple threads.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.0
 * @since   1.0
 */
public abstract class IndexedResourceBundle extends ResourceBundle {
    /**
     * The filename extension of resource files.
     */
    private static final String EXTENSION = ".utf";

    /**
     * First valid key index.
     * We start at 1 rather than 0 in order to keep value 0 available for meaning "no localized message".
     */
    static final int FIRST = 1;

    /**
     * The array of resources. Keys are an array index plus {@value #FIRST}. For example the value for key "14" is
     * {@code values[13]}. This array will be loaded only when first needed. We should not load it at construction
     * time, because some {@code ResourceBundle} objects will never ask for values.  This is particularly the case
     * for parent resources of {@code Resources_fr_CA}, {@code Resources_en}, {@code Resources_de}, etc. which will
     * only be used if a key has not been found in the child resources.
     *
     * @see #ensureLoaded(String)
     */
    @SuppressWarnings("VolatileArrayField")     // Okay because we set this field only after the array has been fully constructed.
    private volatile String[] values;

    /**
     * The object to use for formatting messages. This object
     * will be constructed only when first needed.
     */
    private transient MessageFormat format;

    /**
     * The key of the last resource requested. If the same resource is requested multiple times,
     * knowing its key allows us to avoid invoking the costly {@link MessageFormat#applyPattern}
     * method.
     */
    private transient short lastKey;

    /**
     * Constructs a new resource bundle loading data from a file inferred from the class name.
     */
    IndexedResourceBundle() {
    }

    /**
     * Returns a resource bundle of the specified class.
     *
     * @param  <T>      the resource bundle class.
     * @param  base     the resource bundle class.
     * @param  locale   the locale, or {@code null} for the default locale.
     * @return resources in the given locale.
     * @throws MissingResourceException if resources can't be found.
     *
     * @see Errors#getResources(Locale)
     */
    protected static <T extends IndexedResourceBundle> T getBundle(Class<T> base, Locale locale)
            throws MissingResourceException
    {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        // No caching; we rely on the one implemented in ResourceBundle.
        return base.cast(getBundle(base.getName(), locale, base.getModule()));
    }

    /**
     * Returns a handler for the constants declared in the inner {@code Keys} class.
     *
     * @return a handler for the constants declared in the inner {@code Keys} class.
     */
    abstract KeyConstants getKeyConstants();

    /**
     * Returns an enumeration of the keys.
     *
     * @return all keys in this resource bundle.
     */
    @Override
    public final Enumeration<String> getKeys() {
        return new KeyEnum(getKeyConstants().getKeyNames());
    }

    /**
     * The keys as an enumeration. This enumeration needs to skip null values, which
     * may occur if the resource bundle is incomplete for that particular locale.
     */
    private static final class KeyEnum implements Enumeration<String> {
        /** The keys to return.          */ private final String[] keys;
        /** Index of next key to return. */ private int next;

        /** Creates a new enum for the given array of keys. */
        KeyEnum(final String[] keys) {
            this.keys = keys;
        }

        /** Returns {@code true} if there is at least one more non-null key. */
        @Override public boolean hasMoreElements() {
            while (next < keys.length) {
                if (keys[next] != null) {
                    return true;
                }
                next++;
            }
            return false;
        }

        /** Returns the next key. */
        @Override public String nextElement() {
            while (next < keys.length) {
                final String key = keys[next++];
                if (key != null) {
                    return key;
                }
            }
            throw new NoSuchElementException();
        }
    }

    /**
     * Ensures that resource values are loaded. If they are not, loads them immediately.
     *
     * @param  key  key for the requested resource, or {@code null} if all resources
     *         are requested. This key is used mostly for constructing messages.
     * @return the resources.
     * @throws MissingResourceException if this method failed to load resources.
     */
    private String[] ensureLoaded(final String key) throws MissingResourceException {
        String[] values = this.values;
        if (values == null) synchronized (this) {
            values = this.values;
            if (values == null) {
                /*
                 * Loads resources from the UTF file.
                 */
                final Class<?> c = getClass();
                final InputStream in = c.getResourceAsStream(c.getSimpleName() + EXTENSION);
                if (in == null) {
                    /*
                     * If there is no explicit resources for this instance, inherit the resources
                     * from the parent. Note that this IndexedResourceBundle instance may still
                     * differ from its parent in the way dates and numbers are formatted.
                     *
                     * If we get a NullPointerException or ClassCastException here,
                     * it would be a bug in the way we create the chain of parents.
                     */
                    values = ((IndexedResourceBundle) parent).ensureLoaded(key);
                } else {
                    try (DataInputStream input = new DataInputStream(in)) {
                        values = new String[input.readInt()];
                        for (int i=0; i<values.length; i++) {
                            values[i] = input.readUTF();
                            if (values[i].isEmpty()) {
                                values[i] = null;
                            }
                        }
                    } catch (IOException exception) {
                        throw (MissingResourceException) new MissingResourceException(
                                exception.getLocalizedMessage(), c.getCanonicalName(), key).initCause(exception);
                    }
                }
                this.values = values;
            }
        }
        return values;
    }

    /**
     * Gets an object for the given key from this resource bundle.
     * Returns null if this resource bundle does not contain an
     * object for the given key.
     *
     * @param  key  the key for the desired object
     * @throws NullPointerException if {@code key} is {@code null}
     * @return the object for the given key, or null
     */
    @Override
    protected final Object handleGetObject(final String key) {
        /*
         * Note: Synchronization is performed by 'ensureLoaded'
         */
        final String[] values = ensureLoaded(key);
        int keyID;
        try {
            keyID = Short.parseShort(key);
        } catch (NumberFormatException exception) {
            /*
             * Maybe the full key name has been specified instead. We do that for localized
             * LogRecords, for easier debugging if the message has not been properly formatted.
             */
            try {
                keyID = getKeyConstants().getKeyValue(key);
            } catch (ReflectiveOperationException e) {
                e.addSuppressed(exception);
                System.getLogger("tech.uom.seshat").log(System.Logger.Level.WARNING, e);
                return null;                // This is okay as of 'handleGetObject' contract.
            }
        }
        keyID -= FIRST;
        return (keyID >= 0 && keyID < values.length) ? values[keyID] : null;
    }

    /**
     * Returns {@code arguments} as an array, and convert some types that are not recognized
     * by {@link MessageFormat}. If {@code arguments} is already an array, then that array or
     * a copy of that array will be returned. If {@code arguments} is not an array, it will be
     * placed in an array of length 1.
     *
     * @param  arguments  the object to check.
     * @return {@code arguments} as an array, eventually with some elements replaced.
     */
    final Object[] toArray(final Object arguments) {
        Object[] array;
        if (arguments instanceof Object[]) {
            array = (Object[]) arguments;
        } else {
            array = new Object[] {arguments};
        }
        return array;
    }

    /**
     * Gets a string for the given key from this resource bundle or one of its parents.
     *
     * @param  key  the key for the desired string.
     * @return the string for the given key.
     * @throws MissingResourceException if no object for the given key can be found.
     */
    public final String getString(final short key) throws MissingResourceException {
        return getString(String.valueOf(key));
    }

    /**
     * Gets a string for the given key and formats it with the specified argument. The message is
     * formatted using {@link MessageFormat}. Calling this method is approximately equivalent to
     * calling:
     *
     * <pre>{@code
     *     String pattern = getString(key);
     *     Format f = new MessageFormat(pattern);
     *     return f.format(arg0);
     * }</pre>
     *
     * If {@code arg0} is not already an array, it will be placed into an array of length 1. Using
     * {@link MessageFormat}, all occurrences of "{0}", "{1}", "{2}" in the resource string will be
     * replaced by {@code arg0[0]}, {@code arg0[1]}, {@code arg0[2]}, etc.
     *
     * @param  key   the key for the desired string.
     * @param  arg0  a single object or an array of objects to be formatted and substituted.
     * @return the string for the given key.
     * @throws MissingResourceException if no object for the given key can be found.
     *
     * @see #getString(String)
     * @see #getString(short,Object,Object)
     * @see MessageFormat
     */
    public final String getString(final short key, final Object arg0) throws MissingResourceException {
        final String pattern = getString(key);
        final Object[] arguments = toArray(arg0);
        synchronized (this) {
            if (format == null) {
                /*
                 * Constructs a new MessageFormat for formatting the arguments.
                 */
                format  = new MessageFormat(pattern, getLocale());
                lastKey = key;
            } else if (key != lastKey) {
                /*
                 * Method MessageFormat.applyPattern(…) is costly! We will avoid
                 * calling it again if the format already has the right pattern.
                 */
                format.applyPattern(pattern);
                lastKey = key;
            }
            try {
                return format.format(arguments);
            } catch (RuntimeException e) {
                /*
                 * Safety against badly implemented toString() method
                 * in libraries that we do not control.
                 */
                return "[Unformattable message: " + e + ']';
            }
        }
    }

    /**
     * Gets a string for the given key and replaces all occurrences of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}, etc.
     *
     * @param  key   the key for the desired string.
     * @param  arg0  value to substitute for "{0}".
     * @param  arg1  value to substitute for "{1}".
     * @return the formatted string for the given key.
     * @throws MissingResourceException if no object for the given key can be found.
     */
    public final String getString(final short  key,
                                  final Object arg0,
                                  final Object arg1) throws MissingResourceException
    {
        return getString(key, new Object[] {arg0, arg1});
    }

    /**
     * Returns a string representation of this object.
     * This method is for debugging purposes only.
     *
     * @return a string representation of this resources bundle.
     */
    @Override
    public synchronized String toString() {
        return getClass().getSimpleName() + '[' + getLocale() + ']';
    }
}
