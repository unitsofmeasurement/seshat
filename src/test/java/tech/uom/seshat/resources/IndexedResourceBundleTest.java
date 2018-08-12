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
package tech.uom.seshat.resources;

import java.util.Locale;
import java.util.Enumeration;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests the {@link IndexedResourceBundle} subclasses.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 */
public final strictfp class IndexedResourceBundleTest {
    /**
     * The resource bundle in process of being tested.
     */
    private IndexedResourceBundle testing;

    /**
     * Tests the {@link Errors#getResources(Locale)} method on different locales.
     */
    @Test
    public void testGetResources() {
        final Errors english = Errors.getResources(Locale.ENGLISH);
        final Errors french  = Errors.getResources(Locale.FRENCH);
        final Errors canada  = Errors.getResources(Locale.CANADA);
        final Errors quebec  = Errors.getResources(Locale.CANADA_FRENCH);
        assertNotSame(english, french);

        assertSame(english, Errors.getResources(Locale.ENGLISH));
        assertSame(canada,  Errors.getResources(Locale.CANADA));
        assertSame(french,  Errors.getResources(Locale.FRENCH));
        assertSame(quebec,  Errors.getResources(Locale.CANADA_FRENCH));
    }

    /**
     * Tests the {@link IndexedResourceBundle#getKeys()} method.
     */
    @Test
    public void testGetKeys() {
        testing = Errors.getResources(Locale.ENGLISH);
        final Enumeration<String> e = testing.getKeys();
        int count = 0;
        boolean foundUnknownUnit_1 = false;
        while (e.hasMoreElements()) {
            final String key = e.nextElement();
            if (key.equals("UnknownUnit_1")) {
                foundUnknownUnit_1 = true;
            }
            count++;
        }
        assertTrue("foundUnknownUnit_1:", foundUnknownUnit_1);
        assertTrue("count > 5", count > 5);
        testing = null;
    }

    /**
     * Tests the {@link IndexedResourceBundle#getString(short)} method on different locales.
     */
    @Test
    public void testGetString() {
        final Errors english = Errors.getResources(Locale.ENGLISH);
        final Errors french  = Errors.getResources(Locale.FRENCH);

        assertEquals("Unit “{0}” is not recognized.",             (testing = english).getString(Errors.Keys.UnknownUnit_1));
        assertEquals("Les unités « {0} » ne sont pas reconnues.", (testing = french) .getString(Errors.Keys.UnknownUnit_1));
        testing = null;
    }

    /**
     * Tests the {@link IndexedResourceBundle#getString(String)} method on different locales.
     */
    @Test
    public void testGetStringByName() {
        final Errors english = Errors.getResources(Locale.ENGLISH);
        final Errors french  = Errors.getResources(Locale.FRENCH);

        assertEquals("Unit “{0}” is not recognized.",             (testing = english).getString("UnknownUnit_1"));
        assertEquals("Les unités « {0} » ne sont pas reconnues.", (testing = french) .getString("UnknownUnit_1"));
        testing = null;
    }

    /**
     * Tests the {@link IndexedResourceBundle#getString(short, Object)} method on different locales.
     */
    @Test
    public void testGetStringWithParameter() {
        testing = Errors.getResources(Locale.ENGLISH);
        assertEquals("Unit “X” is not recognized.", testing.getString(Errors.Keys.UnknownUnit_1, "X"));
        testing = Errors.getResources(Locale.FRENCH);
        assertEquals("Les unités « X » ne sont pas reconnues.", testing.getString(Errors.Keys.UnknownUnit_1, "X"));
        testing = null;
    }
}
