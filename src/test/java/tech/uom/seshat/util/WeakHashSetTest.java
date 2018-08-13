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

import java.util.HashSet;
import java.util.Random;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests the {@link WeakHashSet}.
 * A standard {@link HashSet} object is used for comparison purpose.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.0
 */
public final strictfp class WeakHashSetTest {
    /**
     * The size of the test sets to be created.
     */
    private static final int SAMPLE_SIZE = 500;

    /**
     * Number of time to retry the tests.
     */
    private static final int NUM_RETRY = 4;

    /**
     * Tests the {@link WeakHashSet} using strong references.
     * The tested {@code WeakHashSet} shall behave like a standard {@link HashSet},
     * except for element order.
     */
    @Test
    public void testStrongReferences() {
        final Random random = new Random();
        for (int pass=0; pass<NUM_RETRY; pass++) {
            final WeakHashSet<Integer> weakSet = new WeakHashSet<>(Integer.class);
            final HashSet<Integer> strongSet = new HashSet<>();
            for (int i=0; i<SAMPLE_SIZE; i++) {
                final Integer value = random.nextInt(SAMPLE_SIZE);
                if (random.nextBoolean()) {
                    /*
                     * Tests addition.
                     */
                    final boolean   weakModified = weakSet  .add(value);
                    final boolean strongModified = strongSet.add(value);
                    assertEquals("add:", strongModified, weakModified);
                    if (strongModified) {
                        assertSame("get:", value, weakSet.get(value));
                    } else {
                        assertEquals("get:",  value, weakSet.get(value));
                    }
                } else {
                    /*
                     * Tests remove
                     */
                    final boolean   weakModified = weakSet  .remove(value);
                    final boolean strongModified = strongSet.remove(value);
                    assertEquals("remove:", strongModified, weakModified);
                    assertNull("get:", weakSet.get(value));
                }
                assertEquals("contains:", strongSet.contains(value), weakSet.contains(value));
                assertEquals("equals:", strongSet, weakSet);
            }
            assertEquals(strongSet, weakSet);
        }
    }

    /**
     * Tests the {@link WeakHashSet} using weak references. In this test, we have to keep
     * in mind that some elements in {@code weakSet} may disappear at any time!
     */
    @Test
    public void testWeakReferences() {
        final Random random = new Random();
        for (int pass=0; pass<NUM_RETRY; pass++) {
            final WeakHashSet<Integer> weakSet = new WeakHashSet<>(Integer.class);
            final HashSet<Integer> strongSet = new HashSet<>();
            for (int i=0; i<SAMPLE_SIZE; i++) {
                @SuppressWarnings("UnnecessaryBoxing")
                final Integer value = new Integer(random.nextInt(SAMPLE_SIZE));         // Really need new instances
                if (random.nextBoolean()) {
                    /*
                     * Tests addition.
                     */
                    final boolean   weakModified = weakSet  .add(value);
                    final boolean strongModified = strongSet.add(value);
                    if (weakModified) {
                        /*
                         * The element was not in the WeakHashSet, possibly GC collected it.
                         * Consequently that element can not be in the HashSet neither, otherwise
                         * a strong reference would exist which should have prevented the element
                         * from being removed from the WeakHashSet.
                         */
                        assertTrue("add:", strongModified);
                    } else {
                        assertNotSame(value, weakSet.get(value));
                        if (strongModified) {
                            /*
                             * The element was not in HashSet but still exist in the WeakHashSet.
                             * This is because GC has not cleared it yet. Replace the reference
                             * by 'value', otherwise it may be cleared later and the 'contains'
                             * test below would fail.
                             *
                             * Note: we don't test if 'remove' below returns 'true', because GC
                             *       may have already done its work since the few previous lines!
                             */
                            weakSet.remove(value);
                            assertTrue(weakSet.add(value));
                            assertSame(value, weakSet.get(value));
                        }
                    }
                } else {
                    /*
                     * Test remove.
                     */
                    final boolean c = weakSet.contains(value);
                    if (strongSet.remove(value)) {
                        assertTrue("contains:", c);
                    }
                }
                assertTrue("containsAll:", weakSet.containsAll(strongSet));
            }
        }
    }
}
