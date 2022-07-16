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

import java.util.OptionalInt;
import javax.measure.Unit;
import javax.measure.Quantity;
import javax.measure.quantity.*;
import javax.measure.quantity.Angle;
import javax.measure.IncommensurableException;
import org.junit.Test;

import static tech.uom.seshat.SexagesimalConverter.*;
import static tech.uom.seshat.Units.*;
import static org.junit.Assert.*;


/**
 * Test conversions using the units declared in {@link Units}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.2
 * @since   1.0
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
        assertFalse(isTemporal(DMS));
        assertFalse(isTemporal(DMS_SCALED));
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
        assertFalse(isLinear(DMS));
        assertFalse(isLinear(DMS_SCALED));
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
        assertTrue (isAngular(DMS));
        assertTrue (isAngular(DMS_SCALED));
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
        assertFalse(isScale(DMS));
        assertFalse(isScale(DMS_SCALED));
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
        assertEquals(1000.0,               toStandardUnit(KILOMETRE), 1E-15);
        assertEquals(0.017453292519943295, toStandardUnit(DEGREE),    1E-15);
        assertEquals(0.01,                 toStandardUnit(GAL),       1E-15);
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
        verifyGetFromQuantity(Length.class,            METRE);
        verifyGetFromQuantity(Mass.class,              KILOGRAM);
        verifyGetFromQuantity(Time.class,              SECOND);
        verifyGetFromQuantity(Temperature.class,       KELVIN);
        verifyGetFromQuantity(Area.class,              SQUARE_METRE);
        verifyGetFromQuantity(Volume.class,            CUBIC_METRE);
        verifyGetFromQuantity(Speed.class,             METRES_PER_SECOND);
        verifyGetFromQuantity(LuminousIntensity.class, CANDELA);
        verifyGetFromQuantity(LuminousFlux.class,      LUMEN);
        verifyGetFromQuantity(SolidAngle.class,        STERADIAN);
        verifyGetFromQuantity(Angle.class,             RADIAN);
        verifyGetFromQuantity(Dimensionless.class,     UNITY);
        verifyGetFromQuantity(Acceleration.class,      METRES_PER_SECOND_SQUARED);
    }

    /**
     * Tests getting a unit for a given dimension.
     */
    @Test
    public void testGetForDimension() {
        verifyGetFromDimension(Length.class,            METRE,                     METRE);
        verifyGetFromDimension(Mass.class,              KILOGRAM,                  KILOGRAM);
        verifyGetFromDimension(Time.class,              SECOND,                    SECOND);
        verifyGetFromDimension(Temperature.class,       KELVIN,                    KELVIN);
        verifyGetFromDimension(Area.class,              SQUARE_METRE,              SQUARE_METRE);
        verifyGetFromDimension(Volume.class,            CUBIC_METRE,               CUBIC_METRE);
        verifyGetFromDimension(Speed.class,             METRES_PER_SECOND,         METRES_PER_SECOND);
        verifyGetFromDimension(LuminousIntensity.class, CANDELA,                   CANDELA);
        verifyGetFromDimension(LuminousFlux.class,      CANDELA,                   LUMEN);      // Because lumen is candela divided by a dimensionless unit.
        verifyGetFromDimension(SolidAngle.class,        UNITY,                     STERADIAN);
        verifyGetFromDimension(Angle.class,             UNITY,                     RADIAN);
        verifyGetFromDimension(Dimensionless.class,     UNITY,                     UNITY);
        verifyGetFromDimension(Acceleration.class,      METRES_PER_SECOND_SQUARED, METRES_PER_SECOND_SQUARED);
    }

    /**
     * For a given {@code test} quantity class, verifies that {@link Units#get(Class)} gives the expected value.
     */
    private static <Q extends Quantity<Q>> void verifyGetFromQuantity(final Class<Q> test, final Unit<Q> expected) {
        assertSame(test.getSimpleName(), expected, Units.get(test));
    }

    /**
     * For a given {@code test} dimension, verifies that {@link Units#get(Dimension)} gives the expected value.
     */
    private static <Q extends Quantity<Q>> void verifyGetFromDimension(final Class<Q> label, final Unit<?> expected, final Unit<Q> test) {
        assertSame(label.getSimpleName(), expected, Units.get(test.getDimension()));
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
        assertSame(DEGREE,       valueOf("degree_north"));
        assertSame(DEGREE,       valueOf("degrees_N"));
        assertSame(DEGREE,       valueOf("degree_N"));
        assertSame(DEGREE,       valueOf("degreesN"));
        assertSame(DEGREE,       valueOf("degreeN"));
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
        assertSame(GAL,          valueOf("gal"));
        assertSame(GAL,          valueOf("cm/s²"));
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

    /**
     * Tests {@link Units#valueOfEPSG(int)} and {@link Units#valueOf(String)} with a {@code "EPSG:####"} syntax.
     */
    @Test
    public void testValueOfEPSG() {
        assertSame(METRE,          valueOfEPSG(9001));
        assertSame(DEGREE,         valueOfEPSG(9102));      // Used in prime meridian and operation parameters.
        assertSame(DEGREE,         valueOfEPSG(9122));      // Used in coordinate system axes.
        assertSame(TROPICAL_YEAR,  valueOfEPSG(1029));
        assertSame(SECOND,         valueOfEPSG(1040));
        assertSame(FOOT,           valueOfEPSG(9002));
        assertSame(US_SURVEY_FOOT, valueOfEPSG(9003));
        assertSame(NAUTICAL_MILE,  valueOfEPSG(9030));
        assertSame(KILOMETRE,      valueOfEPSG(9036));
        assertSame(RADIAN,         valueOfEPSG(9101));
        assertSame(ARC_MINUTE,     valueOfEPSG(9103));
        assertSame(ARC_SECOND,     valueOfEPSG(9104));
        assertSame(GRAD,           valueOfEPSG(9105));
        assertSame(MICRORADIAN,    valueOfEPSG(9109));
        assertSame(DMS_SCALED,     valueOfEPSG(9107));
        assertSame(DMS_SCALED,     valueOfEPSG(9108));
        assertSame(DMS,            valueOfEPSG(9110));
        assertSame(DM,             valueOfEPSG(9111));
        assertSame(UNITY,          valueOfEPSG(9203));
        assertSame(UNITY,          valueOfEPSG(9201));
        assertSame(PPM,            valueOfEPSG(9202));
    }

    /**
     * Tests {@link Units#getEpsgCode(Unit)}.
     */
    @Test
    public void testGetEpsgCode() {
        assertEquals(OptionalInt.of(9001), getEpsgCode(METRE));
        assertEquals(OptionalInt.of(9102), getEpsgCode(DEGREE));
        assertEquals(OptionalInt.of(9110), getEpsgCode(DMS));
        assertEquals(OptionalInt.of(9110), getEpsgCode(DMS));
        assertEquals(OptionalInt.of(9107), getEpsgCode(DMS_SCALED));
        assertEquals(OptionalInt.of(9111), getEpsgCode(DM));
        assertEquals(OptionalInt.of(1029), getEpsgCode(TROPICAL_YEAR));
        assertEquals(OptionalInt.of(1040), getEpsgCode(SECOND));
        assertEquals(OptionalInt.of(9002), getEpsgCode(FOOT));
        assertEquals(OptionalInt.of(9003), getEpsgCode(US_SURVEY_FOOT));
        assertEquals(OptionalInt.of(9030), getEpsgCode(NAUTICAL_MILE));
        assertEquals(OptionalInt.of(9036), getEpsgCode(KILOMETRE));
        assertEquals(OptionalInt.of(9101), getEpsgCode(RADIAN));
        assertEquals(OptionalInt.of(9103), getEpsgCode(ARC_MINUTE));
        assertEquals(OptionalInt.of(9104), getEpsgCode(ARC_SECOND));
        assertEquals(OptionalInt.of(9105), getEpsgCode(GRAD));
        assertEquals(OptionalInt.of(9109), getEpsgCode(MICRORADIAN));
        assertEquals(OptionalInt.of(9201), getEpsgCode(UNITY));
        assertEquals(OptionalInt.of(9202), getEpsgCode(PPM));
    }
}
