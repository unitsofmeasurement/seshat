/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Arrays;
import java.util.Objects;


/**
 * Simple mathematical functions in addition to the ones provided in {@link Math}.
 * This is a port of Apache SIS {@code MathFunctions}, {@code DecimalFunctions} and {@code Numerics} classes
 * merged together. The decimal functions works on {@code float} and {@code double} values while taking in
 * account their representation in base 10. Those methods are usually <strong>not</strong> recommended for
 * intermediate calculations, since base 10 is not more "real" than base 2 for natural phenomenon.
 *
 * @author  Martin Desruisseaux (MPO, IRD, Geomatys)
 * @author  Johann Sorel (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class MathFunctions {
    /**
     * Number of bits in the significand (mantissa) part of IEEE 754 {@code float} representation,
     * <strong>not</strong> including the hidden bit.
     */
    private static final int SIGNIFICAND_SIZE_OF_FLOAT = 23;

    /**
     * The greatest power of 10 such as {@code Math.pow(10, E10_FOR_ZERO) == 0}.
     * This is the exponent in {@code parseDouble("1E-324")} &lt; {@link Double#MIN_VALUE},
     * which is stored as zero because non-representable as a {@code double} value.
     * The next power, {@code parseDouble("1E-323")}, is a non-zero {@code double} value.
     *
     * @see Double#MIN_VALUE
     */
    static final int EXPONENT_FOR_ZERO = -324;

    /**
     * The maximal exponent value such as {@code parseDouble("1E+308")} still a finite number.
     *
     * @see Double#MAX_VALUE
     */
    static final int EXPONENT_FOR_MAX = 308;

    /**
     * The highest prime number supported by the {@link #nextPrimeNumber(int)} method.
     * In the current implementation, this value is {@value}. However this limit may
     * change in any future Apache SIS version.
     *
     * <p>The current value is the highest prime number representable as an unsigned 16 bits integer.
     * This is enough for current needs because 16 bits prime numbers are sufficient for finding
     * the divisors of any 32 bits integers.</p>
     *
     * @see #nextPrimeNumber(int)
     */
    public static final int HIGHEST_SUPPORTED_PRIME_NUMBER = 65521;

    /**
     * Maximal length needed for the {@link #primes} array in order to store prime numbers
     * from 2 to 32749 (15 bits) or {@value #HIGHEST_SUPPORTED_PRIME_NUMBER} (16 bits).
     *
     * @see #primeNumberAt(int)
     */
    static final int PRIMES_LENGTH_15_BITS = 3512,
                     PRIMES_LENGTH_16_BITS = 6542;

    /**
     * The sequence of prime numbers computed so far. Will be expanded as needed.
     * We limit ourself to 16 bits numbers because they are sufficient for computing
     * divisors of any 32 bits number.
     *
     * @see #primeNumberAt(int)
     */
    @SuppressWarnings("VolatileArrayField")     // Because we will not modify array content.
    private static volatile short[] primes = new short[] {2, 3};

    /**
     * Table of integer powers of 10, precomputed both for performance and accuracy reasons.
     * This table consumes 4.9 kb of memory. We pay this cost because integer powers of ten
     * are requested often, and {@link Math#pow(double, double)} has slight rounding errors.
     *
     * @see #pow10(int)
     */
    private static final double[] POW10 = new double[EXPONENT_FOR_MAX - EXPONENT_FOR_ZERO];
    static {
        final StringBuilder buffer = new StringBuilder("1E");
        for (int i=0; i<POW10.length; i++) {
            buffer.setLength(2);
            buffer.append(i + (EXPONENT_FOR_ZERO + 1));
            /*
             * Double.parseDouble("1E"+i) gives as good or better numbers than Math.pow(10,i)
             * for ALL integer powers, but is slower. We hope that the current workaround is only
             * temporary. See http://developer.java.sun.com/developer/bugParade/bugs/4358794.html
             */
            POW10[i] = Double.parseDouble(buffer.toString());
        }
    }

    /**
     * Do not allow instantiation of this class.
     */
    private MathFunctions() {
    }

    /**
     * Returns the <var>i</var><sup>th</sup> prime number.
     * This method returns (2, 3, 5, 7, 11, …) for index (0, 1, 2, 3, 4, …).
     *
     * @param  index  the prime number index, starting at index 0 for prime number 2.
     * @return the prime number at the specified index.
     * @throws IndexOutOfBoundsException if the specified index is too large.
     *
     * @see java.math.BigInteger#isProbablePrime(int)
     */
    static int primeNumberAt(final int index) throws IndexOutOfBoundsException {
        Objects.checkIndex(index, PRIMES_LENGTH_16_BITS);
        short[] primes = MathFunctions.primes;
        if (index >= primes.length) {
            synchronized (MathFunctions.class) {
                primes = MathFunctions.primes;
                if (index >= primes.length) {
                    int i = primes.length;
                    int n = Short.toUnsignedInt(primes[i - 1]);
                    // Compute by block of 16 values, for reducing the amount of array resize.
                    primes = Arrays.copyOf(primes, Math.min((index | 0xF) + 1, PRIMES_LENGTH_16_BITS));
                    do {
testNextNumber:         while (true) {      // Simulate a "goto" statement (usually not recommanded...)
                            final int stopAt = (int) Math.sqrt(n += 2);
                            int prime;
                            int j = 0;
                            do {
                                prime = Short.toUnsignedInt(primes[++j]);
                                if (n % prime == 0) {
                                    continue testNextNumber;
                                }
                            } while (prime <= stopAt);
                            primes[i] = (short) n;
                            break;
                        }
                    } while (++i < primes.length);
                    MathFunctions.primes = primes;
                }
            }
        }
        return Short.toUnsignedInt(primes[index]);
    }

    /**
     * Returns the first prime number equals or greater than the given value.
     * Current implementation accepts only values in the
     * [2 … {@value #HIGHEST_SUPPORTED_PRIME_NUMBER}] range.
     *
     * @param  number  the number for which to find the next prime.
     * @return the given number if it is a prime number, or the next prime number otherwise.
     * @throws IllegalArgumentException if the given value is outside the supported range.
     *
     * @see java.math.BigInteger#isProbablePrime(int)
     */
    public static int nextPrimeNumber(final int number) throws IllegalArgumentException {
        if (number < 2 || number > HIGHEST_SUPPORTED_PRIME_NUMBER) {
            throw new IllegalArgumentException(String.valueOf(number));
        }
        final short[] primes = MathFunctions.primes;
        int lower = 0;
        int upper = Math.min(PRIMES_LENGTH_15_BITS, primes.length);
        if (number > Short.MAX_VALUE) {
            lower = upper;
            upper = primes.length;
        }
        int i = Arrays.binarySearch(primes, lower, upper, (short) number);
        if (i < 0) {
            i = ~i;
            if (i >= primes.length) {
                int p;
                do p = primeNumberAt(i++);
                while (p < number);
                return p;
            }
        }
        return Short.toUnsignedInt(primes[i]);
    }

    /**
     * Computes 10 raised to the power of <var>x</var>.
     *
     * @param  x  the exponent.
     * @return 10 raised to the given exponent.
     */
    static double pow10(int x) {
        x -= EXPONENT_FOR_ZERO + 1;
        return (x >= 0) ? (x < POW10.length ? POW10[x] : Double.POSITIVE_INFINITY) : 0;
    }

    /**
     * Converts a power of 2 to a power of 10, rounded toward negative infinity.
     * This method is equivalent to the following code, but using only integer arithmetic:
     *
     * <pre>{@code return (int) Math.floor(exp2 * LOG10_2)}</pre>
     *
     * This method is valid only for arguments in the [-2620 … 2620] range, which is more than enough
     * for the range of {@code double} exponents. We do not put this method in public API because it
     * does not check the argument validity.
     *
     * <div class="section">Arithmetic notes</div>
     * {@code toExp10(getExponent(10ⁿ))} returns <var>n</var> only for {@code n == 0}, and <var>n</var>-1 in all other
     * cases. This is because 10ⁿ == m × 2<sup>exp2</sup> where the <var>m</var> significand is always greater than 1,
     * which must be compensated by a smaller {@code exp2} value such as {@code toExp10(exp2) < n}. Note that if the
     * {@code getExponent(…)} argument is not a power of 10, then the result can be either <var>n</var> or <var>n</var>-1.
     *
     * @param  exp2  the power of 2 to convert Must be in the [-2620 … 2620] range.
     * @return the power of 10, rounded toward negative infinity.
     */
    static int toExp10(final int exp2) {
        /*
         * Compute:
         *          exp2 × (log10(2) × 2ⁿ) / 2ⁿ
         * where:
         *          n = 20   (arbitrary value)
         *
         * log10(2) × 2ⁿ  =  315652.82873335475, which we round to 315653.
         *
         * The range of valid values for such approximation is determined
         * empirically by running the NumericsTest.testToExp10() method.
         */
        assert exp2 >= -2620 && exp2 <= 2620 : exp2;
        return (exp2 * 315653) >> 20;
    }

    /**
     * Returns the significand <var>m</var> of the given value such as {@code value = m×2ⁿ} where
     * <var>n</var> is {@link Math#getExponent(float)} - {@value #SIGNIFICAND_SIZE_OF_FLOAT}.
     * For any non-NaN positive values (including infinity), the following relationship holds:
     *
     * <pre>{@code assert Math.scalb(getSignificand(value), Math.getExponent(value) - SIGNIFICAND_SIZE_OF_FLOAT) == value;}</pre>
     *
     * For negative values, this method behaves as if the value was positive.
     *
     * @param  value  the value for which to get the significand.
     * @return the significand of the given value.
     */
    private static int getSignificand(final float value) {
        int bits = Float.floatToRawIntBits(value);
        final int exponent = bits & (0xFF << SIGNIFICAND_SIZE_OF_FLOAT);
        bits &= (1L << SIGNIFICAND_SIZE_OF_FLOAT) - 1;
        if (exponent != 0) {
            bits |= (1L << SIGNIFICAND_SIZE_OF_FLOAT);
        } else {
            bits <<= 1;
        }
        return bits;
    }

    /**
     * Converts the given {@code float} value to a {@code double} with the extra <em>decimal</em> fraction digits
     * set to zero. This is different than the standard cast in the Java language, which set the extra <em>binary</em>
     * fraction digits to zero.
     * For example {@code (double) 0.1f} gives 0.10000000149011612 while {@code floatToDouble(0.1f)} returns 0.1.
     *
     * <div class="note"><b>Note:</b>
     * This method is <strong>not</strong> more accurate than the standard Java cast – it should be used only when
     * the base 10 representation of the given value may be of special interest. If the value come from a call to
     * {@link Float#parseFloat(String)} (directly or indirectly), and if that call can not be replaced by a call to
     * {@link Double#parseDouble(String)} (for example because the original {@code String} is not available anymore),
     * then this method may be useful if one consider the {@code String} representation in base 10 as definitive.
     * But if the value come from an instrument measurement or a calculation, then there is probably no reason to use
     * this method because base 10 is not more "real" than base 2 or any other base for natural phenomenon.</div>
     *
     * This method is equivalent to the following code, except that it is potentially faster since the
     * actual implementation avoid to format and parse the value:
     *
     * <pre>{@code return Double.parseDouble(Float.toString(value));}</pre>
     *
     * @param  value  the {@code float} value to convert as a {@code double}.
     * @return the given value as a {@code double} with the extra decimal fraction digits set to zero.
     */
    public static double floatToDouble(final float value) {
        /*
         * Decompose  value == m × 2^e  where m and e are integers. If the exponent is not negative, then
         * there is no fractional part in the value, in which case there is no rounding error to fix.
         * (Note: NaN and infinities also have exponent greater than zero).
         */
        final int e = Math.getExponent(value) - SIGNIFICAND_SIZE_OF_FLOAT;
        if (e >= 0) {
            return value;                               // Integer, infinity or NaN.
        }
        final int m = getSignificand(value);
        assert Math.scalb((float) m, e) == Math.abs(value) : value;
        /*
         * Get the factor c for converting the significand m from base 2 to base 10, such as:
         *
         *    m × (2 ^ e)  ==  m × c × (10 ^ -e₁₀)
         *
         * where e₁₀ is the smallest exponent which allow to represent the value without precision lost when (m × c)
         * is rounded to an integer. Because the number of significant digits in base 2 does not correspond to an
         * integer number of significand digits in base 10, we have slightly more precision than what the 'float'
         * value had: we have something between 0 and 1 extraneous digits.
         *
         * Note: the conversation factor c is also equals to 1 ULP converted to the units of (m × c).
         */
        final int    e10 = -toExp10(e);                         // Range: [0 … 45] inclusive.
        final double c   = Math.scalb(pow10(e10), e);           // Range: (1 … 10) exclusive.
        final double mc  = m * c;                               // Only integer part is meaningful.
        /*
         * First, presume that our representation in base 10 has one extranous digit, so we will round
         * to the tens instead of unities. If the difference appears to not be smaller than half a ULP,
         * then the last digit was not extranous - we need to keep it.
         */
        double r = Math.rint(mc / 10) * 10;
        if (Math.abs(r - mc) >= c/2) {
            r = Math.rint(mc);
        }
        r = Math.copySign(Math.scalb(r / c, e), value);
        assert value == (float) r : value;
        return r;
    }

    /**
     * Returns {@code true} if the given doubles are equal.
     * Positive and negative zeros are considered different.
     * NaN values are considered equal to all other NaN values.
     *
     * @param  v1  the first value to compare.
     * @param  v2  the second value to compare.
     * @return {@code true} if both values are equal.
     *
     * @see Double#equals(Object)
     */
    static boolean equals(final double v1, final double v2) {
        return Double.doubleToLongBits(v1) == Double.doubleToLongBits(v2);
    }

    /**
     * Returns {@code true} if the given floating point numbers are considered equal.
     * The tolerance factor used in this method is arbitrary and may change in any future version.
     */
    static boolean epsilonEquals(final double expected, final double actual) {
        return Math.abs(expected - actual) <= Math.scalb(Math.ulp(expected), 4);
    }
}
