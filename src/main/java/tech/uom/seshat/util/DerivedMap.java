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
import java.util.function.Function;


/**
 * A map whose keys and values are derived <cite>on-the-fly</cite> from an other map.
 * Conversions are performed when needed by the following methods:
 *
 * <ul>
 *   <li>The iterators over the {@linkplain #keySet() key set} or {@linkplain #entrySet() entry set}
 *       obtain the derived keys using the {@link #keyConverter}.</li>
 *   <li>The iterators over the {@linkplain #values() values} or {@linkplain #entrySet() entry set}
 *       obtain the derived values using the {@link #valueConverter}.</li>
 * </ul>
 *
 * <h2>Constraints</h2>
 * <ul>
 *   <li>This map does not support {@code null} keys, since {@code null} is used as a
 *       sentinel value when no mapping from {@linkplain #storage} to {@code this} exists.</li>
 *   <li>This class is not thread-safe.</li>
 *   <li>Write operations available in Apache SIS are removed from this port to Seshat.</li>
 * </ul>
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.0
 *
 * @param <SK>  the type of keys in the storage map.
 * @param <SV>  the type of values in the storage map.
 * @param <K>   the type of keys in this map.
 * @param <V>   the type of values in this map.
 */
public final class DerivedMap<SK,SV,K,V> extends AbstractMap<K,V> implements Function<Map.Entry<SK,SV>, Map.Entry<K,V>> {
    /**
     * The storage map whose keys are derived from.
     */
    private final Map<SK,SV> storage;

    /**
     * The converter from the storage to the derived keys.
     */
    private final Function<SK,K> keyConverter;

    /**
     * The converter from the storage to the derived values.
     */
    private final Function<SV,V> valueConverter;

    /**
     * Key set. Will be constructed only when first needed.
     *
     * @see #keySet()
     */
    private transient Set<K> keySet;

    /**
     * Entry set. Will be constructed only when first needed.
     *
     * @see #entrySet()
     */
    private transient Set<Map.Entry<K,V>> entrySet;

    /**
     * Creates a new derived map from the specified storage map.
     *
     * @param storage         the map which actually store the entries.
     * @param keyConverter    the converter for the keys.
     * @param valueConverter  the converter for the values.
     */
    public DerivedMap(final Map<SK,SV> storage,
                      final Function<SK,K> keyConverter,
                      final Function<SV,V> valueConverter)
    {
        this.storage        = storage;
        this.keyConverter   = keyConverter;
        this.valueConverter = valueConverter;
    }

    /**
     * Returns the number of entries in this map.
     *
     * @return the number of entries in this map.
     */
    @Override
    public int size() {
        return keySet().size();
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map contains no key-value mappings.
     */
    @Override
    public boolean isEmpty() {
        return storage.isEmpty() || keySet().isEmpty();
    }

    /**
     * Returns a set view of the keys contained in this map.
     *
     * @return a view of the keys in this map.
     */
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public final Set<K> keySet() {
        if (keySet == null) {
            keySet = new DerivedSet<>(storage.keySet(), keyConverter);
        }
        return keySet;
    }

    /**
     * Returns a set view of the mappings contained in this map.
     *
     * @return a view of the entries in this map.
     */
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public final Set<Map.Entry<K,V>> entrySet() {
        if (entrySet == null) {
            entrySet = new DerivedSet<>(storage.entrySet(), this);
        }
        return entrySet;
    }

    /**
     * Converts the given entry.
     *
     * @param  entry an entry with the key and value from the storage.
     * @return an entry with the key and value converted to types declared by this map,
     *         or {@code null} if the key is unconvertible.
     */
    @Override
    public final Entry<K,V> apply(final Entry<SK,SV> entry) {
        final K key = keyConverter.apply(entry.getKey());
        if (key != null) {
            final V value = valueConverter.apply(entry.getValue());
            return new SimpleImmutableEntry<>(key, value);
        }
        return null;
    }
}
