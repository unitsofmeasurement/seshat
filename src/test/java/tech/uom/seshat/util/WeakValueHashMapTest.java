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

import java.util.HashMap;
import java.util.Random;

// Test dependencies
import tech.uom.seshat.TestCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests the {@link WeakValueHashMap}.
 * A standard {@link HashMap} object is used for comparison purpose.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 */
public final class WeakValueHashMapTest extends TestCase {
    /**
     * The size of the test sets to be created.
     */
    static final int SAMPLE_SIZE = 400;

    /**
     * Number of time to retry the tests.
     */
    private static final int NUM_RETRY = 2;

    /**
     * Creates a new test case.
     */
    public WeakValueHashMapTest() {
    }

    /**
     * Tests the {@link WeakValueHashMap} using strong references.
     * The tested {@code WeakValueHashMap} shall behave like a standard {@link HashMap},
     * except for element order.
     */
    @Test
    public void testStrongReferences() {
        final var weakMap = new WeakValueHashMap<Integer,IntObject>(Integer.class);
        final Random random = new Random();
        for (int pass=0; pass<NUM_RETRY; pass++) {
            weakMap.clear();
            final var strongMap = new HashMap<Integer, IntObject>();
            for (int i=0; i<SAMPLE_SIZE; i++) {
                final Integer   key   = random.nextInt(SAMPLE_SIZE);
                final IntObject value = new IntObject(random.nextInt(SAMPLE_SIZE));
                assertEquals(strongMap.containsKey(key),     weakMap.containsKey(key),     "containsKey:");
                assertEquals(strongMap.containsValue(value), weakMap.containsValue(value), "containsValue:");
                assertSame  (strongMap.get(key),             weakMap.get(key),             "get:");
                if (random.nextBoolean()) {
                    // Test addition.
                    assertSame(strongMap.put(key, value), weakMap.put(key, value), "put:");
                } else {
                    // Test remove
                    assertSame(strongMap.remove(key), weakMap.remove(key), "remove:");
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
        final var weakMap = new WeakValueHashMap<Integer,IntObject>(Integer.class);
        final Random random = new Random();
        for (int pass=0; pass<NUM_RETRY; pass++) {
            weakMap.clear();
            final var strongMap = new HashMap<Integer, IntObject>();
            for (int i=0; i<SAMPLE_SIZE; i++) {
                final Integer   key   = random.nextInt(SAMPLE_SIZE);
                final IntObject value = new IntObject(random.nextInt(SAMPLE_SIZE));     // Really need new instances.
                if (random.nextBoolean()) {
                    /*
                     * Tests addition.
                     */
                    final IntObject   weakPrevious = weakMap  .put(key, value);
                    final IntObject strongPrevious = strongMap.put(key, value);
                    if (weakPrevious == null) {
                        /*
                         * The element was not in the WeakValueHashMap, possibly GC collected it.
                         * Consequently, that element cannot be in the HashMap neither, otherwise
                         * a strong reference would exist which should have prevented the element
                         * from being removed from the WeakValueHashMap.
                         */
                        assertNull(strongPrevious, "put:");
                    } else {
                        assertNotSame(value, weakPrevious);
                    }
                    if (strongPrevious != null) {
                        /*
                         * Note: If `strongPrevious==null`, `weakPrevious` cannot
                         *       be null since GC has not collected its entry yet.
                         */
                        assertSame(strongPrevious, weakPrevious, "put:");
                    }
                } else {
                    /*
                     * Tests remove.
                     */
                    final IntObject   weakPrevious = weakMap.get(key);
                    final IntObject strongPrevious = strongMap.remove(key);
                    if (strongPrevious != null) {
                        assertSame(strongPrevious, weakPrevious, "remove:");
                    }
                }
                assertTrue(weakMap.entrySet().containsAll(strongMap.entrySet()), "containsAll:");
            }
        }
    }

    /**
     * Tests {@code putIfAbsent(…)}, {@code replace(…)} and other optional methods.
     */
    @Test
    public void testOptionalMethods() {
        final var weakMap = new WeakValueHashMap<Integer,Integer>(Integer.class);
        final var reference = new HashMap<Integer, Integer>();
        final Random random = new Random();
        for (int i=0; i<100; i++) {
            final Integer key   = random.nextInt(10);
            final Integer value = random.nextInt(20);
            switch (random.nextInt(7)) {
                case 0: {
                    assertEquals(reference.get(key), weakMap.get(key));
                    break;
                }
                case 1: {
                    assertEquals(reference.put(key, value), weakMap.put(key, value));
                    break;
                }
                case 2: {
                    assertEquals(reference.putIfAbsent(key, value), weakMap.putIfAbsent(key, value));
                    break;
                }
                case 3: {
                    assertEquals(reference.replace(key, value), weakMap.replace(key, value));
                    break;
                }
                case 4: {
                    final Integer condition = random.nextInt(20);
                    assertEquals(reference.replace(key, condition, value), weakMap.replace(key, condition, value));
                    break;
                }
                case 5: {
                    assertEquals(reference.remove(key), weakMap.remove(key));
                    break;
                }
                case 6: {
                    assertEquals(reference.remove(key, value), weakMap.remove(key, value));
                    break;
                }
            }
        }
        assertEquals(reference, weakMap);
    }
}
