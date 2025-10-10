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

import static tech.uom.seshat.util.Characters.*;

// Test dependencies
import tech.uom.seshat.TestCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests the {@link Characters} methods.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Johann Sorel (Geomatys)
 */
public final class CharactersTest extends TestCase {
    /**
     * Creates a new test case.
     */
    public CharactersTest() {
    }

    /**
     * Tests the {@link Characters#trimFractionalPart(StringBuilder)} method.
     */
    @Test
    public void testTrimFractionalPart() {
        final var buffer = new StringBuilder("4.10");
        trimFractionalPart(buffer);
        assertEquals("4.10", buffer.toString());
        buffer.setCharAt(2, '0');                                   // Replace the '1' by '0'.
        trimFractionalPart(buffer);
        assertEquals("4", buffer.toString());
    }

    /**
     * Tests the {@link Characters#toASCII(CharSequence)} method.
     */
    @Test
    public void testToASCII() {
        final String metre = "metre";
        assertSame  (metre, toASCII(metre));
        assertEquals(metre, toASCII("mètre").toString());
        assertEquals("kg, mg, cm, km, km2, km3, ml, m/s, Pa, Hz, mol, ms, μs, m3, rad",
                toASCII("㎏, ㎎, ㎝, ㎞, ㎢, ㎦, ㎖, ㎧, ㎩, ㎐, ㏖, ㎳, ㎲, ㎥, ㎭").toString());
    }

    /**
     * Tests the {@link Characters#regionMatches(CharSequence, int, CharSequence)} and
     * {@link Characters#regionMatches(CharSequence, int, CharSequence)} methods.
     */
    @Test
    public void testRegionMatches() {
        assertTrue (regionMatches(new StringBuilder("Un chasseur sachant chasser sans son chien"), 12, "sachant"));
        assertTrue (regionMatches(new StringBuilder("Un chasseur sachant chasser sans son chien"), 12, "sacHant"));
    }

    /**
     * Tests {@link Characters#toSuperScript(char)}.
     */
    @Test
    public void testSuperScript() {
        for (char c='0'; c<='9'; c++) {
            final char s = toSuperScript(c);
            assertNotEquals(s, c);
            assertFalse(isSuperScript(c));
            assertTrue (isSuperScript(s));
            assertEquals(c, toNormalScript(s));
        }
        final char c = 'A';
        assertEquals(c, toSuperScript(c));
        assertEquals(c, toNormalScript(c));
        assertFalse(isSuperScript(c));
    }
}
