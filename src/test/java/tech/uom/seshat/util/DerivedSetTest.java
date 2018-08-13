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
import java.util.HashSet;
import java.util.Arrays;
import java.util.function.Function;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests the {@link DerivedSet}. For the purpose of this test, this class implements an
 * {@link Function} for which input values are multiplied by 10, except value
 * {@value #EXCLUDED} which is converted to {@code null} (meaning: excluded from the
 * converted set).
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 */
public final strictfp class DerivedSetTest implements Function<Integer,Integer> {
    /**
     * The value to replace by {@code null}.
     */
    protected static final int EXCLUDED = 19;                   // non-private for javadoc purpose.

    /**
     * Tests {@link DerivedSet} without excluded value.
     */
    @Test
    public void testNoExclusion() {
        final Set<Integer> source = new HashSet<>(Arrays.asList(2,  7,  12,  17,  20 ));
        final Set<Integer> target = new HashSet<>(Arrays.asList(20, 70, 120, 170, 200));
        final Set<Integer> tested = new DerivedSet<>(source, this);
        assertEquals(target.size(), tested.size());
        assertEquals(target, tested);

        assertFalse(tested.contains(2 ));
        assertTrue (tested.contains(20));
        assertFalse(source.contains(3 ));
        assertTrue (source.contains(7 ));
    }

    /**
     * Tests {@link DerivedSet} with an excluded value.
     */
    @Test
    public void testWithExclusion() {
        final Set<Integer> source = new HashSet<>(Arrays.asList(2,  7,  12,  EXCLUDED, 20));
        final Set<Integer> target = new HashSet<>(Arrays.asList(20, 70, 120, 200));
        final Set<Integer> tested = new DerivedSet<>(source, this);
        assertEquals(target.size(), tested.size());
        assertEquals(target, tested);
        assertFalse(tested.contains(EXCLUDED * 10));
    }

    /**
     * Multiply the given value by 10, except value {@value #EXCLUDED}.
     *
     * @param  value  the value to multiply.
     * @return the multiplied value, or {@code null}.
     */
    @Override
    public Integer apply(final Integer value) {
        if (value == EXCLUDED) {
            return null;
        }
        return value * 10;
    }
}
