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

import org.junit.Test;

import static org.junit.Assert.*;
import static tech.uom.seshat.util.CharSequences.*;


/**
 * Tests the {@link CharSequences} methods.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Johann Sorel (Geomatys)
 * @version 1.2
 */
public final strictfp class CharSequencesTest {
    /**
     * Tests the {@link CharSequences#toASCII(CharSequence)} method.
     */
    @Test
    public void testToASCII() {
        final String metre = "metre";
        assertSame  (metre, toASCII(metre));
        assertEquals(metre, toASCII("mètre").toString());
        assertNull  (       toASCII(null));
        assertEquals("kg, mg, cm, km, km2, km3, ml, m/s, Pa, Hz, mol, ms, μs, m3, rad",
                toASCII("㎏, ㎎, ㎝, ㎞, ㎢, ㎦, ㎖, ㎧, ㎩, ㎐, ㏖, ㎳, ㎲, ㎥, ㎭").toString());
    }

    /**
     * Tests the {@link CharSequences#regionMatches(CharSequence, int, CharSequence)}.
     */
    @Test
    public void testRegionMatches() {
        assertTrue(regionMatches(new StringBuilder("Un chasseur sachant chasser sans son chien"), 12, "sachant"));
        assertTrue(regionMatches(new StringBuilder("Un chasseur sachant chasser sans son chien"), 12, "sacHant"));
    }

    /**
     * Tests the {@link CharSequences#replace(CharSequence, CharSequence, CharSequence)} method.
     */
    @Test
    public void testReplace() {
        final String text = "One apple, two orange oranges";
        assertSame(text, replace(text, "pineapple", "orange"));
        assertEquals("One orange, two orange oranges", replace(text, "apple", "orange").toString());
        assertEquals("One apple, two apple apples",    replace(text, "orange", "apple").toString());
        assertNull(replace(null, "orange", "apple"));
    }
}
