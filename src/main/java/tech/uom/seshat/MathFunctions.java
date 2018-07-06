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
 *
 * @author  Martin Desruisseaux (MPO, IRD, Geomatys)
 * @author  Johann Sorel (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class MathFunctions {
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
}
