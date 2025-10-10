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

import java.util.Objects;
import java.text.Normalizer;
import static java.lang.Character.*;


/**
 * Static methods working on character or sequence of characters.
 * This class contains copies of selected methods from the following classes of Apache SIS:
 *
 * <ul>
 *   <li>{@code org.apache.sis.util.Characters}</li>
 *   <li>{@code org.apache.sis.util.CharSequences}</li>
 *   <li>{@code org.apache.sis.util.StringBuilders}</li>
 * </ul>
 *
 * <h2>Unicode support</h2>
 * Every methods defined in this class work on <i>code points</i> instead of characters
 * when appropriate. Consequently, those methods should behave correctly with characters
 * outside the <i>Basic Multilingual Plane</i> (BMP).
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public final class Characters {
    /**
     * Letters in the range 00C0 (192) to 00FF (255) inclusive with their accent removed, when possible.
     * This string partially duplicates the work done by {@link Normalizer} with additional replacements.
     * We use it for more direct character replacements (compared to using {@code Normalizer} than removing
     * combining marks) for those common and easy cases.
     */
    private static final String ASCII = "AAAAAAÆCEEEEIIIIDNOOOOO*OUUUUYÞsaaaaaaæceeeeiiiionooooo/ouuuuyþy";
    // Original letters (with accent) = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";

    /**
     * Do not allow instantiation of this class.
     */
    private Characters() {
    }

    /**
     * Returns the {@linkplain CharSequence#length() length} of the given characters sequence,
     * or 0 if {@code null}.
     *
     * @param  text  the character sequence from which to get the length, or {@code null}.
     * @return the length of the character sequence, or 0 if the argument is {@code null}.
     */
    private static int length(final CharSequence text) {
        return (text != null) ? text.length() : 0;
    }

    /**
     * Returns the index of the first non-white character in the given range.
     * If the given range contains only space characters, then this method returns the index of the
     * first character after the given range, which is always equals or greater than {@code toIndex}.
     * Note that this character may not exist if {@code toIndex} is equal to the text length.
     *
     * <p>Special cases:</p>
     * <ul>
     *   <li>If {@code fromIndex} is greater than {@code toIndex},
     *       then this method unconditionally returns {@code fromIndex}.</li>
     *   <li>If the given range contains only space characters and the character at {@code toIndex-1}
     *       is the high surrogate of a valid supplementary code point, then this method returns
     *       {@code toIndex+1}, which is the index of the next code point.</li>
     *   <li>If {@code fromIndex} is negative or {@code toIndex} is greater than the text length,
     *       then the behavior of this method is undefined.</li>
     * </ul>
     *
     * Space characters are identified by the {@link Character#isWhitespace(int)} method.
     *
     * @param  text       the string in which to perform the search (cannot be null).
     * @param  fromIndex  the index from which to start the search (cannot be negative).
     * @param  toIndex    the index after the last character where to perform the search.
     * @return the index within the text of the first occurrence of a non-space character, starting
     *         at the specified index, or a value equals or greater than {@code toIndex} if none.
     * @throws NullPointerException if the {@code text} argument is null.
     *
     * @see #skipTrailingWhitespaces(CharSequence, int, int)
     */
    public static int skipLeadingWhitespaces(final CharSequence text, int fromIndex, final int toIndex) {
        while (fromIndex < toIndex) {
            final int c = codePointAt(text, fromIndex);
            if (!isWhitespace(c)) break;
            fromIndex += charCount(c);
        }
        return fromIndex;
    }

    /**
     * Returns the index <em>after</em> the last non-white character in the given range.
     * If the given range contains only space characters, then this method returns the index of the
     * first character in the given range, which is always equals or lower than {@code fromIndex}.
     *
     * <p>Special cases:</p>
     * <ul>
     *   <li>If {@code fromIndex} is lower than {@code toIndex},
     *       then this method unconditionally returns {@code toIndex}.</li>
     *   <li>If the given range contains only space characters and the character at {@code fromIndex}
     *       is the low surrogate of a valid supplementary code point, then this method returns
     *       {@code fromIndex-1}, which is the index of the code point.</li>
     *   <li>If {@code fromIndex} is negative or {@code toIndex} is greater than the text length,
     *       then the behavior of this method is undefined.</li>
     * </ul>
     *
     * Space characters are identified by the {@link Character#isWhitespace(int)} method.
     *
     * @param  text       the string in which to perform the search (cannot be null).
     * @param  fromIndex  the index from which to start the search (cannot be negative).
     * @param  toIndex    the index after the last character where to perform the search.
     * @return the index within the text of the last occurrence of a non-space character, starting
     *         at the specified index, or a value equals or lower than {@code fromIndex} if none.
     * @throws NullPointerException if the {@code text} argument is null.
     *
     * @see #skipLeadingWhitespaces(CharSequence, int, int)
     */
    public static int skipTrailingWhitespaces(final CharSequence text, final int fromIndex, int toIndex) {
        while (toIndex > fromIndex) {
            final int c = codePointBefore(text, toIndex);
            if (!isWhitespace(c)) break;
            toIndex -= charCount(c);
        }
        return toIndex;
    }

    /**
     * Returns a sub-sequence with leading and trailing whitespace characters omitted.
     * Space characters are identified by the {@link Character#isWhitespace(int)} method.
     *
     * <p>Invoking this method is functionally equivalent to the following code snippet,
     * except that the {@link CharSequence#subSequence(int, int) subSequence} method is
     * invoked only once instead of two times:</p>
     *
     * {@snippet lang="java" :
     *     text = trimWhitespaces(text.subSequence(lower, upper));
     *     }
     *
     * @param  text   the text from which to remove leading and trailing white spaces.
     * @param  lower  index of the first character to consider for inclusion in the sub-sequence.
     * @param  upper  index after the last character to consider for inclusion in the sub-sequence.
     * @return a characters sequence with leading and trailing white spaces removed, or {@code null}
     *         if the {@code text} argument is null.
     * @throws IndexOutOfBoundsException if {@code lower} or {@code upper} is out of bounds.
     */
    public static CharSequence trimWhitespaces(CharSequence text, int lower, int upper) {
        final int length = length(text);
        Objects.checkFromToIndex(lower, upper, length);
        if (text != null) {
            lower = skipLeadingWhitespaces (text, lower, upper);
            upper = skipTrailingWhitespaces(text, lower, upper);
            if (lower != 0 || upper != length) {                  // Safety in case subSequence doesn't make the check.
                text = text.subSequence(lower, upper);
            }
        }
        return text;
    }

    /**
     * Trims the fractional part of the given formatted number, provided that it doesn't change
     * the value. This method assumes that the number is formatted in the US locale, typically
     * by the {@link Double#toString(double)} method.
     *
     * <p>More specifically if the given buffer ends with a {@code '.'} character followed by a
     * sequence of {@code '0'} characters, then those characters are removed. Otherwise this
     * method does nothing. This is a <q>all or nothing</q> method: either the fractional
     * part is completely removed, or either it is left unchanged.</p>
     *
     * <h4>Use case</h4>
     * This method is useful after a {@linkplain StringBuilder#append(double) double value has
     * been appended to the buffer}, in order to make it appears like an integer when possible.
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

    /**
     * Replaces some Unicode characters by ASCII characters on a "best effort basis".
     * For example, the “ é ” character is replaced by  “ e ” (without accent),
     * the  “ ″ ” symbol for minutes of angle is replaced by straight double quotes “ " ”,
     * and combined characters like ㎏, ㎎, ㎝, ㎞, ㎢, ㎦, ㎖, ㎧, ㎩, ㎐, <i>etc.</i> are replaced
     * by the corresponding sequences of characters.
     *
     * @param  text  the text to scan for Unicode characters to replace by ASCII characters.
     * @return the given text with substitutions applied, or {@code text} if no replacement has been applied.
     *
     * @see java.text.Normalizer
     */
    public static CharSequence toASCII(CharSequence text) {
        StringBuilder buffer = null;
        boolean doneNFKD = false;
        /*
         * Scan the buffer in reverse order because we may suppress some characters.
         */
        int i = text.length();
        while (i > 0) {
            final int c = codePointBefore(text, i);
            final int n = charCount(c);
            final int r = c - 0xC0;
            i -= n;                                     // After this line, 'i' is the index of character 'c'.
            if (r >= 0) {
                final char cr;                          // The character replacement.
                if (r < ASCII.length()) {
                    cr = ASCII.charAt(r);
                } else {
                    switch (getType(c)) {
                        case FORMAT:
                        case CONTROL:                   // Character.isIdentifierIgnorable
                        case NON_SPACING_MARK:          cr = 0; break;
                        case PARAGRAPH_SEPARATOR:       // Fall through
                        case LINE_SEPARATOR:            cr = '\n'; break;
                        case SPACE_SEPARATOR:           cr = ' '; break;
                        case INITIAL_QUOTE_PUNCTUATION: cr = (c == '‘') ? '\'' : '"'; break;
                        case FINAL_QUOTE_PUNCTUATION:   cr = (c == '’') ? '\'' : '"'; break;
                        case OTHER_PUNCTUATION:
                        case MATH_SYMBOL: {
                            switch (c) {
                                case '⋅': cr = '*';  break;
                                case '∕': cr = '/';  break;
                                case '′': cr = '\''; break;
                                case '″': cr = '"';  break;
                                default:  continue;
                            }
                            break;
                        }
                        default: {
                            /*
                             * For any unknown character, try to decompose the string in a sequence of simpler
                             * letters with their modifiers and restart the whole process from the beginning.
                             * If the character is still unknown after decomposition, leave it unchanged.
                             */
                            if (!doneNFKD) {
                                doneNFKD = true;
                                final String decomposed = Normalizer.normalize(text, Normalizer.Form.NFKD);
                                if (!decomposed.contentEquals(text)) {
                                    if (buffer == null) {
                                        text = buffer = new StringBuilder(decomposed.length());
                                    } else {
                                        buffer.setLength(0);
                                    }
                                    i = buffer.append(decomposed).length();
                                }
                            }
                            continue;
                        }
                    }
                }
                if (buffer == null) {
                    buffer = new StringBuilder(text.length()).append(text);
                    text = buffer;
                }
                if (cr == 0) {
                    buffer.delete(i, i + n);
                } else {
                    if (n == 2) {
                        buffer.deleteCharAt(i + 1);         // Remove the low surrogate of a surrogate pair.
                    }
                    /*
                     * Nothing special to do about codepoint here, since 'c' is in
                     * the basic plane (verified by the r < ASCII.length() check).
                     */
                    buffer.setCharAt(i, cr);
                }
            }
        }
        return text;
    }

    /**
     * Returns {@code true} if the given code points are equal, ignoring case.
     * This method implements the same comparison algorithm as String#equalsIgnoreCase(String).
     *
     * <p>This method does not verify if {@code c1 == c2}. This check should have been done
     * by the caller, since the caller code is a more optimal place for this check.</p>
     */
    private static boolean equalsIgnoreCase(int c1, int c2) {
        c1 = toUpperCase(c1);
        c2 = toUpperCase(c2);
        if (c1 == c2) {
            return true;
        }
        // Need this check for Georgian alphabet.
        return toLowerCase(c1) == toLowerCase(c2);
    }

    /**
     * Returns {@code true} if the given text at the given offset contains the given part,
     * in a case-insensitive comparison. This method is equivalent to the following code,
     * except that this method works on arbitrary {@link CharSequence} objects instead of
     * {@link String}s only:
     *
     * {@snippet lang="java" :
     *     return text.regionMatches(ignoreCase, offset, part, 0, part.length());
     *     }
     *
     * This method does not thrown {@code IndexOutOfBoundsException}. Instead, if
     * {@code fromIndex < 0} or {@code fromIndex + part.length() > text.length()},
     * then this method returns {@code false}.
     *
     * @param  text       the character sequence for which to tests for the presence of {@code part}.
     * @param  fromIndex  the offset in {@code text} where to test for the presence of {@code part}.
     * @param  part       the part which may be present in {@code text}.
     * @return {@code true} if {@code text} contains {@code part} at the given {@code offset}.
     * @throws NullPointerException if any of the arguments is null.
     *
     * @see String#regionMatches(boolean, int, String, int, int)
     */
    public static boolean regionMatches(final CharSequence text, int fromIndex, final CharSequence part) {
        // Do not check for String cases. We do not want to delegate to String.regionMatches
        // because we compare code points while String.regionMatches(…) compares characters.
        final int limit  = text.length();
        final int length = part.length();
        if (fromIndex < 0) {                // Not checked before because we want NullPointerException if an argument is null.
            return false;
        }
        for (int i=0; i<length;) {
            if (fromIndex >= limit) {
                return false;
            }
            final int c1 = codePointAt(part, i);
            final int c2 = codePointAt(text, fromIndex);
            if (c1 != c2 && !equalsIgnoreCase(c1, c2)) {
                return false;
            }
            fromIndex += charCount(c2);
            i += charCount(c1);
        }
        return true;
    }

    /**
     * Determines whether the given character is a superscript. Most (but not all) superscripts
     * have a Unicode value in the [2070 … 207F] range. Superscripts are the following symbols:
     *
     * <blockquote>
     *   ⁰ ¹ ² ³ ⁴ ⁵ ⁶ ⁷ ⁸ ⁹ ⁺ ⁻ ⁼ ⁽ ⁾ ⁿ
     * </blockquote>
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
     * <blockquote>
     *   ₀ ₁ ₂ ₃ ₄ ₅ ₆ ₇ ₈ ₉ ₊ ₋ ₌ ₍ ₎
     * </blockquote>
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
     * <blockquote>
     *     0 1 2 3 4 5 6 7 8 9 + - = ( ) n
     * </blockquote>
     *
     * @param  c  the character to convert.
     * @return the given character as a superscript, or {@code c} if the given character cannot be converted.
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
     * Converts the given character argument to normal script.
     *
     * @param  c  the character to convert.
     * @return the given character as a normal script, or {@code c} if the
     *         given character was not a superscript or a subscript.
     */
    public static char toNormalScript(char c) {
        // Cast is safe because all return values are in the Basic Multilingual Plane (BMP).
        return (char) toNormalScript((int) c);
    }

    /**
     * Converts the given code point to normal script.
     *
     * @param  c  the character to convert.
     * @return the given character as a normal script, or {@code c} if the
     *         given character was not a superscript or a subscript.
     */
    public static int toNormalScript(int c) {
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
}
