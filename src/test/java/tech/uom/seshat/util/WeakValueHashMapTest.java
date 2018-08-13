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
import java.util.Random;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests the {@link WeakValueHashMap}.
 * A standard {@link HashMap} object is used for comparison purpose.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.0
 */
public final strictfp class WeakValueHashMapTest {
    /**
     * The size of the test sets to be created.
     */
    static final int SAMPLE_SIZE = 400;

    /**
     * Number of time to retry the tests.
     */
    private static final int NUM_RETRY = 2;

    /**
     * Tests the {@link WeakValueHashMap} using strong references.
     * The tested {@code WeakValueHashMap} shall behave like a standard {@link HashMap},
     * except for element order.
     */
    @Test
    public void testStrongReferences() {
        final Map<Integer,Integer> weakMap = new WeakValueHashMap<>(Integer.class);
        final Random random = new Random();
        for (int pass=0; pass<NUM_RETRY; pass++) {
            weakMap.clear();
            final HashMap<Integer,Integer> strongMap = new HashMap<>();
            for (int i=0; i<SAMPLE_SIZE; i++) {
                final Integer key   = random.nextInt(SAMPLE_SIZE);
                final Integer value = random.nextInt(SAMPLE_SIZE);
                assertEquals("containsKey:",   strongMap.containsKey(key),     weakMap.containsKey(key));
                assertEquals("containsValue:", strongMap.containsValue(value), weakMap.containsValue(value));
                assertSame  ("get:",           strongMap.get(key),             weakMap.get(key));
                if (random.nextBoolean()) {
                    // Test addition.
                    assertSame("put:", strongMap.put(key, value), weakMap.put(key, value));
                } else {
                    // Test remove
                    assertSame("remove:", strongMap.remove(key), weakMap.remove(key));
                }
                assertEquals(strongMap, weakMap);
            }
        }
    }

    /**
     * Tests the {@link WeakValueHashMap} using weak references.
     * In this test, we have to keep in mind than some elements
     * in {@code weakMap} may disappear at any time.
     */
    @Test
    public void testWeakReferences() {
        final Map<Integer,Integer> weakMap = new WeakValueHashMap<>(Integer.class);
        final Random random = new Random();
        for (int pass=0; pass<NUM_RETRY; pass++) {
            weakMap.clear();
            final HashMap<Integer,Integer> strongMap = new HashMap<>();
            for (int i=0; i<SAMPLE_SIZE; i++) {
                /*
                 * We really want new instances here.
                 */
                final Integer key   = new Integer(random.nextInt(SAMPLE_SIZE));
                final Integer value = new Integer(random.nextInt(SAMPLE_SIZE));
                if (random.nextBoolean()) {
                    /*
                     * Tests addition.
                     */
                    final Integer   weakPrevious = weakMap  .put(key, value);
                    final Integer strongPrevious = strongMap.put(key, value);
                    if (weakPrevious == null) {
                        /*
                         * The element was not in the WeakValueHashMap, possibly GC collected it.
                         * Consequently that element can not be in the HashMap neither, otherwise
                         * a strong reference would exist which should have prevented the element
                         * from being removed from the WeakValueHashMap.
                         */
                        assertNull("put:", strongPrevious);
                    } else {
                        assertNotSame(value, weakPrevious);
                    }
                    if (strongPrevious != null) {
                        /*
                         * Note: If 'strongPrevious==null', 'weakPrevious' can not
                         *       be null since GC has not collected its entry yet.
                         */
                        assertSame("put:", strongPrevious, weakPrevious);
                    }
                } else {
                    /*
                     * Tests remove.
                     */
                    final Integer   weakPrevious = weakMap.get(key);
                    final Integer strongPrevious = strongMap.remove(key);
                    if (strongPrevious != null) {
                        assertSame("remove:", strongPrevious, weakPrevious);
                    }
                }
                assertTrue("containsAll:", weakMap.entrySet().containsAll(strongMap.entrySet()));
            }
        }
    }
}
