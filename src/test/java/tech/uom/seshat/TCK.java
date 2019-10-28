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

import java.util.Arrays;
import java.util.Objects;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Locale;
import javax.measure.Unit;
import javax.measure.Dimension;
import javax.measure.Quantity;
import javax.measure.UnitConverter;
import javax.measure.spi.SystemOfUnits;
import javax.measure.spi.ServiceProvider;
import org.reflections.Reflections;
import tec.units.tck.util.ServiceConfiguration;

import static org.junit.Assert.*;


/**
 * Provides the implementations to be tested by the TCK.
 * The TCK will discover this class using {@link java.util.ServiceLoader}.
 *
 * @version 1.0
 *
 * @see <a href="https://github.com/unitsofmeasurement/unit-tck/tree/1.0">TCK 1.0</a>
 */
public final class TCK implements ServiceConfiguration {
    /**
     * The {@link UnitRegistry} instance providing the implementations to test.
     */
    private final SystemOfUnits registry;

    /**
     * Creates a new service configuration for the TCK.
     */
    public TCK() {
        registry = ServiceProvider.current().getSystemOfUnitsService().getSystemOfUnits();
    }

    /**
     * Ensures that the implementation registered in {@link ServiceProvider} is the Seshat implementation.
     * This method should be invoked in each method, otherwise the tests may have unexpected behavior.
     */
    private void verifyImplementation() {
        assertTrue("The current ServiceProvider is not Seshat implementation.", registry instanceof UnitRegistry);
    }

    /**
     * Returns all implementation classes of {@link Dimension}.
     *
     * @return {@link Dimension} implementations to be tested.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Collection<Class> getDimensionClasses() {
        verifyImplementation();
        return Collections.singleton(UnitDimension.class);
    }

    /**
     * Returns all implementation classes of {@link Unit}.
     *
     * @return {@link Unit} implementations to be tested.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Collection<Class> getUnitClasses() {
        verifyImplementation();
        return Arrays.asList(SystemUnit.class, ConventionalUnit.class);
    }

    /**
     * Returns the {@link Quantity} implementations defined by a specific class. The returned set does
     * not include the proxy classes, but includes the handler that we use for proxy implementations.
     *
     * @return a set with all implemented quantity classes.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Collection<Class> getQuantityClasses() {
        verifyImplementation();
        final Set<Class> classes = new HashSet<>();
        for (final Class<?> c : Scalar.class.getDeclaredClasses()) {
            if (Quantity.class.isAssignableFrom(c)) {
                assertTrue(classes.add(c));
            }
        }
        assertFalse(classes.isEmpty());
        assertTrue(classes.add(ScalarFallback.class));
        return classes;
    }

    /**
     * Returns the {@link Quantity} type for which Seshat defines a unit of measurement.
     * This method get the list of all interfaces in the {@link javax.measure} package,
     * then remove the quantities for which Seshat does not define any units.
     *
     * @return quantity types to be checked.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Collection<Class<? extends Quantity>> getSupportedQuantityTypes() {
        verifyImplementation();
        final Set<Class<? extends Quantity>> subTypes = new Reflections("javax.measure").getSubTypesOf(Quantity.class);
        assertTrue(subTypes.remove(javax.measure.quantity.Acceleration.class));
        assertTrue(subTypes.remove(javax.measure.quantity.CatalyticActivity.class));
        assertTrue(subTypes.remove(javax.measure.quantity.RadiationDoseAbsorbed.class));
        assertTrue(subTypes.remove(javax.measure.quantity.RadiationDoseEffective.class));
        assertTrue(subTypes.remove(javax.measure.quantity.Radioactivity.class));
        assertFalse(subTypes.isEmpty());
        return subTypes;
    }

    /**
     * Returns all 7 SI base dimensions.
     *
     * @return the list of base dimensions to be checked.
     */
    @Override
    public Collection<Dimension> getBaseDimensions() {
        verifyImplementation();
        final Unit<?>[] bases = {
            Units.METRE,
            Units.KILOGRAM,
            Units.SECOND,
            Units.AMPERE,
            Units.KELVIN,
            Units.MOLE,
            Units.LUMEN
        };
        final Dimension[] dimensions = new Dimension[bases.length];
        for (int i=0; i<bases.length; i++) {
            dimensions[i] = bases[i].getDimension();
        }
        return Arrays.asList(dimensions);
    }

    /**
     * Returns a matching unit for the specified quantity type.
     *
     * @param  <Q> the type of quantity.
     * @param  quantityType the quantity type.
     * @return the unit for the specified quantity type.
     */
    @Override
    public <Q extends Quantity<Q>> Unit<Q> getUnit4Type(final Class<Q> quantityType) {
        verifyImplementation();
        return registry.getUnit(quantityType);
    }

    /**
     * Returns all {@link Unit} instances hard-coded by Seshat.
     *
     * @return the units to be tested.
     */
    @Override
    public Collection<? extends Unit<?>> getUnits4Test() {
        verifyImplementation();
        return registry.getUnits();
    }

    /**
     * Returns {@link UnitConverter} instances to be tested for requirements and recommendations.
     * This method returns all converters to system units, together with some arbitrary converters.
     *
     * @return arbitrary collection of unit converters to be tested.
     */
    @Override
    public Collection<UnitConverter> getUnitConverters4Test() {
        verifyImplementation();
        final Set<UnitConverter> converters = getUnits4Test().stream()
                .map(TCK::converter)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        /*
         * Add some arbitrary converters.
         */
        assertFalse(converters.isEmpty());
        assertTrue(converters.add(LinearConverter.create(2, 1)));
        return converters;
    }

    /**
     * Returns the converter to system units. May be the identity converter.
     * This method is used for populating the collection of converters returned
     * by {@link #getUnitConverters4Test()}.
     */
    private static <Q extends Quantity<Q>> UnitConverter converter(final Unit<Q> unit) {
        return unit.getConverterTo(unit.getSystemUnit());
    }

    /**
     * Returns {@link UnitFormat} instances to be tested for requirements and recommendations.
     *
     * @return unit formats to be tested.
     */
    @Override
    public Collection<javax.measure.format.UnitFormat> getUnitFormats4Test() {
        verifyImplementation();
        final UnitFormat usingUCUM = new UnitFormat(Locale.US);
        usingUCUM.setStyle(UnitFormat.Style.UCUM);
        return Arrays.asList(usingUCUM, new UnitFormat(Locale.ENGLISH));
    }
}
