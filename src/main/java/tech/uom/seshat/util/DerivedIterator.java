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

import java.util.Iterator;
import java.util.function.Function;


/**
 * An iterator which performs conversions on the fly using the given converter.
 * If a value is converted into a null value, then this iterator skips that value.
 * Consequently this iterator can not return null value.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.0
 *
 * @param <S>  the type of elements in the storage collection.
 * @param <E>  the type of elements in this set.
 */
final class DerivedIterator<S,E> implements Iterator<E> {
    /**
     * The original iterator to wrap.
     */
    private final Iterator<S> iterator;

    /**
     * The converter from the original values to the converted values.
     */
    private final Function<S,E> converter;

    /**
     * The next element to be returned, or {@code null}.
     */
    private transient E next;

    /**
     * Creates a new iterator wrapping the given original iterator and converting the
     * values using the given converter.
     */
    DerivedIterator(final Iterator<S> iterator, Function<S,E> converter) {
        this.iterator  = iterator;
        this.converter = converter;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     */
    @Override
    public boolean hasNext() {
        while (next == null) {
            if (!iterator.hasNext()) {
                return false;
            }
            next = converter.apply(iterator.next());
        }
        return true;
    }

    /**
     * Returns the next element in the iteration.
     */
    @Override
    public E next() {
        E value = next;
        next = null;
        while (value == null) {
            value = converter.apply(iterator.next());
        }
        return value;
    }

    /*
     * The `remove` operation available in Apache SIS is removed from this port to Seshat.
     */
}
