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
import javax.measure.spi.ServiceProvider;

/**
 * Implementation of Units of Measurement API defined by JSR 363.
 * Seshat is a subset of <a href="http://sis.apache.org/">Apache Spatial Information System (SIS)</a>
 * library keeping only the classes required for JSR 363, with geospatial-specific functionalities omitted.
 * The omitted functionalities are {@code Salinity}, {@code AngularVelocity} and {@code ScaleRateOfChange}
 * quantities, sigma-level units, sexagesimal units and EPSG codes.
 *
 * <p>Seshat supports arithmetic operations on units and on quantities.
 * The unit (including SI prefix) and the quantity type resulting from
 * those arithmetic operations are automatically inferred.
 * For example this line of code:
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
 *System.out.println("Instance of Power: " + (e instanceof Power));}</pre></blockquote>
 *
 * <p>prints <cite>"8 mW"</cite> and <cite>"Instance of Power: true"</cite>,
 * i.e. Seshat detects that the result of N⋅m∕s is Watt,
 * inherits the milli prefix from millimetre and creates an instance
 * of {@link javax.measure.quantity.Power}, not just {@code Quantity<Power>} (the generic parent).</p>
 *
 * {@linkplain tech.uom.seshat.Units#valueOf(String) Parsing} and formatting use Unicode symbols by default, as in µg/m².
 * Parenthesis are recognized at parsing time and used for denominators at formatting time, as in kg/(m²⋅s).
 * While uncommon, Seshat accepts fractional powers as in m^⅔.
 * Some sentences like <cite>"100 feet"</cite>, <cite>"square metre"</cite> and <cite>"degree Kelvin"</cite>
 * are also recognized at parsing time.
 *
 * @author  Martin Desruisseaux (MPO, IRD, Geomatys)
 * @version 1.0
 * @since   1.0
 */
module tech.uom.seshat {
    requires unit.api;          // Temporary name, to be renamed after JSR-385 is released.

    exports tech.uom.seshat;
    /*
     * Do not export tech.uom.seshat.math and tech.uom.seshat.util packages; it is not the
     * purpose of Seshat to publish additional collection implementations. Those classes are
     * public in Apache SIS if needed.
     */

    provides ServiceProvider with tech.uom.seshat.UnitServices;
}
