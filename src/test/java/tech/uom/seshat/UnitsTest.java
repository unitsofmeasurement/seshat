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
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.LuminousFlux;
import javax.measure.quantity.LuminousIntensity;
import javax.measure.quantity.Mass;
import javax.measure.quantity.SolidAngle;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;
import javax.measure.quantity.Volume;
import javax.measure.IncommensurableException;
import org.junit.Test;

import static tech.uom.seshat.Units.*;
import static org.junit.Assert.*;


/**
 * Test conversions using the units declared in {@link Units}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 */
public final strictfp class UnitsTest {
    private static final double STRICT = 0;

    /**
     * Verifies that the {@link Units#initialized} flag has been set.
     */
    @Test
    public void testInitialized() {
        assertTrue(Units.initialized);
    }

    /**
     * Tests {@link Units#isTemporal(Unit)}.
     */
    @Test
    public void testIsTemporal() {
        // Standard units
        assertFalse(isTemporal(null));
        assertFalse(isTemporal(UNITY));
        assertFalse(isTemporal(METRE));
        assertFalse(isTemporal(RADIAN));
        assertFalse(isTemporal(DEGREE));
        assertFalse(isTemporal(ARC_MINUTE));
        assertFalse(isTemporal(ARC_SECOND));
        assertFalse(isTemporal(GRAD));
        assertTrue (isTemporal(DAY));
        assertFalse(isTemporal(NAUTICAL_MILE));

        // Additional units
        assertFalse(isTemporal(PPM));
        assertTrue (isTemporal(MILLISECOND));
    }

    /**
     * Tests {@link Units#isLinear(Unit)}.
     */
    @Test
    public void testIsLinear() {
        // Standard units
        assertFalse(isLinear(null));
        assertFalse(isLinear(UNITY));
        assertTrue (isLinear(METRE));
        assertFalse(isLinear(RADIAN));
        assertFalse(isLinear(DEGREE));
        assertFalse(isLinear(ARC_MINUTE));
        assertFalse(isLinear(ARC_SECOND));
        assertFalse(isLinear(GRAD));
        assertFalse(isLinear(DAY));
        assertTrue (isLinear(NAUTICAL_MILE));

        // Additional units
        assertFalse(isLinear(PPM));
        assertFalse(isLinear(MILLISECOND));
    }

    /**
     * Tests {@link Units#isAngular(Unit)}.
     */
    @Test
    public void testIsAngular() {
        // Standard units
        assertFalse(isAngular(null));
        assertFalse(isAngular(UNITY));
        assertFalse(isAngular(METRE));
        assertTrue (isAngular(RADIAN));
        assertTrue (isAngular(DEGREE));
        assertTrue (isAngular(ARC_MINUTE));
        assertTrue (isAngular(ARC_SECOND));
        assertTrue (isAngular(GRAD));
        assertFalse(isAngular(DAY));
        assertFalse(isAngular(NAUTICAL_MILE));

        // Additional units
        assertFalse(isAngular(PPM));
        assertFalse(isAngular(MILLISECOND));
    }

    /**
     * Tests {@link Units#isScale(Unit)}.
     */
    @Test
    public void testIsScale() {
        // Standard units
        assertFalse(isScale(null));
        assertTrue (isScale(UNITY));
        assertFalse(isScale(METRE));
        assertFalse(isScale(RADIAN));
        assertFalse(isScale(DEGREE));
        assertFalse(isScale(ARC_MINUTE));
        assertFalse(isScale(ARC_SECOND));
        assertFalse(isScale(GRAD));
        assertFalse(isScale(DAY));
        assertFalse(isScale(NAUTICAL_MILE));

        // Additional units
        assertTrue (isScale(PPM));
        assertFalse(isScale(MILLISECOND));
    }

    /**
     * Tests {@link Units#isPressure(Unit)}.
     */
    @Test
    public void testIsPressure() {
        assertFalse(isPressure(null));
        assertFalse(isPressure(METRE));
    }

    /**
     * Tests {@link Units#toStandardUnit(Unit)}.
     */
    @Test
    public void testToStandardUnit() {
        assertEquals(1000.0,               toStandardUnit(KILOMETRE),    1E-15);
        assertEquals(0.017453292519943295, toStandardUnit(DEGREE), 1E-15);
    }

    /**
     * Verifies some conversion factors.
     */
    @Test
    public void testConversionFactors() {
        assertEquals(1000, KILOMETRE        .getConverterTo(METRE)              .convert(1), STRICT);
        assertEquals( 3.6, METRES_PER_SECOND.getConverterTo(KILOMETRES_PER_HOUR).convert(1), STRICT);
    }

    /**
     * Tests the conversion factor of {@link Units#DECIBEL}.
     *
     * @throws IncommensurableException if the conversion can not be applied.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Decibel#Conversions">Decibel on Wikipedia</a>
     */
    @Test
    public void testDecibelConversionFactor() throws IncommensurableException {
        final Unit<?> bel = Units.valueOf("B");
        assertEquals(10,      bel.getConverterToAny(DECIBEL).convert(1), STRICT);
        assertEquals(0.1,     DECIBEL.getConverterToAny(bel).convert(1), STRICT);
        assertEquals(3.16228,     bel.getConverterToAny(UNITY).convert(1), 5E-6);
        assertEquals(1.12202, DECIBEL.getConverterToAny(UNITY).convert(1), 5E-6);
        /*
         * Reverse of last two lines above.
         */
        assertEquals(1, UNITY.getConverterToAny(bel)    .convert(3.16228), 2E-5);
        assertEquals(1, UNITY.getConverterToAny(DECIBEL).convert(1.12202), 2E-5);
    }

    /**
     * Tests getting a unit for a given quantity type.
     */
    @Test
    public void testGetForQuantity() {
        assertSame("Length",            Units.METRE,             Units.get(Length.class));
        assertSame("Mass",              Units.KILOGRAM,          Units.get(Mass.class));
        assertSame("Time",              Units.SECOND,            Units.get(Time.class));
        assertSame("Temperature",       Units.KELVIN,            Units.get(Temperature.class));
        assertSame("Area",              Units.SQUARE_METRE,      Units.get(Area.class));
        assertSame("Volume",            Units.CUBIC_METRE,       Units.get(Volume.class));
        assertSame("Speed",             Units.METRES_PER_SECOND, Units.get(Speed.class));
        assertSame("LuminousIntensity", Units.CANDELA,           Units.get(LuminousIntensity.class));
        assertSame("LuminousFlux",      Units.LUMEN,             Units.get(LuminousFlux.class));
        assertSame("SolidAngle",        Units.STERADIAN,         Units.get(SolidAngle.class));
        assertSame("Angle",             Units.RADIAN,            Units.get(Angle.class));
        assertSame("Dimensionless",     Units.UNITY,             Units.get(Dimensionless.class));
    }

    /**
     * Tests getting a unit for a given dimension.
     */
    @Test
    public void testGetForDimension() {
        assertSame("Length",            Units.METRE,             Units.get(Units.METRE            .getDimension()));
        assertSame("Mass",              Units.KILOGRAM,          Units.get(Units.KILOGRAM         .getDimension()));
        assertSame("Time",              Units.SECOND,            Units.get(Units.SECOND           .getDimension()));
        assertSame("Temperature",       Units.KELVIN,            Units.get(Units.KELVIN           .getDimension()));
        assertSame("Area",              Units.SQUARE_METRE,      Units.get(Units.SQUARE_METRE     .getDimension()));
        assertSame("Volume",            Units.CUBIC_METRE,       Units.get(Units.CUBIC_METRE      .getDimension()));
        assertSame("Speed",             Units.METRES_PER_SECOND, Units.get(Units.METRES_PER_SECOND.getDimension()));
        assertSame("LuminousIntensity", Units.CANDELA,           Units.get(Units.CANDELA          .getDimension()));
        assertSame("LuminousFlux",      Units.CANDELA,           Units.get(Units.LUMEN            .getDimension()));    // Because lumen is candela divided by a dimensionless unit.
        assertSame("SolidAngle",        Units.UNITY,             Units.get(Units.STERADIAN        .getDimension()));
        assertSame("Angle",             Units.UNITY,             Units.get(Units.RADIAN           .getDimension()));
        assertSame("Dimensionless",     Units.UNITY,             Units.get(Units.UNITY            .getDimension()));
    }

    /**
     * Tests {@link Units#valueOf(String)} with units most commonly found in geospatial data.
     */
    @Test
    public void testValueOf() {
        assertSame(DEGREE,       valueOf("°"));
        assertSame(DEGREE,       valueOf("deg"));
        assertSame(DEGREE,       valueOf("degree"));
        assertSame(DEGREE,       valueOf("degrees"));
        assertSame(DEGREE,       valueOf("degrées"));
        assertSame(DEGREE,       valueOf("DEGREES"));
        assertSame(DEGREE,       valueOf("DEGRÉES"));
        assertSame(DEGREE,       valueOf("degrees_east"));
        assertSame(DEGREE,       valueOf("degrees_north"));
        assertSame(DEGREE,       valueOf("degrées_north"));
        assertSame(DEGREE,       valueOf("decimal_degree"));
        assertSame(ARC_SECOND,   valueOf("arcsec"));
        assertSame(RADIAN,       valueOf("rad"));
        assertSame(RADIAN,       valueOf("radian"));
        assertSame(RADIAN,       valueOf("radians"));
        assertSame(SECOND,       valueOf("s"));
        assertSame(SECOND,       valueOf("second"));
        assertSame(SECOND,       valueOf("seconds"));
        assertSame(MINUTE,       valueOf("min"));
        assertSame(MINUTE,       valueOf("minute"));
        assertSame(MINUTE,       valueOf("minutes"));
        assertSame(HOUR,         valueOf("h"));
        assertSame(HOUR,         valueOf("hr"));
        assertSame(HOUR,         valueOf("hour"));
        assertSame(HOUR,         valueOf("hours"));
        assertSame(DAY,          valueOf("d"));
        assertSame(DAY,          valueOf("day"));
        assertSame(DAY,          valueOf("days"));
        assertSame(METRE,        valueOf("m"));
        assertSame(METRE,        valueOf("metre"));
        assertSame(METRE,        valueOf("meter"));
        assertSame(METRE,        valueOf("metres"));
        assertSame(METRE,        valueOf("mètres"));
        assertSame(METRE,        valueOf("meters"));
        assertSame(KILOMETRE,    valueOf("km"));
        assertSame(KILOMETRE,    valueOf("kilometre"));
        assertSame(KILOMETRE,    valueOf("kilometer"));
        assertSame(KILOMETRE,    valueOf("kilometres"));
        assertSame(KILOMETRE,    valueOf("kilomètres"));
        assertSame(KILOMETRE,    valueOf("kilometers"));
        assertSame(KELVIN,       valueOf("K"));
        assertSame(KELVIN,       valueOf("degK"));
        assertSame(CELSIUS,      valueOf("Celsius"));
        assertSame(CELSIUS,      valueOf("degree Celsius"));
        assertSame(CELSIUS,      valueOf("degree_Celcius"));
        assertSame(PASCAL,       valueOf("Pa"));
        assertSame(DECIBEL,      valueOf("dB"));
    }

    /**
     * Tests {@link Units#valueOf(String)} with more advanced units.
     * Those units are found in netCDF files among others.
     */
    @Test
    public void testAdvancedValueOf() {
        assertSame  (MILLISECOND,                   valueOf("ms"));
        assertEquals(METRES_PER_SECOND,             valueOf("m/s"));
        assertEquals(METRES_PER_SECOND,             valueOf("m.s-1"));
        assertEquals(SQUARE_METRE.divide(SECOND),   valueOf("m2.s-1"));
        assertEquals(KILOGRAM.divide(SQUARE_METRE), valueOf("kg.m-2"));
        assertEquals(JOULE.divide(KILOGRAM),        valueOf("J/kg"));
        assertEquals(PASCAL.divide(SECOND),         valueOf("Pa/s"));
        assertSame  (HERTZ,                         valueOf("1/s"));
        assertSame  (HERTZ,                         valueOf("s-1"));
        assertSame  (PERCENT,                       valueOf("%"));
        assertEquals(KILOGRAM.divide(KILOGRAM),     valueOf("kg/kg"));
        assertEquals(KILOGRAM.divide(KILOGRAM),     valueOf("kg.kg-1"));
        assertSame  (PPM,                           valueOf("ppm"));            // Parts per million

        // Potential vorticity surface
        assertEquals(KELVIN.multiply(SQUARE_METRE).divide(KILOGRAM.multiply(SECOND)), valueOf("K.m2.kg-1.s-1"));
    }
}
