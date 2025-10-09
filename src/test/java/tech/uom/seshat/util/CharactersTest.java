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
 * Tests the {@link Characters} utility methods.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public final class CharactersTest extends TestCase {
    /**
     * Creates a new test case.
     */
    public CharactersTest() {
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
