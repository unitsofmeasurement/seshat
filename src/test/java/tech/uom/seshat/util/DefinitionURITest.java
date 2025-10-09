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

// Test dependencies
import tech.uom.seshat.TestCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests {@link DefinitionURI}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public final class DefinitionURITest extends TestCase {
    /**
     * Creates a new test case.
     */
    public DefinitionURITest() {
    }

    /**
     * Convenience method invoking {@link DefinitionURI#codeOf(String[], String)}
     * with a single authority.
     */
    private static String codeOf(final String uri) {
        Map.Entry<String, String> entry = DefinitionURI.codeOf(new String[] {"EPSG"}, uri);
        if (entry == null) {
            return null;
        }
        assertEquals("EPSG", entry.getKey());
        return entry.getValue();
    }

    /**
     * Tests {@link DefinitionURI#codeOf(String[], String)}
     * with URI like {@code "EPSG:9102"} (the code for meters).
     */
    @Test
    public void testCodeOfEPSG() {
        assertNull  (        codeOf("9102"));
        assertEquals("9102", codeOf("EPSG:9102"));
        assertEquals("9102", codeOf("EPSG::9102"));
        assertNull  (        codeOf("EPSG:::9102"));
        assertEquals("9102", codeOf("EPSG:8.2:9102"));
        assertEquals("9102", codeOf(" epsg : 9102 "));
        assertEquals("9102", codeOf(" epsg :: 9102 "));
        assertEquals("9102", codeOf(" epsg : : 9102 "));
    }

    /**
     * Tests {@link DefinitionURI#codeOf(String[], String)}
     * with URN like {@code "urn:ogc:def:crs:EPSG::9102"}.
     */
    @Test
    public void testCodeOfURN() {
        assertEquals("9102",  codeOf("urn:ogc:def:uom:EPSG:9102"));
        assertEquals("9102",  codeOf("urn:ogc:def:uom:EPSG::9102"));
        assertEquals("9102",  codeOf("urn:ogc:def:uom:EPSG:8.2:9102"));
        assertEquals("9102",  codeOf("urn:x-ogc:def:uom:EPSG::9102"));
        assertNull  (         codeOf("urn:n-ogc:def:crs:EPSG::4326"));
        assertEquals("9102",  codeOf(" urn : ogc : def : uom : epsg : : 9102"));
        assertEquals("9102",  codeOf("urn:ogc:def:uom:EPSG:9102"));
    }

    /**
     * Tests {@link DefinitionURI#codeOf(String[], String)}
     * with URL like {@code "http://www.opengis.net/def/uom/EPSG/0/9102"}.
     */
    @Test
    public void testCodeOfDefinitionServer() {
        assertEquals("9102", codeOf("http://www.opengis.net/def/uom/EPSG/0/9102"));
        assertNull  (        codeOf("http://www.opengis.net/def/crs/EPSG/0/4326"));
        assertEquals("9102", codeOf("https://www.opengis.net/def/uom/EPSG/0/9102"));
    }
}
