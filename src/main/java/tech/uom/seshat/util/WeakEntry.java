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

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import tech.uom.seshat.math.MathFunctions;


/**
 * A weak reference to an element in a {@link WeakHashSet} or {@link WeakValueHashMap}.
 * This is an element in a linked list. When the reference is disposed, it is removed
 * from the enclosing collection.
 *
 * @author  Martin Desruisseaux (MPO, IRD, Geomatys)
 * @version 1.0
 *
 * @param <E>  the type of elements in the collection.
 *
 * @since 1.0
 */
abstract class WeakEntry<E> extends WeakReference<E> {
    /**
     * Minimal capacity for the internal table of entries.
     * Must be a prime number.
     */
    static final int MIN_CAPACITY = 7;

    /**
     * The mask to apply on hash code values for ensuring positive values.
     */
    static final int HASH_MASK = Integer.MAX_VALUE;

    /**
     * Number of nanoseconds to wait before to rehash the table for reducing its size.
     * When the garbage collector collects a lot of elements, we will wait at least this amount of time
     * before to rehash the tables, in case lot of news elements are going to be added. We noticed that
     * in the absence of delay, there is a lot of "reduce", "expand", "reduce", "expand", <i>etc.</i> cycles.
     */
    static final long REHASH_DELAY = 4000_000_000L;                     // 4 seconds.

    /**
     * The next entry, or {@code null} if there is none.
     * This is used when more than one entry has the same hash code value.
     */
    WeakEntry<E> next;

    /**
     * The absolute value of the hash value of the referenced object.
     */
    final int hash;

    /**
     * Constructs a new weak reference.
     */
    WeakEntry(final E obj, final WeakEntry<E> next, final int hash) {
        super(obj, ReferenceQueueConsumer.QUEUE);
        this.next = next;
        this.hash = hash;
    }

    /**
     * Allows any resources held by this object to be released. The result of calling any other
     * method (other than {@code finalize()}) subsequent to a call to this method is undefined.
     */
    public abstract void dispose();

    /**
     * Counts the number of entries in the given table.
     * This method does not verify if the referenced object has been garbage collected.
     *
     * @param  <E>    the type of elements in the collection.
     * @param  table  the table in which to count the number of entries.
     * @return number of entries in the given table.
     */
    static <E> int count(final WeakEntry<E>[] table) {
        int n = 0;
        for (WeakEntry<E> e : table) {
            while (e != null) {
                n++;
                e = e.next;
            }
        }
        return n;
    }

    /**
     * Removes this entry from the given table of entries.
     *
     * @param  table     the table from which to remove this entry.
     * @param  removeAt  the index of this entry in the given table.
     * @return {@code true} if this entry has been found and removed, or {@code false} otherwise.
     */
    final boolean removeFrom(final WeakEntry<E>[] table, final int removeAt) {
        WeakEntry<E> prev = null;
        WeakEntry<E> e = table[removeAt];
        while (e != null) {
            if (e == this) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    table[removeAt] = e.next;
                }
                // We can not continue the loop pass that point, since `e` is no longer valid.
                return true;
            }
            prev = e;
            e = e.next;
        }
        return false;
    }

    /**
     * Rehashes the given table.
     *
     * @param  oldTable  the table to rehash.
     * @param  count     number of elements in the table (including chained elements).
     * @return the new table array, or {@code oldTable} if no rehash were needed.
     */
    static <E> WeakEntry<E>[] rehash(final WeakEntry<E>[] oldTable, final int count) {
        /*
         * Compute the capacity as twice the expected number of elements, then take
         * (if possible) the first prime number equals or greater to that value.
         * This is based on classical books saying that prime values reduce the
         * risk of key collisions.
         */
        int capacity = Math.max(count*2, MIN_CAPACITY);
        if (capacity < MathFunctions.HIGHEST_SUPPORTED_PRIME_NUMBER) {
            capacity = MathFunctions.nextPrimeNumber(capacity);
        }
        if (capacity == oldTable.length) {
            return oldTable;
        }
        /*
         * Rehash the table.
         */
        final Class<?> entryType = oldTable.getClass().getComponentType();
        @SuppressWarnings("unchecked")
        final WeakEntry<E>[] table = (WeakEntry<E>[]) Array.newInstance(entryType, capacity);
        for (WeakEntry<E> next : oldTable) {
            while (next != null) {
                final WeakEntry<E> e = next;
                next = next.next;                           // Fetch `next` now because its value will change.
                final int index = e.hash % table.length;
                e.next = table[index];
                table[index] = e;
            }
        }
        return table;
    }

    /**
     * If the number of elements is lower than this threshold, then the table should be
     * rehashed for saving space.
     *
     * @param  capacity  the table capacity.
     * @return minimal number of elements for not rehashing.
     */
    static int lowerCapacityThreshold(final int capacity) {
        return capacity >>> 2;
    }

    /**
     * If the number of elements is upper than this threshold, then the table should be
     * rehashed for better performance.
     *
     * @param  capacity  the table capacity.
     * @return maximal number of elements for not rehashing.
     */
    static int upperCapacityThreshold(final int capacity) {
        return capacity - (capacity >>> 2);
    }
}
