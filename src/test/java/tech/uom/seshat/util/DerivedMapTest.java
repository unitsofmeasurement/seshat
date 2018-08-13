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
import java.util.HashMap;
import java.util.function.Function;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests the {@link DerivedMap}. For the purpose of this test, this class implements an
 * {@link Function} for which input values are multiplied by 100, except value
 * {@value #EXCLUDED} which is converted to {@code null} (meaning: excluded from the
 * converted map).
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 */
public final strictfp class DerivedMapTest implements Function<Integer,Integer> {
    /**
     * The value to replace by {@code null}.
     */
    protected static final int EXCLUDED = 17;                       // non-private for javadoc purpose.

    /**
     * Fills test values in the given maps.
     */
    private static void fill(final Map<Integer,Integer> source,
                             final Map<Integer,Integer> target)
    {
        assertNull(source.put(4,   7 ));
        assertNull(target.put(400, 70));
        assertNull(source.put(3,   8 ));
        assertNull(target.put(300, 80));
        assertNull(source.put(9,   1 ));
        assertNull(target.put(900, 10));
        assertNull(source.put(2,   1 ));
        assertNull(target.put(200, 10));
    }

    /**
     * Tests {@link DerivedMap} without excluded value.
     */
    @Test
    public void testNoExclusion() {
        final Map<Integer,Integer> source = new HashMap<>();
        final Map<Integer,Integer> target = new HashMap<>();
        final Map<Integer,Integer> tested = new DerivedMap<>(source, this, new DerivedSetTest());
        fill(source, target);
        assertEquals(target.size(),     tested.size());
        assertEquals(target.keySet(),   tested.keySet());
        assertEquals(target.entrySet(), tested.entrySet());
        assertEquals(target,            tested);
        assertTrue ("containsKey(400)", tested.containsKey(400));
        assertFalse("containsKey(4)",   tested.containsKey(4));
        assertEquals(8, source.get(3).intValue());
    }

    /**
     * Tests {@link DerivedMap} with an excluded key.
     */
    @Test
    public void testWithExclusion() {
        final Map<Integer,Integer> source = new HashMap<>();
        final Map<Integer,Integer> target = new HashMap<>();
        final Map<Integer,Integer> tested = new DerivedMap<>(source, this, new DerivedSetTest());
        fill(source, target);
        assertNull(source.put(EXCLUDED, 4));
        assertEquals(target.size(),     tested.size());
        assertEquals(target.keySet(),   tested.keySet());
        assertEquals(target.entrySet(), tested.entrySet());
        assertEquals(target,            tested);
    }

    /**
     * Multiplies the given value by 10, except value {@value #EXCLUDED}.
     *
     * @param  value  the value to multiply.
     * @return the multiplied value, or {@code null}.
     */
    @Override
    public Integer apply(final Integer value) {
        if (value == EXCLUDED) {
            return null;
        }
        return value * 100;
    }
}
