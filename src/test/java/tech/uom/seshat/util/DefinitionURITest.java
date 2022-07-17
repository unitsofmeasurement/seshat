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


/**
 * Tests {@link DefinitionURI}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.2
 * @since   1.2
 * @module
 */
public final strictfp class DefinitionURITest {
    /**
     * Tests {@link DefinitionURI#codeOf(String, String)}
     * with URI like {@code "EPSG:9102"} (the code for meters).
     */
    @Test
    public void testCodeOfEPSG() {
        assertNull  (        DefinitionURI.codeOf("EPSG", "9102"));
        assertEquals("9102", DefinitionURI.codeOf("EPSG", "EPSG:9102"));
        assertEquals("9102", DefinitionURI.codeOf("EPSG", "EPSG::9102"));
        assertNull  (        DefinitionURI.codeOf("EPSG", "EPSG:::9102"));
        assertEquals("9102", DefinitionURI.codeOf("EPSG", "EPSG:8.2:9102"));
        assertEquals("9102", DefinitionURI.codeOf("EPSG", " epsg : 9102 "));
        assertEquals("9102", DefinitionURI.codeOf("EPSG", " epsg :: 9102 "));
        assertEquals("9102", DefinitionURI.codeOf("EPSG", " epsg : : 9102 "));
    }

    /**
     * Tests {@link DefinitionURI#codeOf(String, String)}
     * with URN like {@code "urn:ogc:def:crs:EPSG::9102"}.
     */
    @Test
    public void testCodeOfURN() {
        assertEquals("9102",  DefinitionURI.codeOf("EPSG", "urn:ogc:def:uom:EPSG:9102"));
        assertEquals("9102",  DefinitionURI.codeOf("EPSG", "urn:ogc:def:uom:EPSG::9102"));
        assertEquals("9102",  DefinitionURI.codeOf("EPSG", "urn:ogc:def:uom:EPSG:8.2:9102"));
        assertEquals("9102",  DefinitionURI.codeOf("EPSG", "urn:x-ogc:def:uom:EPSG::9102"));
        assertNull  (         DefinitionURI.codeOf("EPSG", "urn:n-ogc:def:crs:EPSG::4326"));
        assertEquals("9102",  DefinitionURI.codeOf("EPSG", " urn : ogc : def : uom : epsg : : 9102"));
        assertEquals("9102",  DefinitionURI.codeOf("EPSG", "urn:ogc:def:uom:EPSG:9102"));
    }

    /**
     * Tests {@link DefinitionURI#codeOf(String, String)}
     * with URL like {@code "http://www.opengis.net/def/uom/EPSG/0/9102"}.
     */
    @Test
    public void testCodeOfDefinitionServer() {
        assertEquals("9102", DefinitionURI.codeOf("EPSG", "http://www.opengis.net/def/uom/EPSG/0/9102"));
        assertNull  (        DefinitionURI.codeOf("EPSG", "http://www.opengis.net/def/crs/EPSG/0/4326"));
        assertEquals("9102", DefinitionURI.codeOf("EPSG", "https://www.opengis.net/def/uom/EPSG/0/9102"));
    }
}
