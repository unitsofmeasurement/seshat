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
import static java.lang.Character.*;


/**
 * Static methods working with {@link CharSequence} instances. Some methods defined in this
 * class duplicate the functionalities already provided in the standard {@link String} class,
 * but works on a generic {@code CharSequence} instance instead of {@code String}.
 *
 * <h2>Unicode support</h2>
 * Every methods defined in this class work on <cite>code points</cite> instead of characters
 * when appropriate. Consequently, those methods should behave correctly with characters outside
 * the <cite>Basic Multilingual Plane</cite> (BMP).
 *
 * <h2>Policy on space characters</h2>
 * Java defines two methods for testing if a character is a white space:
 * {@link Character#isWhitespace(int)} and {@link Character#isSpaceChar(int)}.
 * Those two methods differ in the way they handle no-break spaces, tabulations and line feeds.
 * The general policy in the Seshat library is:
 *
 * <ul>
 *   <li>Use {@code isWhitespace(…)} when separating entities (words, numbers, tokens, <i>etc.</i>)
 *       in a list. Using that method, characters separated by a no-break space are considered as
 *       part of the same entity.</li>
 *   <li>Use {@code isSpaceChar(…)} when parsing a single entity, for example a single word.
 *       Using this method, no-break spaces are considered as part of the entity while line
 *       feeds or tabulations are entity boundaries.</li>
 * </ul>
 *
 * <div class="note"><b>Example:</b>
 * Numbers formatted in the French locale use no-break spaces as group separators. When parsing a list of numbers,
 * ordinary spaces around the numbers may need to be ignored, but no-break spaces shall be considered as part of the
 * numbers. Consequently, {@code isWhitespace(…)} is appropriate for skipping spaces <em>between</em> the numbers.
 * But if there is spaces to skip <em>inside</em> a single number, then {@code isSpaceChar(…)} is a good choice
 * for accepting no-break spaces and for stopping the parse operation at tabulations or line feed character.
 * A tabulation or line feed between two characters is very likely to separate two distinct values.</div>
 *
 * <p>Note that the {@link String#trim()} method doesn't follow any of those policies and should
 * generally be avoided. That {@code trim()} method removes every ISO control characters without
 * distinction about whether the characters are space or not, and ignore all Unicode spaces.</p>
 *
 * <h2>Handling of null values</h2>
 * Most methods in this class accept a {@code null} {@code CharSequence} argument. In such cases
 * the method return value is either a {@code null} {@code CharSequence}, an empty array, or a
 * {@code 0} or {@code false} primitive type calculated as if the input was an empty string.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.2
 *
 * @see StringBuilders
 */
public final class CharSequences {
    /**
     * Do not allow instantiation of this class.
     */
    private CharSequences() {
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
     * Returns the index within the given strings of the first occurrence of the specified part,
     * starting at the specified index. This method is equivalent to the following method call,
     * except that this method works on arbitrary {@link CharSequence} objects instead of
     * {@link String}s only, and that the upper limit can be specified:
     *
     * {@snippet lang="java" :
     *     return text.indexOf(part, fromIndex);
     *     }
     *
     * There is no restriction on the value of {@code fromIndex}. If negative or greater
     * than {@code toIndex}, then the behavior of this method is as if the search started
     * from 0 or {@code toIndex} respectively. This is consistent with the
     * {@link String#indexOf(String, int)} behavior.
     *
     * @param  text       the string in which to perform the search.
     * @param  toSearch   the substring for which to search.
     * @param  fromIndex  the index from which to start the search.
     * @param  toIndex    the index after the last character where to perform the search.
     * @return the index within the text of the first occurrence of the specified part, starting at the specified index,
     *         or -1 if no occurrence has been found or if the {@code text} argument is null.
     * @throws NullPointerException if the {@code toSearch} argument is null.
     * @throws IllegalArgumentException if the {@code toSearch} argument is empty.
     *
     * @see String#indexOf(String, int)
     * @see StringBuilder#indexOf(String, int)
     * @see StringBuffer#indexOf(String, int)
     */
    private static int indexOf(final CharSequence text, final CharSequence toSearch, int fromIndex, int toIndex) {
        if (text != null) {
            int length = text.length();
            if (toIndex > length) {
                toIndex = length;
            }
            if (toSearch instanceof String && toIndex == length) {
                if (text instanceof String) {
                    return ((String) text).indexOf((String) toSearch, fromIndex);
                }
                if (text instanceof StringBuilder) {
                    return ((StringBuilder) text).indexOf((String) toSearch, fromIndex);
                }
                if (text instanceof StringBuffer) {
                    return ((StringBuffer) text).indexOf((String) toSearch, fromIndex);
                }
            }
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            length = toSearch.length();
            toIndex -= length;
search:     for (; fromIndex <= toIndex; fromIndex++) {
                for (int i=0; i<length; i++) {
                    // No need to use the codePointAt API here, since we are looking for exact matches.
                    if (text.charAt(fromIndex + i) != toSearch.charAt(i)) {
                        continue search;
                    }
                }
                return fromIndex;
            }
        }
        return -1;
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
     * Replaces some Unicode characters by ASCII characters on a "best effort basis".
     * For example, the “ é ” character is replaced by  “ e ” (without accent),
     * the  “ ″ ” symbol for minutes of angle is replaced by straight double quotes “ " ”,
     * and combined characters like ㎏, ㎎, ㎝, ㎞, ㎢, ㎦, ㎖, ㎧, ㎩, ㎐, <i>etc.</i> are replaced
     * by the corresponding sequences of characters.
     *
     * @param  text  the text to scan for Unicode characters to replace by ASCII characters, or {@code null}.
     * @return the given text with substitutions applied, or {@code text} if no replacement
     *         has been applied, or {@code null} if the given text was null.
     *
     * @see java.text.Normalizer
     */
    public static CharSequence toASCII(final CharSequence text) {
        return StringBuilders.toASCII(text, null);
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
     * Returns {@code true} if the given code points are equal, ignoring case.
     * This method implements the same comparison algorithm than String#equalsIgnoreCase(String).
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
     * Replaces all occurrences of a given string in the given character sequence. If no occurrence of
     * {@code toSearch} is found in the given text or if {@code toSearch} is equal to {@code replaceBy},
     * then this method returns the {@code text} unchanged.
     * Otherwise this method returns a new character sequence with all occurrences replaced by {@code replaceBy}.
     *
     * <p>This method is similar to {@link String#replace(CharSequence, CharSequence)} except that is accepts
     * arbitrary {@code CharSequence} objects. As of Java 10, another difference is that this method does not
     * create a new {@code String} if {@code toSearch} is equal to {@code replaceBy}.</p>
     *
     * @param  text       the character sequence in which to perform the replacements, or {@code null}.
     * @param  toSearch   the string to replace.
     * @param  replaceBy  the replacement for the searched string.
     * @return the given text with replacements applied, or {@code text} if no replacement has been applied,
     *         or {@code null} if the given text was null
     *
     * @see String#replace(char, char)
     * @see String#replace(CharSequence, CharSequence)
     */
    public static CharSequence replace(final CharSequence text, final CharSequence toSearch, final CharSequence replaceBy) {
        if (text != null && !toSearch.equals(replaceBy)) {
            if (text instanceof String) {
                return ((String) text).replace(toSearch, replaceBy);
            }
            final int length = text.length();
            int i = indexOf(text, toSearch, 0, length);
            if (i >= 0) {
                int p = 0;
                final int sl = toSearch.length();
                final StringBuilder buffer = new StringBuilder(length + (replaceBy.length() - sl));
                do {
                    buffer.append(text, p, i).append(replaceBy);
                    i = indexOf(text, toSearch, p = i + sl, length);
                } while (i >= 0);
                return buffer.append(text, p, length);
            }
        }
        return text;
    }
}
