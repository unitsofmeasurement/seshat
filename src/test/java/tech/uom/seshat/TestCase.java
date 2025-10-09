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
package tech.uom.seshat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Base class of tests.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public abstract class TestCase {
    /**
     * Creates a new test case.
     */
    protected TestCase() {
    }

    /**
     * Asserts that the message of the given exception contains all the given keywords.
     * We do not test the exact exception message because it is locale-dependent.
     * If the list of keywords is empty, then this method only verifies that the message is non-null.
     *
     * @param  exception  the exception for which to validate the message.
     * @param  keywords   the keywords which should be present in the exception.
     */
    protected static void assertMessageContains(final Throwable exception, final String... keywords) {
        final String message = exception.getMessage();
        assertNotNull(message, "Missing exception message.");
        for (final String keyword : keywords) {
            assertTrue(message.contains(keyword), () -> "Missing \"" + keyword + "\" in exception message: " + message);
        }
    }
}
