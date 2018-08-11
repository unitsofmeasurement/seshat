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

import java.text.Normalizer;

import static java.lang.Character.*;


/**
 * Static methods working on {@link StringBuilder} instances. Some methods defined in this
 * class duplicate the functionalities provided in the {@link CharSequences} class, but
 * modify directly the content of the provided {@code StringBuilder} instead than creating
 * new objects.
 *
 * <div class="section">Unicode support</div>
 * Every methods defined in this class work on <cite>code points</cite> instead than characters
 * when appropriate. Consequently those methods should behave correctly with characters outside
 * the <cite>Basic Multilingual Plane</cite> (BMP).
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 *
 * @see CharSequences
 */
public final class StringBuilders {
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
    private StringBuilders() {
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
     * <div class="section">Use case</div>
     * This method is useful after a {@linkplain StringBuilder#append(double) double value has
     * been appended to the buffer}, in order to make it appears like an integer when possible.
     *
     * @param  buffer  the buffer to trim if possible.
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
     * Implementation of the public {@code toASCII} methods.
     */
    static CharSequence toASCII(CharSequence text, StringBuilder buffer) {
        if (text != null) {
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
        }
        return text;
    }
}
