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

import java.util.Objects;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.io.Serializable;
import javax.measure.Unit;
import javax.measure.Dimension;
import javax.measure.spi.SystemOfUnits;


/**
 * Lookup mechanism for finding a units from its quantity, dimension or symbol.
 * This class opportunistically implements {@link SystemOfUnits}, but Apache SIS
 * rather uses the static methods directly since we define all units in terms of SI.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
abstract class UnitRegistry implements SystemOfUnits, Serializable {  // TODO
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -84557361079506390L;

    /**
     * A bitmask specifying that the unit symbol can be combined with a SI prefix.
     * This is usually combined only with {@link #SI}, not {@link #ACCEPTED} except
     * the litre unit (cL, mL, etc).
     */
    static final byte PREFIXABLE = 1;

    /**
     * Identifies units defined by the SI system.
     * All {@link SystemUnit} instances with this code can have a SI prefix.
     */
    static final byte SI = 2;

    /**
     * Identifies units defined outside the SI system but accepted for use with SI.
     */
    static final byte ACCEPTED = 4;

    /**
     * Identifies units defined for use in British imperial system.
     */
    static final byte IMPERIAL = 8;

    /**
     * Identifies units defined in another system than the above.
     */
    static final byte OTHER = 16;

    /**
     * All {@link UnitDimension}, {@link SystemUnit} or {@link ConventionalUnit} that are hard-coded in Apache SIS.
     * This map is populated by {@link Units} static initializer and shall not be modified after initialization,
     * in order to avoid the need for synchronization. Key and value types are restricted to the following pairs:
     *
     * <table class="sis">
     *   <caption>Key and value types</caption>
     *   <tr><th>Key type</th>                            <th>Value type</th>            <th>Description</th></tr>
     *   <tr><td>{@code Map<UnitDimension,Fraction>}</td> <td>{@link UnitDimension}</td> <td>Key is the base dimensions with their powers</td></tr>
     *   <tr><td>{@link UnitDimension}</td>               <td>{@link SystemUnit}</td>    <td>Key is the dimension of base or derived units.</td></tr>
     *   <tr><td>{@code Class<Quantity>}</td>             <td>{@link SystemUnit}</td>    <td>Key is the quantity type of base of derived units.</td></tr>
     *   <tr><td>{@link String}</td>                      <td>{@link AbstractUnit}</td>  <td>Key is the unit symbol.</td></tr>
     *   <tr><td>{@link Short}</td>                       <td>{@link AbstractUnit}</td>  <td>Key is the EPSG code.</td></tr>
     * </table>
     */
    private static final Map<Object,Object> HARD_CODED = new HashMap<>(256);

    /**
     * Adds an alias for the given unit. The given alias shall be either an instance of {@link String}
     * (for a symbol alias) or an instance of {@link Short} (for an EPSG code alias).
     */
    static void alias(final Unit<?> unit, final Comparable<?> alias) {
        assert !Units.initialized : unit;        // This assertion happens during Units initialization, but it is okay.
        if (HARD_CODED.put(alias, unit) != null) {
            throw new AssertionError(unit);      // Shall not map the same alias twice.
        }
    }

    /**
     * Name of this system of units.
     */
    final String name;

    /**
     * The bitmask for units to include. Can be any combination of {@link #SI}, {@link #ACCEPTED},
     * {@link #IMPERIAL} or {@link #OTHER} bits.
     */
    private final int includes;

    /**
     * The value returned by {@link #getUnits()}, created when first needed.
     */
    private transient Set<Unit<?>> units;

    /**
     * Creates a new unit system.
     */
    UnitRegistry(final String name, final int includes) {
        this.name     = name;
        this.includes = includes;
    }

    /**
     * Returns the name of this system of units.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns a read only view over the units explicitly defined by this system.
     * This include the base and derived units which are assigned a special name and symbol.
     * This set does not include new units created by arithmetic or other operations.
     */
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public Set<Unit<?>> getUnits() {
        if (Units.initialized) {                    // Force Units class initialization.
            synchronized (this) {
                if (units == null) {
                    units = new HashSet<>();
                    for (final Object value : HARD_CODED.values()) {
                        if (value instanceof AbstractUnit<?>) {
                            final AbstractUnit<?> unit = (AbstractUnit<?>) value;
                            if ((unit.scope & includes) != 0) {
                                units.add(unit);
                            }
                        }
                    }
                    units = Collections.unmodifiableSet(units);
                }
            }
        }
        return units;
    }

    /**
     * Returns the units defined in this system having the specified dimension, or an empty set if none.
     */
    @Override
    public Set<Unit<?>> getUnits(final Dimension dimension) {
        Objects.requireNonNull(dimension);
        final Set<Unit<?>> filtered = new HashSet<>();
        for (final Unit<?> unit : getUnits()) {
            if (dimension.equals(unit.getDimension())) {
                filtered.add(unit);
            }
        }
        return filtered;
    }
}
