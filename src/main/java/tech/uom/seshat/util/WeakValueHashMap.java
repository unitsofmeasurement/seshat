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
package tech.uom.seshat.util;

import java.util.Map;
import java.util.Set;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Arrays;
import java.lang.ref.WeakReference;

import static tech.uom.seshat.util.WeakEntry.*;


/**
 * A hashtable-based map implementation that uses {@linkplain WeakReference weak references},
 * leaving memory when an entry is not used anymore. An entry in a {@code WeakValueHashMap}
 * will automatically be removed when its value is no longer in ordinary use. This class is
 * similar to the standard {@link java.util.WeakHashMap} class, except that weak references
 * apply to values rather than keys.
 *
 * <p>Note that this class is <strong>not</strong> a cache, because the entries are discarded
 * as soon as the garbage collector determines that they are no longer in use.</p>
 *
 * <p>This class is convenient for avoiding the creation of duplicated elements, as in the
 * example below:</p>
 *
 * <pre>{@code
 *     K key = ...
 *     V value;
 *     synchronized (map) {
 *         value = map.get(key);
 *         if (value == null) {
 *             value = ...; // Create the value here.
 *             map.put(key, value);
 *         }
 *     }
 * }</pre>
 *
 * In the above example, the calculation of a new value needs to be fast because it is performed inside a synchronized
 * statement blocking all other access to the map. This is okay if that particular {@code WeakValueHashMap} instance
 * is not expected to be used in a highly concurrent environment.
 *
 * <h2>Thread safety</h2>
 * The same {@code WeakValueHashMap} instance can be safely used by many threads without synchronization on the part
 * of the caller. But if a sequence of two or more method calls need to appear atomic from other threads perspective,
 * then the caller can synchronize on {@code this}.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.2
 *
 * @param <K>  the class of key elements.
 * @param <V>  the class of value elements.
 *
 * @see java.util.WeakHashMap
 * @see WeakHashSet
 *
 * @since 1.0
 */
public class WeakValueHashMap<K,V> extends AbstractMap<K,V> {
    /*
     * NOTE: Apache SIS defines constants for handling different comparison modes.
     *       Those modes have been removed in this port because not needed for Seshat.
     */

    /**
     * An entry in the {@link WeakValueHashMap}. This is a weak reference
     * to a value together with a strong reference to a key.
     */
    private final class Entry extends WeakEntry<V> implements Map.Entry<K,V> {
        /**
         * The key.
         */
        final K key;

        /**
         * Constructs a new weak reference.
         */
        Entry(final K key, final V value, final Entry next, final int hash) {
            super(value, next, hash);
            this.key   = key;
            this.next  = next;
        }

        /**
         * Returns the key corresponding to this entry.
         */
        @Override
        public K getKey() {
            return key;
        }

        /**
         * Returns the value corresponding to this entry.
         */
        @Override
        public V getValue() {
            return get();
        }

        /**
         * Replaces the value corresponding in this entry with the specified value.
         * This method can be used only for setting the value to {@code null}.
         */
        @Override
        public V setValue(final V value) {
            if (value != null) {
                throw new UnsupportedOperationException();
            }
            final V old = get();
            dispose();
            return old;
        }

        /**
         * Invoked by {@link ReferenceQueueConsumer}
         * for removing the reference from the enclosing collection.
         */
        @Override
        public void dispose() {
            super.clear();
            removeEntry(this);
        }

        /**
         * Compares the specified object with this entry for equality.
         */
        @Override
        public boolean equals(final Object other) {
            if (other instanceof Map.Entry<?,?>) {
                final Map.Entry<?,?> that = (Map.Entry<?,?>) other;
                return key.equals(that.getKey()) && Objects.equals(get(), that.getValue());
            }
            return false;
        }

        /**
         * Returns the hash code value for this map entry. <strong>This hash code
         * is not stable</strong>, since it will change after GC collect the value.
         */
        @Override
        public int hashCode() {
            int code = key.hashCode();
            final V val = get();
            if (val != null) {
                code ^= val.hashCode();
            }
            return code;
        }
    }

    /**
     * Table of weak references.
     */
    private Entry[] table;

    /**
     * Number of non-null elements in {@link #table}. This is used for determining
     * when {@link WeakEntry#rehash(WeakEntry[], int)} needs to be invoked.
     */
    private int count;

    /**
     * The type of the keys in this map.
     */
    private final Class<K> keyType;

    /**
     * The set of entries, created only when first needed.
     */
    private transient Set<Map.Entry<K,V>> entrySet;

    /**
     * The last time when {@link #table} was not in need for rehash. When the garbage collector
     * collected a lot of elements, we will wait a few seconds before rehashing {@link #table}
     * in case lot of news entries are going to be added. Without this field, we noticed many
     * "reduce", "expand", "reduce", "expand", <i>etc.</i> cycles.
     */
    private transient long lastTimeNormalCapacity;

    /**
     * Creates a new {@code WeakValueHashMap}.
     *
     * @param  keyType  the type of keys in the map.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})    // Generic array creation.
    public WeakValueHashMap(final Class<K> keyType) {
        this.keyType = keyType;
        lastTimeNormalCapacity = System.nanoTime();
        table = new WeakValueHashMap.Entry[MIN_CAPACITY];
    }

    /**
     * Invoked by {@link Entry} when an element has been collected by the garbage
     * collector. This method removes the weak reference from the {@link #table}.
     *
     * @param  toRemove  the entry to remove from this map.
     */
    @SuppressWarnings("unchecked")
    private synchronized void removeEntry(final Entry toRemove) {
        assert isValid();
        final int capacity = table.length;
        if (toRemove.removeFrom(table, toRemove.hash % capacity)) {
            count--;
            assert isValid();
            if (count < lowerCapacityThreshold(capacity)) {
                final long currentTime = System.nanoTime();
                if (currentTime - lastTimeNormalCapacity > REHASH_DELAY) {
                    table = (Entry[]) WeakEntry.rehash(table, count);
                    lastTimeNormalCapacity = currentTime;
                    assert isValid();
                }
            }
        }
    }

    /**
     * Checks if this {@code WeakValueHashMap} is valid. This method counts the number of elements
     * and compares it to {@link #count}. This method is invoked in assertions only.
     *
     * @return whether {@link #count} matches the expected value.
     */
    final boolean isValid() {
        if (!Thread.holdsLock(this)) {
            throw new AssertionError();
        }
        if (count > upperCapacityThreshold(table.length)) {
            throw new AssertionError(count);
        }
        return count(table) == count;
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of entries in this map.
     */
    @Override
    public synchronized int size() {
        assert isValid();
        return count;
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key.
     * Null keys are considered never present.
     *
     * @param  key  key whose presence in this map is to be tested.
     * @return {@code true} if this map contains a mapping for the specified key.
     */
    @Override
    public boolean containsKey(final Object key) {
        return get(key) != null;
    }

    /**
     * Returns {@code true} if this map maps one or more keys to this value.
     * Null values are considered never present.
     *
     * @param  value  value whose presence in this map is to be tested.
     * @return {@code true} if this map maps one or more keys to this value.
     */
    @Override
    public synchronized boolean containsValue(final Object value) {
        return super.containsValue(value);
    }

    /**
     * Returns the value to which this map maps the specified key.
     * Returns {@code null} if the map contains no mapping for this key.
     * Null keys are considered never present.
     *
     * @param  key  key whose associated value is to be returned.
     * @return the value to which this map maps the specified key.
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized V get(final Object key) {
        assert isValid();
        if (key != null) {
            final Entry[] table = this.table;
            final int index = (key.hashCode() & HASH_MASK) % table.length;
            for (Entry e = table[index]; e != null; e = (Entry) e.next) {
                if (key.equals(e.key)) {
                    return e.get();
                }
            }
        }
        return null;
    }

    /**
     * Wildcard for {@link #intern(Object, Object, Object)} condition meaning whether a key shall be associated
     * to a value or not. Note that {@link #equals(Object)} and {@link #hashCode()} methods are inconsistent in
     * this class; the {@code hashCode()} method should never be invoked.
     */
    @SuppressWarnings("overrides")
    private static final class Wildcard {
        static final Wildcard ANY_VALUE = new Wildcard(true);
        static final Wildcard NO_VALUE  = new Wildcard(false);

        /** Whether the key shall be associated to a value. */
        private final boolean present;

        /** Creates the {@link #ANY_VALUE} or {@link #NO_VALUE} constant. */
        private Wildcard(final boolean present) {
            this.present = present;
        }

        /** Tests for the {@link #ANY_VALUE} or {@link #NO_VALUE} condition. */
        @Override public boolean equals(Object oldValue) {
            return (oldValue != null) == present;
        }
    }

    /**
     * Implementation of {@link #put(Object, Object)}, {@link #putIfAbsent(Object, Object)}, {@link #remove(Object)},
     * {@link #replace(Object, Object)} and {@link #replace(Object, Object, Object)} operations.
     *
     * @param  key        key with which the specified value is to be associated.
     * @param  value      value to be associated with the specified key, or {@code null} for removing the entry.
     * @param  condition  previous value that entry must have for doing the action, or {@code null} if no restriction.
     * @return the previous value associated with specified key, or {@code null} if there was no mapping for the key.
     */
    @SuppressWarnings("unchecked")
    private synchronized V intern(final Object key, final V value, final Object condition) {
        assert isValid();
        /*
         * If `value` is already contained in this WeakValueHashMap, we need to clear it.
         */
        V oldValue = null;
        Entry[] table = this.table;
        final int hash = key.hashCode() & HASH_MASK;
        int index = hash % table.length;
        for (Entry e = table[index]; e != null; e = (Entry) e.next) {
            if (key.equals(e.key)) {
                oldValue = e.get();
                if (condition != null && !condition.equals(oldValue)) {
                    return oldValue;
                }
                e.dispose();
                table = this.table;             // May have changed.
                index = hash % table.length;
            }
        }
        /*
         * If a value has been specified, add it after above removal of previous value except if
         * this method is invoked from `replace(key, old)` (condition = `Wildcard.ANY_VALUE`) or
         * `replace(key, old, new)` (condition = valid object) and no previous value was mapped.
         */
        if (value != null && (condition == null || condition == Wildcard.NO_VALUE || oldValue != null)) {
            if (++count >= lowerCapacityThreshold(table.length)) {
                if (count > upperCapacityThreshold(table.length)) {
                    this.table = table = (Entry[]) rehash(table, count);
                    index = hash % table.length;
                }
                lastTimeNormalCapacity = System.nanoTime();
            }
            table[index] = new Entry(keyType.cast(key), value, table[index], hash);
        }
        assert isValid();
        return oldValue;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * The value is associated using a {@link WeakReference}.
     *
     * @param  key    key with which the specified value is to be associated.
     * @param  value  value to be associated with the specified key.
     * @return the previous value associated with specified key, or {@code null} if there was no mapping for the key.
     * @throws NullPointerException if the key or the value is {@code null}.
     */
    @Override
    public V put(final K key, final V value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        return intern(key, value, null);
    }

    /**
     * Associates the specified value with the specified key in this map if no value were previously associated.
     * If an other value is already associated to the given key, then the map is left unchanged and the current
     * value is returned. Otherwise the specified value is associated to the key using a {@link WeakReference}
     * and {@code null} is returned.
     *
     * @param  key    key with which the specified value is to be associated.
     * @param  value  value to be associated with the specified key.
     * @return the current value associated with specified key, or {@code null} if there was no mapping for the key.
     * @throws NullPointerException if the key or the value is {@code null}.
     */
    @Override
    public V putIfAbsent(final K key, final V value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        return intern(key, value, Wildcard.NO_VALUE);
    }

    /**
     * Replaces the entry for the specified key only if it is currently mapped to some value.
     *
     * @param  key    key with which the specified value is to be associated.
     * @param  value  value to be associated with the specified key.
     * @return the previous value associated with specified key, or {@code null} if there was no mapping for the key.
     * @throws NullPointerException if the value is {@code null}.
     */
    @Override
    public V replace(final K key, final V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (key == null) return null;
        return intern(key, value, Wildcard.ANY_VALUE);
    }

    /**
     * Replaces the entry for the specified key only if currently mapped to the specified value.
     *
     * @param  key       key with which the specified value is to be associated.
     * @param  oldValue  value expected to be associated with the specified key.
     * @param  newValue  value to be associated with the specified key.
     * @return {@code true} if the value was replaced.
     * @throws NullPointerException if the new value is {@code null}.
     */
    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        if (newValue == null) {
            throw new NullPointerException();
        }
        return replaceOrRemove(key, oldValue, newValue);
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param  key  key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or {@code null} if there was no entry for the key.
     */
    @Override
    public V remove(final Object key) {
        if (key == null) return null;
        return intern(key, null, null);
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to the specified value.
     *
     * @param  key    key whose mapping is to be removed from the map.
     * @param  value  value expected to be associated with the specified key.
     * @return {@code true} if the value was removed.
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        return replaceOrRemove(key, value, null);
    }

    /**
     * Implementation of {@link #replace(Object, Object, Object)} and {@link #remove(Object, Object)}.
     * The replace action has a non-null {@code newValue} and the remove action has a null new value.
     */
    private boolean replaceOrRemove(final Object key, final Object oldValue, final V newValue) {
        if (key == null || oldValue == null) {
            return false;
        }
        @SuppressWarnings("overrides")
        final class Observer {
            boolean equals;

            @Override public boolean equals(final Object other) {
                return equals = oldValue.equals(other);
            }
        }
        final Observer observer = new Observer();
        return intern(key, newValue, observer) != null && observer.equals;
    }

    /**
     * Removes all of the elements from this map.
     */
    @Override
    public synchronized void clear() {
        Arrays.fill(table, null);
        count = 0;
    }

    /**
     * Returns a set view of the mappings contained in this map.
     * Each element in this set is a {@link java.util.Map.Entry}.
     *
     * @return a set view of the mappings contained in this map.
     */
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public synchronized Set<Map.Entry<K,V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    /**
     * The set of entries.
     *
     * @author  Martin Desruisseaux (Geomatys)
     */
    private final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        /**
         * Returns the number of entries in the map.
         */
        @Override
        public int size() {
            return WeakValueHashMap.this.size();
        }

        /**
         * Returns a view of this set as an array. Note that this array contains strong references.
         * Consequently, no object reclamation will occur as long as a reference to this array is hold.
         */
        @Override
        @SuppressWarnings("unchecked")
        public Map.Entry<K,V>[] toArray() {
            synchronized (WeakValueHashMap.this) {
                assert isValid();
                @SuppressWarnings({"unchecked","rawtypes"})
                Map.Entry<K,V>[] elements = new Map.Entry[size()];
                int index = 0;
                final Entry[] table = WeakValueHashMap.this.table;
                for (Entry el : table) {
                    while (el != null) {
                        final Map.Entry<K,V> entry = new SimpleEntry<>(el);
                        if (entry.getValue() != null) {
                            elements[index++] = entry;
                        }
                        el= (Entry) el.next;
                    }
                }
                if (index != elements.length) {
                    elements = Arrays.copyOf(elements, index);
                }
                return elements;
            }
        }

        /**
         * Returns an iterator over the elements contained in this collection. No element from
         * this set will be garbage collected as long as a reference to the iterator is hold.
         */
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return Arrays.asList(toArray()).iterator();
        }
    }
}
