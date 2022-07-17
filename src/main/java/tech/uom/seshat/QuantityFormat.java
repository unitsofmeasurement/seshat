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

import java.util.Objects;
import java.util.Locale;
import java.text.Format;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.lang.reflect.InaccessibleObjectException;
import javax.measure.Quantity;
import javax.measure.Unit;


/**
 * Parses and formats numbers with units of measurement.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.2
 *
 * @see NumberFormat
 * @see UnitFormat
 *
 * @since 1.2
 * @module
 */
public class QuantityFormat extends Format {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 1014042719969477503L;

    /**
     * The default separator used between numerical value and its unit of measurement.
     * Current value is narrow no-break space (U+202F).
     */
    public static final char SEPARATOR = '\u202F';

    /**
     * The format for parsing and formatting the number part.
     */
    protected final NumberFormat numberFormat;

    /**
     * The format for parsing and formatting the unit of measurement part.
     */
    protected final UnitFormat unitFormat;

    /**
     * Creates a new instance for the given locale.
     *
     * @param  locale  the locale for the quantity format.
     */
    public QuantityFormat(final Locale locale) {
        Objects.requireNonNull(locale);
        numberFormat = NumberFormat.getNumberInstance(locale);
        unitFormat   = new UnitFormat(locale);
    }

    /**
     * Creates a new instance using the given number and unit formats.
     *
     * @param  numberFormat  the format for parsing and formatting the number part.
     * @param  unitFormat    the format for parsing and formatting the unit of measurement part.
     */
    public QuantityFormat(final NumberFormat numberFormat, final UnitFormat unitFormat) {
        Objects.requireNonNull(numberFormat);
        Objects.requireNonNull(unitFormat);
        this.numberFormat = numberFormat;
        this.unitFormat   = unitFormat;
    }

    /**
     * Formats the specified quantity in the given buffer.
     * The given object shall be an {@link Quantity} instance.
     *
     * @param  quantity    the quantity to format.
     * @param  toAppendTo  where to format the quantity.
     * @param  pos         where to store the position of a formatted field, or {@code null} if none.
     * @return the given {@code toAppendTo} argument, for method calls chaining.
     */
    @Override
    public StringBuffer format(final Object quantity, StringBuffer toAppendTo, FieldPosition pos) {
        final Quantity<?> q = (Quantity<?>) quantity;
        if (pos == null) pos = new FieldPosition(0);
        toAppendTo = numberFormat.format(q.getValue(), toAppendTo, pos).append(SEPARATOR);   // Narrow no-break space.
        toAppendTo = unitFormat.format(q.getUnit(), toAppendTo, pos);
        return toAppendTo;
    }

    /**
     * Parses text from a string to produce a quantity, or returns {@code null} if the parsing failed.
     *
     * @param  source  the text, part of which should be parsed.
     * @param  pos     index and error index information.
     * @return a unit parsed from the string, or {@code null} in case of error.
     */
    @Override
    public Object parseObject(final String source, final ParsePosition pos) {
        final Number value = numberFormat.parse(source, pos);
        if (value != null) {
            final Unit<?> unit = unitFormat.parse(source, pos);
            if (unit != null) {
                return Quantities.create(value.doubleValue(), unit);
            }
        }
        return null;
    }

    /**
     * Returns a clone of this format.
     *
     * @return a clone of this format.
     */
    @Override
    public QuantityFormat clone() {
        final QuantityFormat clone = (QuantityFormat) super.clone();
        try {
            clone.setFinalField("numberFormat", numberFormat);
            clone.setFinalField("unitFormat", unitFormat);
        } catch (ReflectiveOperationException e) {
            throw (InaccessibleObjectException) new InaccessibleObjectException().initCause(e);
        }
        return clone;
    }

    /**
     * Clones the given format, then assigns the clone to the field of given name.
     */
    private void setFinalField(final String fieldName, final Format value) throws ReflectiveOperationException {
        final java.lang.reflect.Field f = QuantityFormat.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(this, value.clone());
    }
}
