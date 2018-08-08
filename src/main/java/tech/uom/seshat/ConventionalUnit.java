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

import java.util.Map;
import java.util.Objects;
import javax.measure.Unit;
import javax.measure.Quantity;
import javax.measure.Dimension;
import javax.measure.UnitConverter;
import javax.measure.UnconvertibleException;
import javax.measure.IncommensurableException;
import tech.uom.seshat.math.Fraction;


/**
 * A unit of measure which is related to a base or derived unit through a conversion formula.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 */
final class ConventionalUnit<Q extends Quantity<Q>> extends AbstractUnit<Q> {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 6963634855104019466L;

    /**
     * The base, derived or alternate units to which this {@code ConventionalUnit} is related.
     * This is called "preferred unit" in GML. This is usually an instance of {@link SystemUnit},
     * but may also be another {@link ConventionalUnit} in some rare cases where this conventional
     * unit can be prefixed like a SI units (e.g. litre: L, cl, mL, µL).
     */
    private final AbstractUnit<Q> target;

    /**
     * The conversion from this unit to the {@linkplain #target} unit.
     */
    final UnitConverter toTarget;

    /**
     * Creates a new unit having the given symbol and EPSG code.
     *
     * @param  target    the base or derived units to which this {@code ConventionalUnit} is related.
     * @param  toTarget  the conversion from this unit to the {@code target} unit.
     * @param  symbol    the unit symbol, or {@code null} if this unit has no specific symbol.
     * @param  scope     {@link UnitRegistry#SI}, {@link UnitRegistry#ACCEPTED}, other constants or 0 if unknown.
     * @param  epsg      the EPSG code, or 0 if this unit has no EPSG code.
     */
    ConventionalUnit(final AbstractUnit<Q> target, final UnitConverter toTarget, final String symbol, final byte scope, final short epsg) {
        super(symbol, scope, epsg);
        this.target   = target;
        this.toTarget = toTarget;
    }

    /**
     * Creates a new unit with default name and symbol for the given converter.
     *
     * @param  target    the base or derived units to which the new unit will be related.
     * @param  toTarget  the conversion from the new unit to the {@code target} unit.
     */
    static <Q extends Quantity<Q>> AbstractUnit<Q> create(final AbstractUnit<Q> target, final UnitConverter toTarget) {
        if (toTarget.isIdentity()) {
            return target;
        }
        /*
         * Verifies if an instance already exists for the given converter.
         * The 'related' array is populated only by the Units class static initializer.
         * The SystemUnitTest.verifyRelatedUnits() method verified that the array does
         * not contain null element and that all 'toTarget' are instances of LinearConverter.
         */
        final ConventionalUnit<Q>[] related = target.related();
        if (related != null && toTarget instanceof LinearConverter) {
            final LinearConverter c = (LinearConverter) toTarget;
            for (final ConventionalUnit<Q> existing : related) {
                if (c.equivalent((LinearConverter) existing.toTarget)) {        // Cast is safe - see above comment.
                    return existing;
                }
            }
        }
        /*
         * If the unit is a SI unit, try to create the SI symbol by the concatenation of the SI prefix
         * with the system unit symbol. The unit symbol are used later as a key for searching existing
         * unit instances.
         */
        String symbol = null;
        if (target.isPrefixable()) {
            final String ts = target.getSymbol();
            if (ts != null && !ts.isEmpty()) {
                double scale = AbstractConverter.scale(toTarget);
                if (!Double.isNaN(scale)) {
                    final char prefix = Prefixes.symbol(scale, power(ts));
                    if (prefix != 0) {
                        symbol = Prefixes.concat(prefix, ts);
                    }
                }
            }
        }
        /*
         * Create the unit, but we may discard it later if an equivalent unit already exists in the cache.
         * The use of the cache is not only for sharing instances, but also because existing instances may
         * have more information.  For example instances provided by Units static constants may contain an
         * EPSG code, or even an alternative symbol (e.g. “hm²” will be replaced by “ha” for hectare).
         */
        ConventionalUnit<Q> unit = new ConventionalUnit<>(target, toTarget, symbol, (byte) 0, (short) 0);
        if (symbol != null) {
            unit = unit.unique(symbol);
        }
        return unit;
    }

    /**
     * Returns a unique instance of this unit if available, or store this unit in the map of existing unit otherwise.
     *
     * @param  symbol  the symbol of this unit, which must be non-null.
     */
    @SuppressWarnings("unchecked")
    final ConventionalUnit<Q> unique(final String symbol) {
        final Object existing = UnitRegistry.putIfAbsent(symbol, this);
        if (existing instanceof ConventionalUnit<?>) {
            final ConventionalUnit<?> c = (ConventionalUnit<?>) existing;
            if (target.equals(c.target)) {
                final boolean equivalent;
                if (toTarget instanceof LinearConverter && c.toTarget instanceof LinearConverter) {
                    equivalent = ((LinearConverter) toTarget).equivalent((LinearConverter) c.toTarget);
                } else {
                    equivalent = toTarget.equals(c.toTarget);   // Fallback for unknown implementations.
                }
                if (equivalent) {
                    return (ConventionalUnit<Q>) c;
                }
            }
        }
        return this;
    }

    /**
     * Returns the positive power after the given unit symbol, or 0 in case of doubt.
     * For example this method returns 1 for “m” and 2 for “m²”. We parse the unit symbol instead
     * than the {@link SystemUnit#dimension} because we can not extract easily the power from the
     * product of dimensions (e.g. what is the M⋅L²∕T³ power?) Furthermore the power will be used
     * for choosing a symbol prefix, so we want it to be consistent with the symbol more than the
     * internal representation.
     *
     * <p>If the unit is itself a product of other units, then this method returns the power of
     * the first unit. For example the power of “m/s²” is 1. This means that the “k” prefix in
     * “km/s²” apply only to the “m” unit.</p>
     */
    static int power(final String symbol) {
        final int length = symbol.length();
        int i = 0, c;
        do {
            if (i >= length) return 1;              // Single symbol (no product, no exponent).
            c = symbol.codePointAt(i);
            i += Character.charCount(c);
        } while (isSymbolChar(c));
        /*
         * At this point we found the first character which is not part of a unit symbol.
         * We may have found the exponent as in “m²”, or we may have found an arithmetic
         * operator like the “/” in “m/s²”. In any cases we stop here because we want the
         * exponent of the first symbol, not the “²” in “m/s²”.
         */
        if (Character.isBmpCodePoint(c)) {
            final int p = Characters.toNormalScript((char) c) - '0';
            if (p >= 0 && p <= 9) {
                if (i < length) {
                    c = symbol.codePointAt(i);
                    if (isSymbolChar(c)) {
                        // Exponent is immediately followed by a another unit symbol character.
                        // We would have expected something else, like an arithmetic operator.
                        return 0;
                    }
                    if (Character.isBmpCodePoint(c)) {
                        c = Characters.toNormalScript((char) c);
                        if (c >= '0' && c <= '9') {
                            // Exponent on two digits. We do not expect so high power after unit symbol.
                            return 0;
                        }
                    }
                }
                return p;
            }
        }
        return 1;
    }

    /**
     * Returns the dimension of this unit.
     * Two units {@code u1} and {@code u2} are {@linkplain #isCompatible(Unit) compatible}
     * if and only if {@code u1.getDimension().equals(u2.getDimension())}.
     *
     * @return the dimension of this unit.
     *
     * @see #isCompatible(Unit)
     */
    @Override
    public Dimension getDimension() {
        return target.getDimension();
    }

    /**
     * Returns the unscaled system unit from which this unit is derived.
     */
    @Override
    public SystemUnit<Q> getSystemUnit() {
        return target.getSystemUnit();
    }

    /**
     * Returns the base units and their exponent whose product is the system unit,
     * or {@code null} if the system unit is a base unit (not a product of existing units).
     *
     * @return the base units and their exponent making up the system unit.
     */
    @Override
    public Map<SystemUnit<?>, Integer> getBaseUnits() {
        return target.getBaseUnits();
    }

    /**
     * Returns the base units used by Apache SIS implementations.
     * Contrarily to {@link #getBaseUnits()}, this method never returns {@code null}.
     */
    @Override
    final Map<SystemUnit<?>, Fraction> getBaseSystemUnits() {
        return target.getBaseSystemUnits();
    }

    /**
     * Casts this unit to a parameterized unit of specified nature or throw a {@code ClassCastException}
     * if the dimension of the specified quantity and this unit's dimension do not match.
     *
     * @param  <T>   the type of the quantity measured by the unit.
     * @param  type  the quantity class identifying the nature of the unit.
     * @return this unit parameterized with the specified type.
     * @throws ClassCastException if the dimension of this unit is different from the specified quantity dimension.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Quantity<T>> Unit<T> asType(final Class<T> type) throws ClassCastException {
        final Unit<T> alternate = target.asType(type);
        if (target.equals(alternate)) {
            return (Unit<T>) this;
        }
        return alternate.transform(toTarget);
    }

    /**
     * Returns a converter of numeric values from this unit to another unit of same type.
     *
     * @param  that  the unit of same type to which to convert the numeric values.
     * @return the converter from this unit to {@code that} unit.
     * @throws UnconvertibleException if the converter can not be constructed.
     */
    @Override
    public UnitConverter getConverterTo(final Unit<Q> that) throws UnconvertibleException {
        if (that == this) {
            return LinearConverter.IDENTITY;
        }
        Objects.requireNonNull(that);
        UnitConverter c = toTarget;
        if (target != that) {                           // Optimization for a common case.
            final Unit<Q> step = that.getSystemUnit();
            if (target != step && !target.isCompatible(step)) {
                // Should never occur unless parameterized type has been compromised.
                throw new UnconvertibleException(incompatible(that));
            }
            c = target.getConverterTo(step).concatenate(c);         // Usually leave 'c' unchanged.
            c =   step.getConverterTo(that).concatenate(c);
        }
        return c;
    }

    /**
     * Returns a converter from this unit to the specified unit of unknown type.
     * This method can be used when the quantity type of the specified unit is unknown at compile-time
     * or when dimensional analysis allows for conversion between units of different type.
     *
     * @param  that  the unit to which to convert the numeric values.
     * @return the converter from this unit to {@code that} unit.
     * @throws IncommensurableException if this unit is not {@linkplain #isCompatible(Unit) compatible} with {@code that} unit.
     *
     * @see #isCompatible(Unit)
     */
    @Override
    public UnitConverter getConverterToAny(final Unit<?> that) throws IncommensurableException {
        if (that == this) {
            return LinearConverter.IDENTITY;
        }
        Objects.requireNonNull(that);
        UnitConverter c = toTarget;
        if (target != that) {                           // Optimization for a common case.
            final Unit<?> step = that.getSystemUnit();
            if (target != step && !target.isCompatible(step)) {
                throw new IncommensurableException(incompatible(that));
            }
            c = target.getConverterToAny(step).concatenate(c);      // Usually leave 'c' unchanged.
            c =   step.getConverterToAny(that).concatenate(c);
        }
        return c;
    }

    /**
     * Unsupported operation for conventional units, as required by JSR-363 specification.
     *
     * @param  symbol  the new symbol for the alternate unit.
     * @return the alternate unit.
     * @throws UnsupportedOperationException always thrown since this unit is not an unscaled standard unit.
     *
     * @see SystemUnit#alternate(String)
     */
    @Override
    public Unit<Q> alternate(final String symbol) {
        throw new UnsupportedOperationException();
    }

    /**
     * Ensures that the scale of measurement of this units is a ratio scale.
     */
    private void ensureRatioScale() {
        if (!toTarget.isLinear()) {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns the product of this unit with the one specified.
     *
     * @param  multiplier  the unit multiplier.
     * @return {@code this} × {@code multiplier}
     */
    @Override
    public Unit<?> multiply(final Unit<?> multiplier) {
        ensureRatioScale();
        return target.multiply(multiplier).transform(toTarget);
    }

    /**
     * Returns the quotient of this unit with the one specified.
     *
     * @param  divisor  the unit divisor.
     * @return {@code this} ∕ {@code divisor}
     */
    @Override
    public Unit<?> divide(final Unit<?> divisor) {
        ensureRatioScale();
        return target.divide(divisor).transform(toTarget);
    }

    /**
     * Returns a unit equals to this unit raised to an exponent.
     *
     * @param  n  the exponent.
     * @return the result of raising this unit to the exponent.
     */
    @Override
    public Unit<?> pow(final int n) {
        ensureRatioScale();
        final Unit<?> result = target.pow(n);
        return (result == target) ? this : result.transform(LinearConverter.pow(toTarget, n, false));
    }

    /**
     * Returns a unit equals to the given root of this unit.
     *
     * @param  n  the root's order.
     * @return the result of taking the given root of this unit.
     * @throws ArithmeticException if {@code n == 0}.
     */
    @Override
    public Unit<?> root(final int n) {
        ensureRatioScale();
        final Unit<?> result = target.root(n);
        return (result == target) ? this : result.transform(LinearConverter.pow(toTarget, n, true));
    }

    /**
     * Returns the unit derived from this unit using the specified converter.
     *
     * @param  operation  the converter from the transformed unit to this unit.
     * @return the unit after the specified transformation.
     */
    @Override
    public Unit<Q> transform(UnitConverter operation) {
        Objects.requireNonNull(operation);
        AbstractUnit<Q> base = this;
        if (!isPrefixable()) {
            base = target;
            operation = toTarget.concatenate(operation);
        }
        return create(base, operation);
    }

    /**
     * Compares this unit with the given object for equality.
     *
     * @param  other  the other object to compare with this unit, or {@code null}.
     * @return {@code true} if the given object is equal to this unit.
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (super.equals(other)) {
            final ConventionalUnit<?> that = (ConventionalUnit<?>) other;
            return target.equals(that.target) && toTarget.equals(that.toTarget);
        }
        return false;
    }

    /**
     * Returns a hash code value for this unit.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + 37 * (target.hashCode() + 31 * toTarget.hashCode());
    }
}
