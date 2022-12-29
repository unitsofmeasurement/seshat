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

import java.util.Locale;
import java.util.MissingResourceException;


/**
 * Locale-dependent resources for error messages.
 *
 * <h2>Argument order convention</h2>
 * This resource bundle applies the same convention than JUnit: for every {@code format(…)} method,
 * the first arguments provide information about the context in which the error occurred (e.g. the
 * name of a method argument or the range of valid values), while the erroneous values that caused
 * the error are last. Note that being the last programmatic parameter does not means that the value
 * will appears last in the formatted text, since every localized message can reorder the parameters
 * as they want.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.0
 */
public class Errors extends IndexedResourceBundle {
    /**
     * Resource keys. This class is used when compiling sources, but no dependencies to
     * {@code Keys} should appear in any resulting class files. Since the Java compiler
     * inlines final integer values, using long identifiers will not bloat the constant
     * pools of compiled classes.
     *
     * @author  Martin Desruisseaux (IRD, Geomatys)
     * @version 1.0
     */
    public static final class Keys extends KeyConstants {
        /**
         * The unique instance of key constants handler.
         */
        static final Keys INSTANCE = new Keys();

        /**
         * For {@link #INSTANCE} creation only.
         */
        private Keys() {
        }

        /**
         * Can not convert value “{0}” to type ‘{1}’.
         */
        public static final short CanNotConvertValue_2 = 18;

        /**
         * Cannot parse “{0}”.
         */
        public static final short CanNotParse_1 = 20;

        /**
         * Element “{0}” is already present.
         */
        public static final short ElementAlreadyPresent_1 = 1;

        /**
         * Argument ‘{0}’ shall not be empty.
         */
        public static final short EmptyArgument_1 = 2;

        /**
         * Argument ‘{0}’ can not take the “{1}” value.
         */
        public static final short IllegalArgumentValue_2 = 3;

        /**
         * The “{1}” character can not be used for “{0}”.
         */
        public static final short IllegalCharacter_2 = 4;

        /**
         * Sexagesimal angle {0,number} is illegal because the {1,choice,0#minutes|1#seconds} field can
         * not take the {2,number} value.
         */
        public static final short IllegalSexagesimalField_3 = 19;

        /**
         * The “{0}” unit of measurement has dimension of ‘{1}’ ({2}). It is incompatible with
         * dimension of ‘{3}’ ({4}).
         */
        public static final short IncompatibleUnitDimension_5 = 5;

        /**
         * Units “{0}” and “{1}” are incompatible.
         */
        public static final short IncompatibleUnits_2 = 6;

        /**
         * “{0}” is not an angular unit.
         */
        public static final short NonAngularUnit_1 = 7;

        /**
         * Missing a ‘{1}’ parenthesis in “{0}”.
         */
        public static final short NonEquilibratedParenthesis_2 = 8;

        /**
         * “{0}” is not a linear unit.
         */
        public static final short NonLinearUnit_1 = 9;

        /**
         * The scale of measurement for “{0}” unit is not a ratio scale.
         */
        public static final short NonRatioUnit_1 = 10;

        /**
         * “{0}” is not a scale unit.
         */
        public static final short NonScaleUnit_1 = 11;

        /**
         * “{0}” is not a fundamental or derived unit.
         */
        public static final short NonSystemUnit_1 = 12;

        /**
         * “{0}” is not a time unit.
         */
        public static final short NonTemporalUnit_1 = 13;

        /**
         * {0} is not an integer value.
         */
        public static final short NotAnInteger_1 = 14;

        /**
         * The “{1}” characters after “{0}” were unexpected.
         */
        public static final short UnexpectedCharactersAfter_2 = 15;

        /**
         * Unit “{0}” is not recognized.
         */
        public static final short UnknownUnit_1 = 16;

        /**
         * Can not handle this instance of ‘{0}’ because arbitrary implementations are not yet
         * supported.
         */
        public static final short UnsupportedImplementation_1 = 17;
    }

    /**
     * Constructs a new resource bundle loading data from a UTF file of the same name.
     * This constructor needs to be public for instantiation by {@link java.util.ResourceBundle}.
     */
    public Errors() {
    }

    /**
     * Returns the handle for the {@code Keys} constants.
     *
     * @return a handler for the constants declared in the inner {@code Keys} class.
     */
    @Override
    KeyConstants getKeyConstants() {
        return Keys.INSTANCE;
    }

    /**
     * Returns resources in the given locale.
     *
     * @param  locale  the locale, or {@code null} for the default locale.
     * @return resources in the given locale.
     * @throws MissingResourceException if resources can not be found.
     */
    public static Errors getResources(final Locale locale) throws MissingResourceException {
        return getBundle(Errors.class, locale);
    }

    /**
     * Gets a string for the given key are replace all occurrence of "{0}"
     * with values of {@code arg0}.
     *
     * @param  key   the key for the desired string.
     * @param  arg0  value to substitute to "{0}".
     * @return the formatted string for the given key.
     * @throws MissingResourceException if no object for the given key can be found.
     */
    public static String format(final short  key,
                                final Object arg0) throws MissingResourceException
    {
        return getResources((Locale) null).getString(key, arg0);
    }

    /**
     * Gets a string for the given key are replace all occurrence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}.
     *
     * @param  key   the key for the desired string.
     * @param  arg0  value to substitute to "{0}".
     * @param  arg1  value to substitute to "{1}".
     * @return the formatted string for the given key.
     * @throws MissingResourceException if no object for the given key can be found.
     */
    public static String format(final short  key,
                                final Object arg0,
                                final Object arg1) throws MissingResourceException
    {
        return getResources((Locale) null).getString(key, arg0, arg1);
    }


}
