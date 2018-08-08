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
package tech.uom.seshat.math;

import java.util.function.Function;


/**
 * Handles conversions from {@link Fraction} to other kind of numbers.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 */
public final class FractionConverter implements Function<Fraction,Integer> {
    /**
     * The unique instance of this converter.
     */
    public static final FractionConverter INSTANCE = new FractionConverter();

    /**
     * Creates a new converter.
     */
    private FractionConverter() {
    }

    /**
     * Converts the given fraction to an integer.
     *
     * @param  value  the fraction to convert.
     * @return the given fraction as an integer.
     * @throws IllegalArgumentException if the given fraction is not an integer.
     */
    @Override
    public Integer apply(final Fraction value) {
        if ((value.numerator % value.denominator) == 0) {
            return value.numerator / value.denominator;
        }
        throw new IllegalArgumentException();
    }

    /**
     * The inverse of {@link FractionConverter}.
     */
    public static final class FromInteger implements Function<Integer,Fraction> {
        /**
         * The unique instance of this converter.
         */
        public static final FromInteger INSTANCE = new FromInteger();

        /**
         * Creates a new converter.
         */
        private FromInteger() {
        }

        /**
         * Creates a new fraction from the given integer.
         *
         * @param  value  the integer to convert.
         * @return a fraction equals to the given integer.
         */
        @Override
        public Fraction apply(Integer value) {
            return new Fraction(value, 1);
        }
    }
}
