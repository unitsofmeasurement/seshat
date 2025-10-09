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

// Test dependencies
import tech.uom.seshat.TestCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests the {@link IndexedResourceBundle} subclasses.
 * This class arbitrarily use the following keys:
 *
 * <ul>
 *   <li>{@link tech.uom.seshat.resources.Errors.Keys#UnknownUnit_1}</li>
 * </ul>
 *
 * If the localized strings associated to those keys are modified,
 * then this {@code IndexedResourceBundleTest} class will need to be updated.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public final class IndexedResourceBundleTest extends TestCase {
    /**
     * The resource bundle in process of being tested.
     */
    private IndexedResourceBundle testing;

    /**
     * Creates a new test case.
     */
    public IndexedResourceBundleTest() {
    }

    /**
     * Tests the {@link Errors#forLocale(Locale)} method on different locales.
     */
    @Test
    public void testForLocale() {
        final Errors english = Errors.forLocale(Locale.ENGLISH);
        final Errors french  = Errors.forLocale(Locale.FRENCH);
        final Errors canada  = Errors.forLocale(Locale.CANADA);
        final Errors quebec  = Errors.forLocale(Locale.CANADA_FRENCH);
        assertSame   (english, Errors.forLocale(Locale.US));
        assertSame   (english, Errors.forLocale(Locale.UK));
        assertNotSame(english, french);
        assertSame   (english, canada);
        assertSame   (french,  quebec);

        assertSame(english, Errors.forLocale(Locale.ENGLISH));
        assertSame(canada,  Errors.forLocale(Locale.CANADA));
        assertSame(french,  Errors.forLocale(Locale.FRENCH));
        assertSame(quebec,  Errors.forLocale(Locale.CANADA_FRENCH));
    }

    /**
     * Tests the {@link IndexedResourceBundle#getKeys()} method.
     */
    @Test
    public void testGetKeys() {
        testing = Errors.forLocale(Locale.ENGLISH);
        final Enumeration<String> e = testing.getKeys();
        int count = 0;
        boolean foundNullArgument_1 = false;
        while (e.hasMoreElements()) {
            final String key = e.nextElement();
            if (key.equals("UnknownUnit_1")) {
                foundNullArgument_1 = true;
            }
            count++;
        }
        assertTrue(foundNullArgument_1);
        assertTrue(count > 5);
        testing = null;
    }

    /**
     * Tests the {@link IndexedResourceBundle#getString(short)} method on different locales.
     */
    @Test
    public void testGetString() {
        final Errors english = Errors.forLocale(Locale.ENGLISH);
        final Errors french  = Errors.forLocale(Locale.FRENCH);

        assertEquals("Unit “{0}” is not recognized.",             (testing = english).getString(Errors.Keys.UnknownUnit_1));
        assertEquals("Les unités « {0} » ne sont pas reconnues.", (testing = french) .getString(Errors.Keys.UnknownUnit_1));
        testing = null;
    }

    /**
     * Tests the {@link IndexedResourceBundle#getString(String)} method on different locales.
     */
    @Test
    public void testGetStringByName() {
        final Errors english = Errors.forLocale(Locale.ENGLISH);
        final Errors french  = Errors.forLocale(Locale.FRENCH);

        assertEquals("Unit “{0}” is not recognized.",             (testing = english).getString("UnknownUnit_1"));
        assertEquals("Les unités « {0} » ne sont pas reconnues.", (testing = french) .getString("UnknownUnit_1"));
        testing = null;
    }

    /**
     * Tests the {@link IndexedResourceBundle#getString(short, Object)} method on different locales.
     */
    @Test
    public void testGetStringWithParameter() {
        testing = Errors.forLocale(Locale.ENGLISH);
        assertEquals("Unit “X” is not recognized.", testing.getString(Errors.Keys.UnknownUnit_1, "X"));
        testing = Errors.forLocale(Locale.FRENCH);
        assertEquals("Les unités « X » ne sont pas reconnues.", testing.getString(Errors.Keys.UnknownUnit_1, "X"));
        testing = null;
    }
}
