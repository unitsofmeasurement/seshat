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
 * Implementation of Units of Measurement API defined by JSR 363.
 * The {@link tech.uom.seshat.Units} class provides static constants
 * for about 50 units of measurement including all the SI base units
 * ({@linkplain tech.uom.seshat.Units#METRE    metre},
 *  {@linkplain tech.uom.seshat.Units#KILOGRAM kilogram}
 *  {@linkplain tech.uom.seshat.Units#SECOND   second},
 *  {@linkplain tech.uom.seshat.Units#AMPERE   ampere},
 *  {@linkplain tech.uom.seshat.Units#KELVIN   kelvin},
 *  {@linkplain tech.uom.seshat.Units#MOLE     mole} and
 *  {@linkplain tech.uom.seshat.Units#CANDELA  candela})
 * together with some derived units
 * ({@linkplain tech.uom.seshat.Units#SQUARE_METRE      square metre},
 *  {@linkplain tech.uom.seshat.Units#CUBIC_METRE       cubic metre},
 *  {@linkplain tech.uom.seshat.Units#METRES_PER_SECOND metres per second},
 *  {@linkplain tech.uom.seshat.Units#HERTZ             hertz},
 *  {@linkplain tech.uom.seshat.Units#PASCAL            pascal},
 *  {@linkplain tech.uom.seshat.Units#NEWTON            newton},
 *  {@linkplain tech.uom.seshat.Units#JOULE             joule},
 *  {@linkplain tech.uom.seshat.Units#WATT              watt},
 *  {@linkplain tech.uom.seshat.Units#TESLA             tesla},
 *  <i>etc.</i>)
 * and some dimensionless units
 * ({@linkplain tech.uom.seshat.Units#RADIAN    radian},
 *  {@linkplain tech.uom.seshat.Units#STERADIAN steradian},
 *  {@linkplain tech.uom.seshat.Units#PIXEL     pixel},
 *  {@linkplain tech.uom.seshat.Units#UNITY     unity}).
 *
 * In relation to units of measurement, this package also defines:
 *
 * <ul>
 *   <li>{@linkplain tech.uom.seshat.Quantities}
 *       as a {@code double} value value associated to a {@code Unit} instance.</li>
 *   <li>Parsers and formatters
 *      ({@link tech.uom.seshat.UnitFormat}).</li>
 * </ul>
 *
 * <div class="section">Arithmetic operations</div>
 * <p>Seshat supports arithmetic operations on units and on quantities.
 * The unit (including SI prefix) and the quantity type resulting from
 * those arithmetic operations are automatically inferred.
 * For example this line of code:</p>
 *
 * <blockquote><pre>{@code System.out.println( Units.PASCAL.multiply(1000) );}</pre></blockquote>
 *
 * <p>prints <cite>"kPa"</cite>, i.e. the kilo prefix has been automatically applied
 * (SI prefixes are applied on SI units only, not on other systems).
 * Other example:</p>
 *
 * <blockquote><pre>{@code Force  f = Quantities.create(4, Units.NEWTON);
 *Length d = Quantities.create(6, Units.MILLIMETRE);
 *Time   t = Quantities.create(3, Units.SECOND);
 *Quantity<?> e = f.multiply(d).divide(t);
 *System.out.println(e);
 *System.out.println("Is instance of Power: " + (e instanceof Power));}</pre></blockquote>
 *
 * <p>prints {@code "8 mW"} and {@code "Is instance of Power: true"},
 * i.e. Seshat detects that the result of N⋅m∕s is Watt,
 * inherits the milli prefix from millimetre and creates an instance
 * of {@link javax.measure.quantity.Power}, not just {@code Quantity<Power>} (the generic parent).</p>
 *
 * <div class="section">Parsing and formatting</div>
 * <p>{@linkplain tech.uom.seshat.Units#valueOf(String) Parsing} and formatting use Unicode symbols by default, as in "µg/m²".
 * Parenthesis are recognized at parsing time and used for denominators at formatting time, as in "kg/(m²⋅s)".
 * While uncommon, Seshat accepts fractional powers as in "m^⅔".
 * Some sentences like <cite>"100 feet"</cite>, <cite>"square metre"</cite> and <cite>"degree Kelvin"</cite>
 * are also recognized at parsing time.</p>
 *
 * <div class="section">Source</div>
 * <p>Seshat is a subset of <a href="http://sis.apache.org/">Apache Spatial Information System (SIS)</a>
 * library keeping only the classes required for JSR 363, and with geospatial-specific functionalities omitted.
 * The omitted functionalities are {@code Salinity}, {@code AngularVelocity} and {@code ScaleRateOfChange}
 * quantities, sigma-level units, and sexagesimal units.</p>
 *
 * @author  Martin Desruisseaux (MPO, IRD, Geomatys)
 * @version 1.0
 * @since   1.0
 */
package tech.uom.seshat;
