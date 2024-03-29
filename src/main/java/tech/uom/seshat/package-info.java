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

/**
 * Implementation of Units of Measurement API defined by JSR 385.
 * This package provides:
 *
 * <ul>
 *   <li>The {@link tech.uom.seshat.Units} class with static constants for about 50 units of measurement including:
 *     <ul>
 *       <li>all the SI base units
 * ({@linkplain tech.uom.seshat.Units#METRE    metre},
 *  {@linkplain tech.uom.seshat.Units#KILOGRAM kilogram}
 *  {@linkplain tech.uom.seshat.Units#SECOND   second},
 *  {@linkplain tech.uom.seshat.Units#AMPERE   ampere},
 *  {@linkplain tech.uom.seshat.Units#KELVIN   kelvin},
 *  {@linkplain tech.uom.seshat.Units#MOLE     mole} and
 *  {@linkplain tech.uom.seshat.Units#CANDELA  candela}),
 *       </li><li>some derived units
 * ({@linkplain tech.uom.seshat.Units#SQUARE_METRE      square metre},
 *  {@linkplain tech.uom.seshat.Units#CUBIC_METRE       cubic metre},
 *  {@linkplain tech.uom.seshat.Units#METRES_PER_SECOND metres per second},
 *  {@linkplain tech.uom.seshat.Units#HERTZ             hertz},
 *  {@linkplain tech.uom.seshat.Units#PASCAL            pascal},
 *  {@linkplain tech.uom.seshat.Units#NEWTON            newton},
 *  {@linkplain tech.uom.seshat.Units#JOULE             joule},
 *  {@linkplain tech.uom.seshat.Units#WATT              watt},
 *  {@linkplain tech.uom.seshat.Units#TESLA             tesla},
 *  <i>etc.</i>),
 *       </li><li>and some dimensionless units
 * ({@linkplain tech.uom.seshat.Units#RADIAN    radian},
 *  {@linkplain tech.uom.seshat.Units#STERADIAN steradian},
 *  {@linkplain tech.uom.seshat.Units#PIXEL     pixel},
 *  {@linkplain tech.uom.seshat.Units#UNITY     unity}).
 *       </li>
 *     </ul>
 *   </li>
 *   <li>{@linkplain tech.uom.seshat.Quantities}
 *       as a {@code double} value value associated to a {@code Unit} instance.</li>
 *   <li>Parsers and formatters
 *      ({@link tech.uom.seshat.UnitFormat} and {@link tech.uom.seshat.QuantityFormat}).</li>
 * </ul>
 *
 * Seshat supports arithmetic operations on units and on quantities.
 * The unit (including SI prefix) and the quantity type resulting from
 * those arithmetic operations are automatically inferred.
 * See the module summary page for code examples.
 *
 * @author  Martin Desruisseaux (MPO, IRD, Geomatys)
 * @version 1.3
 * @since   1.0
 */
package tech.uom.seshat;
