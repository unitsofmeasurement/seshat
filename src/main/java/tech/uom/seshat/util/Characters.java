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


/**
 * Static methods working on {@code char} values. This class is a port of Apache SIS {@code Characters},
 * {@code CharSequences} and {@code StringBuilders} classes merged together.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 */
public final class Characters {
    /**
     * Do not allow instantiation of this class.
     */
    private Characters() {
    }

    /**
     * Determines whether the given character is a superscript. Most (but not all) superscripts
     * have a Unicode value in the [2070 … 207F] range. Superscripts are the following symbols:
     *
     * <pre>{@code ⁰ ¹ ² ³ ⁴ ⁵ ⁶ ⁷ ⁸ ⁹ ⁺ ⁻ ⁼ ⁽ ⁾ ⁿ}</pre>
     *
     * @param  c  the character to test.
     * @return {@code true} if the given character is a superscript.
     */
    public static boolean isSuperScript(final int c) {
        switch (c) {
            case '¹':      // Legacy values in "Latin-1 supplement" space: 00B9, 00B2 and 00B3.
            case '²':      // Those values are outside the usual [2070 … 207F] range.
            case '³':      return true;
            case '\u2071': // Would be the '¹', '²' and '³' values if they were declared in the usual range.
            case '\u2072': // Since they are not, those values are unassigned.
            case '\u2073': return false;
            default:       return (c >= '⁰' && c <= 'ⁿ');
        }
    }

    /**
     * Determines whether the given character is a subscript. All subscripts have
     * a Unicode value in the [2080 … 208E]. Subscripts are the following symbols:
     *
     * <pre>{@code text ₀ ₁ ₂ ₃ ₄ ₅ ₆ ₇ ₈ ₉ ₊ ₋ ₌ ₍ ₎}</pre>
     *
     * @param  c  the character to test.
     * @return {@code true} if the given character is a subscript.
     */
    public static boolean isSubScript(final int c) {
        return (c >= '₀' && c <= '₎');
    }

    /**
     * Converts the given character argument to superscript.
     * Only the following characters can be converted (other characters are left unchanged):
     *
     * <pre>{@code 0 1 2 3 4 5 6 7 8 9 + - = ( ) n}</pre>
     *
     * @param  c  the character to convert.
     * @return the given character as a superscript, or {@code c} if the given character can not be converted.
     */
    public static char toSuperScript(char c) {
        switch (c) {
            case '1': c = '¹'; break;  // 00B9
            case '2': c = '²'; break;  // 00B2
            case '3': c = '³'; break;  // 00B3
            case '+': c = '⁺'; break;  // 207A
            case '-': c = '⁻'; break;  // 207B
            case '=': c = '⁼'; break;  // 207C
            case '(': c = '⁽'; break;  // 207D
            case ')': c = '⁾'; break;  // 207E
            case 'n': c = 'ⁿ'; break;  // 207F
            default: {
                if (c >= '0' && c <= '9') {
                    c += ('⁰' - '0');
                }
                break;
            }
        }
        return c;
    }

    /**
     * Converts the given character argument to subscript.
     * Only the following characters can be converted (other characters are left unchanged):
     *
     * <pre>{@code text 0 1 2 3 4 5 6 7 8 9 + - = ( )}</pre>
     *
     * @param  c  the character to convert.
     * @return the given character as a subscript, or {@code c} if the given character can not be converted.
     */
    public static char toSubScript(char c) {
        switch (c) {
            case '+': c = '₊'; break;  // 208A
            case '-': c = '₋'; break;  // 208B
            case '=': c = '₌'; break;  // 208C
            case '(': c = '₍'; break;  // 208D
            case ')': c = '₎'; break;  // 208E
            default: {
                if (c >= '0' && c <= '9') {
                    c += ('₀' - '0');
                }
                break;
            }
        }
        return c;
    }

    /**
     * Converts the given character argument to normal script.
     *
     * @param  c  the character to convert.
     * @return the given character as a normal script, or {@code c} if the
     *         given character was not a superscript or a subscript.
     */
    public static char toNormalScript(char c) {
        switch (c) {
            case '\u2071': // Exceptions to the default case. They would be the ¹²³
            case '\u2072': // cases if they were not defined in the Latin-1 range.
            case '\u2073':               break;
            case '¹':           c = '1'; break;
            case '²':           c = '2'; break;
            case '³':           c = '3'; break;
            case '⁺': case '₊': c = '+'; break;
            case '⁻': case '₋': c = '-'; break;
            case '⁼': case '₌': c = '='; break;
            case '⁽': case '₍': c = '('; break;
            case '⁾': case '₎': c = ')'; break;
            case 'ⁿ':           c = 'n'; break;
            default: {
                if (c >= '⁰' && c <= '₉') {
                    if      (c <= '⁹') c -= ('⁰' - '0');
                    else if (c >= '₀') c -= ('₀' - '0');
                }
                break;
            }
        }
        return c;
    }

    /**
     * Returns {@code true} if the given Unicode character is accepted by {@link #equalsFiltered(String, String)}.
     */
    private static boolean filter(final int codePoint) {
        return Character.isUnicodeIdentifierPart(codePoint) &&
              !Character.isIdentifierIgnorable(codePoint);
    }

    /**
     * Returns {@code true} if the given texts are equal, ignoring case and filtered-out characters.
     * This method is sometime used for comparing identifiers in a lenient way.
     *
     * @param  s1  the first characters sequence to compare, or {@code null}.
     * @param  s2  the second characters sequence to compare, or {@code null}.
     * @return whether the given texts are equal, ignoring filtered-out characters.
     */
    @SuppressWarnings("StringEquality")
    public static boolean equalsFiltered(final String s1, final String s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        final int lg1 = s1.length();
        final int lg2 = s2.length();
        int i1 = 0, i2 = 0;
        while (i1 < lg1) {
            int c1 = s1.codePointAt(i1);
            final int n = Character.charCount(c1);
            if (filter(c1)) {
                int c2;                     // Fetch the next significant character from the second string.
                do {
                    if (i2 >= lg2) {
                        return false;       // The first string has more significant characters than expected.
                    }
                    c2 = s2.codePointAt(i2);
                    i2 += Character.charCount(c2);
                } while (!filter(c2));

                // Compare the characters in the same way than String.equalsIgnoreCase(String).
                if (c1 != c2 && !equalsIgnoreCase(c1, c2)) {
                    return false;
                }
            }
            i1 += n;
        }
        while (i2 < lg2) {
            final int s = s2.codePointAt(i2);
            if (filter(s)) {
                return false;               // The first string has less significant characters than expected.
            }
            i2 += Character.charCount(s);
        }
        return true;
    }

    /**
     * Returns {@code true} if the given code points are equal, ignoring case.
     * This method implements the same comparison algorithm than String#equalsIgnoreCase(String).
     *
     * <p>This method does not verify if {@code c1 == c2}. This check should have been done
     * by the caller, since the caller code is a more optimal place for this check.</p>
     */
    private static boolean equalsIgnoreCase(int c1, int c2) {
        c1 = Character.toUpperCase(c1);
        c2 = Character.toUpperCase(c2);
        if (c1 == c2) {
            return true;
        }
        // Need this check for Georgian alphabet.
        return Character.toLowerCase(c1) == Character.toLowerCase(c2);
    }

    /**
     * Trims the fractional part of the given formatted number, provided that it doesn't change
     * the value. This method assumes that the number is formatted in the US locale, typically
     * by the {@link Double#toString(double)} method.
     *
     * <p>More specifically if the given buffer ends with a {@code '.'} character followed by a
     * sequence of {@code '0'} characters, then those characters are removed. Otherwise this
     * method does nothing. This is a <cite>"all or nothing"</cite> method: either the fractional
     * part is completely removed, or either it is left unchanged.</p>
     *
     * @param  buffer  the buffer to trim if possible.
     * @throws NullPointerException if the given {@code buffer} is null.
     */
    @SuppressWarnings("fallthrough")
    public static void trimFractionalPart(final StringBuilder buffer) {
        for (int i=buffer.length(); i > 0;) {
            switch (buffer.charAt(--i)) {               // No need to use Unicode code points here.
                case '0': continue;
                case '.': buffer.setLength(i);          // Fall through
                default : return;
            }
        }
    }
}
