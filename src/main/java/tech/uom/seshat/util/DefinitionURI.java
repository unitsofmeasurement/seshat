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
import static tech.uom.seshat.util.CharSequences.*;


/**
 * Utility methods for parsing OGC's URI (URN or URL) in the {@code "urn:ogc:def"} namespace.
 * This class does not attempt to decode URL characters. For example a URL for "m/s" may be encoded as below,
 * in which case the value taken as the unit symbol would be {@code "m%2Fs"} instead of {@code "m/s"}.
 * <ul>
 *   <li>{@code http://www.opengis.net/def/uom/SI/0/m%2Fs}</li>
 * </ul>
 *
 * Some example of <cite>authorities</cite> are:
 * <table class="sis">
 *   <caption>Authority examples</caption>
 *   <tr><th>Authority</th>      <th>Purpose</th></tr>
 *   <tr><td>{@code "OGC"}</td>  <td>Objects defined by the Open Geospatial Consortium.</td></tr>
 *   <tr><td>{@code "EPSG"}</td> <td>Referencing objects defined in the EPSG database.</td></tr>
 *   <tr><td>{@code "EDCS"}</td> <td>Environmental Data Coding Specification.</td></tr>
 *   <tr><td>{@code "SI"}</td>   <td>International System of Units.</td></tr>
 *   <tr><td>{@code "UCUM"}</td> <td>Unified Code for Units of Measure.</td></tr>
 * </table>
 *
 * This class is a trimmed copy of Apache SIS {@code DefinitionURI} from Apache SIS 1.2 (not SIS 1.3 and later).
 * This class should not be loaded in the common case where authority codes for UOMs are not used.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public final class DefinitionURI {
    /**
     * The path separator in URN.
     */
    public static final char SEPARATOR = ':';

    /**
     * Do not allow instantiation of this class.
     */
    private DefinitionURI() {
    }

    /**
     * Returns {@code true} if a sub-region of {@code urn} matches the given {@code part},
     * ignoring case, leading and trailing whitespaces.
     *
     * @param  part   the expected part ({@code "urn"}, {@code "ogc"}, {@code "def"}, <i>etc.</i>)
     * @param  urn    the URN for which to test a subregion.
     * @param  lower  index of the first character in {@code urn} to compare, after skipping whitespaces.
     * @param  upper  index after the last character in {@code urn} to compare, ignoring whitespaces.
     * @return {@code true} if the given sub-region of {@code urn} match the given part.
     */
    private static boolean regionMatches(final String part, final String urn, int lower, int upper) {
        lower = skipLeadingWhitespaces (urn, lower, upper);
        upper = skipTrailingWhitespaces(urn, lower, upper);
        final int length = upper - lower;
        return (length == part.length()) && urn.regionMatches(true, lower, part, 0, length);
    }

    /**
     * Returns the substring of the given URN, ignoring whitespaces and version number if present.
     * The substring is expected to contains at most one {@code ':'} character. If such separator
     * character is present, then that character and everything before it are ignored.
     * The ignored part should be the version number, but this is not verified.
     *
     * <p>If the remaining substring is empty or contains more {@code ':'} characters, then this method
     * returns {@code null}. The presence of more {@code ':'} characters means that the code has parameters,
     * (e.g. {@code "urn:ogc:def:crs:OGC:1.3:AUTO42003:1:-100:45"}) which are not handled by this method.</p>
     *
     * @param  urn        the URN from which to get the code.
     * @param  fromIndex  index of the first character in {@code urn} to check.
     * @param  separator  separator between URN or HTTP path components.
     * @return the code part of the URN, or {@code null} if empty or invalid.
     */
    private static String codeIgnoreVersion(final String urn, int fromIndex, final char separator) {
        final int length = urn.length();
        fromIndex = skipLeadingWhitespaces(urn, fromIndex, length);
        if (fromIndex >= length) {
            return null;                            // Empty code.
        }
        final int s = urn.indexOf(separator, fromIndex);
        if (s >= 0) {
            // Ignore the version number (actually everything up to the first ':').
            fromIndex = skipLeadingWhitespaces(urn, s+1, length);
            if (fromIndex >= length || urn.indexOf(separator, fromIndex) >= 0) {
                return null;    // Empty code, or the code is followed by parameters.
            }
        }
        return urn.substring(fromIndex, skipTrailingWhitespaces(urn, fromIndex, length));
    }

    /**
     * Returns the code part of the given URI, provided that it matches the given object type and authority.
     * This method is useful when:
     *
     * <ul>
     *   <li>the URI is expected to have a specific <i>object type</i> and <i>authority</i>;</li>
     *   <li>the version number is considered irrelevant;</li>
     *   <li>the code is expected to have no parameters.</li>
     * </ul>
     *
     * This method accepts the following URI representations:
     *
     * <ul>
     *   <li>The given authority followed by the code (e.g. {@code "EPSG:4326"}).</li>
     *   <li>The URN form (e.g. {@code "urn:ogc:def:crs:EPSG::4326"}), ignoring version number.
     *       This method accepts also the former {@code "x-ogc"} in place of {@code "ogc"}.</li>
     *   <li>The HTTP form (e.g. {@code "http://www.opengis.net/def/crs/EPSG/0/4326"}).</li>
     * </ul>
     *
     * @param  authorities  the expected authorities, typically {@code "EPSG"}. See class javadoc for a list of authorities.
     * @param  uri          the URI to parse.
     * @return the code part of the given URI together with the authority as key, or {@code null} if the codespace
     *         does not match any given type and authority, the code is empty, or the code is followed by parameters.
     */
    public static Map.Entry<String, String> codeOf(final String[] authorities, final String uri) {
        int upper = uri.indexOf(SEPARATOR);
        if (upper < 0) {
            return null;
        }
        int lower  = skipLeadingWhitespaces(uri, 0, upper);
        int length = skipTrailingWhitespaces(uri, lower, upper) - lower;
        for (final String authority : authorities) {
            if (length == authority.length() && uri.regionMatches(true, lower, authority, 0, length)) {
                String code = codeIgnoreVersion(uri, upper+1, SEPARATOR);
                return (code == null) ? null : Map.entry(authority, code);
            }
        }
        /*
         * Check for supported protocols: only "urn" and "http(s)" at this time.
         * All other protocols are rejected as unrecognized.
         */
        String expected;
        switch (length) {
            case 3:  expected = "urn";   break;
            case 4:  expected = "http";  break;
            case 5:  expected = "https"; break;
            default: return null;
        }
        if (!uri.regionMatches(true, lower, expected, 0, length)) {
            return null;
        }
        final String[][] expectedParts = {
            new String[] {"ogc", "x-ogc"},
            new String[] {"def"},
            new String[] {"uom"},
            authorities
        };
        char separator = SEPARATOR;
        int skipAfterSeparator = 0;
        if (expected.startsWith("http")) {
            expectedParts[0] = new String[] {"//www.opengis.net"};
            separator = '/';
            skipAfterSeparator = 2;         // Skip "://" instead of ":".
        }
verify: for (final String[] expectOneOf : expectedParts) {
            lower = upper + 1;
            upper = uri.indexOf(separator, lower + skipAfterSeparator);
            if (upper < 0) return null;    // No more parts in the URN.
            skipAfterSeparator = 0;
            for (int i=0; i < expectOneOf.length; i++) {
                expected = expectOneOf[i];
                if (regionMatches(expected, uri, lower, upper)) {
                    continue verify;
                }
            }
            return null;
        }
        // In the last iteration, the expected part is an authority.
        String code = codeIgnoreVersion(uri, upper+1, separator);
        return (code == null) ? null : Map.entry(expected, code);
    }
}
