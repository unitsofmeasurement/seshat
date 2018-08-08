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

import java.util.Set;
import java.util.Iterator;
import java.util.AbstractSet;
import java.util.function.Function;


/**
 * A set whose values are derived <cite>on-the-fly</cite> from an other set.
 *
 * <div class="section">Constraints</div>
 * <ul>
 *   <li>This set does not support {@code null} values, since {@code null} is used as a
 *       sentinel value when no mapping from {@linkplain #storage} to {@code this} exists.</li>
 *   <li>This class performs no synchronization by itself. Nevertheless instances of this class
 *       may be thread-safe (depending on the sub-class implementation) if the underlying
 *       {@linkplain #storage} set (including its iterator) and the {@linkplain #converter}
 *       are thread-safe.</li>
 *   <li>Write operations available in Apache SIS are removed from this port to Seshat.</li>
 * </ul>
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.0
 *
 * @param <S>  the type of elements in the storage set.
 * @param <E>  the type of elements in this set.
 */
final class DerivedSet<S,E> extends AbstractSet<E> {
    /**
     * The storage set whose values are derived from.
     */
    private final Set<S> storage;

    /**
     * The converter from the storage to the derived type.
     */
    private final Function<S,E> converter;

    /**
     * Creates a new derived set from the specified storage set.
     *
     * @param  storage    the set which actually store the elements.
     * @param  converter  the converter from the storage to the derived type.
     */
    DerivedSet(final Set<S> storage, final Function<S,E> converter) {
        this.storage   = storage;
        this.converter = converter;
    }

    /**
     * Returns an iterator over the elements contained in this set.
     * The iterator will invoke the {@link Function#apply(Object)} method for each element.
     *
     * @return an iterator over the elements contained in this set.
     */
    @Override
    public final Iterator<E> iterator() {
        return new DerivedIterator<>(storage.iterator(), converter);
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return storage.isEmpty() || !iterator().hasNext();
    }

    /**
     * Returns the number of elements in this set. The default implementation counts the number of elements
     * returned by the {@link #iterator() iterator}. Subclasses are encouraged to cache this value if they
     * know that the underlying storage is immutable.
     *
     * @return the number of elements in this set.
     */
    @Override
    public int size() {
        int count = 0;
        for (final Iterator<E> it=iterator(); it.hasNext();) {
            it.next();
            if (++count == Integer.MAX_VALUE) {
                break;
            }
        }
        return count;
    }
}
