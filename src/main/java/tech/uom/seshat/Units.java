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

import javax.measure.Unit;
import javax.measure.quantity.*;


/**
 * Provides constants for various Units of Measurement together with static methods working on {@link Unit} instances.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Units {
    static final Unit<Mass> GRAM = null;

    static final Unit<Mass> KILOGRAM = null;

    /**
     * Sets to {@code true} by the static initializer after the initialization has been completed.
     * This is a safety against unexpected changes in the {@link UnitRegistry#HARD_CODED} map.
     *
     * <p>We use here a "lazy final initialization" pattern. We rely on the fact that this field is
     * initialized to {@code true} only at the end of the following static initializer. All methods
     * invoked in the static initializer will see the default value, which is {@code false}, until
     * the initializer fully completed. While apparently dangerous, this behavior is actually documented
     * in <a href="http://docs.oracle.com/javase/specs/jls/se8/html/jls-12.html#jls-12.4.1">section 12.4.1
     * of Java language specification</a>:</p>
     *
     * <blockquote>The fact that initialization code is unrestricted allows examples to be constructed where
     * the value of a class variable can be observed when it still has its initial default value, before its
     * initializing expression is evaluated, but such examples are rare in practice. (…snip…) The full power
     * of the Java programming language is available in these initializers; programmers must exercise some care.
     * This power places an extra burden on code generators, but this burden would arise in any case because
     * the Java programming language is concurrent.</blockquote>
     */
    static final boolean initialized;
    static {
        // TODO

        initialized = true;
    }

    /**
     * Do not allows instantiation of this class.
     */
    private Units() {
    }

    /**
     * Returns the system unit for the given symbol, or {@code null} if none.
     * This method does not perform any parsing (prefix, exponents, <i>etc</i>).
     * It is only for getting one of the pre-defined constants, for example after deserialization.
     *
     * <p><b>Implementation note:</b> this method must be defined in this {@code Units} class
     * in order to force a class initialization before use.</p>
     *
     * @see Prefixes#getUnit(String)
     */
    @SuppressWarnings("unchecked")
    static Unit<?> get(final String symbol) {
        return null;    // TODO
    }
}
