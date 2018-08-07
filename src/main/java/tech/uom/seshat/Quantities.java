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
package tech.uom.seshat;

import javax.measure.Unit;
import javax.measure.Quantity;
import javax.measure.quantity.Time;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;


/**
 * Provides static methods working on {@link Quantity} instances.
 * Seshat implementation of quantities has the following characteristics:
 *
 * <ul>
 *   <li>Values are stored with {@code double} precision.</li>
 *   <li>All quantities implement the specific subtype (e.g. {@link Length} instead of {@code Quantity<Length>}).</li>
 *   <li>Quantities are immutable, {@link Comparable} and {@link java.io.Serializable}.</li>
 * </ul>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 */
final class Quantities {
    /**
     * Do not allow instantiation of this class.
     */
    private Quantities() {
    }

    /**
     * Creates a quantity for the given value and unit of measurement.
     *
     * @param  <Q>    the quantity type (e.g. {@link Length}, {@link Angle}, {@link Time}, <i>etc.</i>).
     * @param  value  the quantity magnitude.
     * @param  unit   the unit of measurement associated to the given value.
     * @return a quantity of the given type for the given value and unit of measurement.
     * @throws IllegalArgumentException if the given unit class is not a supported implementation.
     *
     * @see UnitServices#getQuantityFactory(Class)
     */
    public static <Q extends Quantity<Q>> Q create(final double value, final Unit<Q> unit) {
        return null;    // TODO
    }
}
