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
import static tech.uom.seshat.util.StringBuilders.*;


/**
 * Tests the {@link StringBuilders} methods.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Johann Sorel (Geomatys)
 * @version 1.0
 */
public final strictfp class StringBuildersTest {
    /**
     * Tests the {@link StringBuilders#trimFractionalPart(StringBuilder)} method.
     */
    @Test
    public void testTrimFractionalPart() {
        final StringBuilder buffer = new StringBuilder("4.10");
        trimFractionalPart(buffer);
        assertEquals("4.10", buffer.toString());
        buffer.setCharAt(2, '0');                                   // Replace the '1' by '0'.
        trimFractionalPart(buffer);
        assertEquals("4", buffer.toString());
    }
}
