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
import javax.measure.UnitConverter;


/**
 * Linear converter with a scale factor of 1 and an offset of 0. We define a class for this special case
 * instead of using the more generic {@link LinearConverter} class because we want to avoid performing
 * any arithmetic operation in the {@link #convert(double)} method, in order to preserve negative zero:
 *
 * <pre>
 *     convert(-0d) ≡ -0d
 * </pre>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class IdentityConverter extends AbstractConverter {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 1230536100139464866L;

    /**
     * The identity linear converter.
     */
    static final IdentityConverter INSTANCE = new IdentityConverter();

    /**
     * For {@link #INSTANCE} only.
     */
    private IdentityConverter() {
    }

    /** Straight forward implementation. */
    @Override public boolean       isLinear()                   {return true;}
    @Override public boolean       isIdentity()                 {return true;}
    @Override public UnitConverter inverse()                    {return this;}
    @Override public double        convert(double value)        {return value;}
    @Override public double        derivative(double value)     {return 1;}
    @Override public UnitConverter concatenate(UnitConverter c) {return c;}
    @Override public String        toString()                   {return "y = x";}

    /**
     * Returns the value unchanged, with a check against null values
     * for consistency with {@link LinearConverter#convert(Number)}.
     */
    @Override
    public Number convert(Number value) {
        Objects.requireNonNull(value);
        return value;
    }

    /**
     * Returns a hash code value for this unit converter.
     */
    @Override
    public int hashCode() {
        return (int) serialVersionUID;
    }

    /**
     * Compares this converter with the given object for equality. This method may return {@code true}
     * only if {@code Object} is an instance of {@link IdentityConverter} or {@link LinearConverter}.
     * We apply this restriction in order to be symmetric with those cases,
     * i.e. {@code A.equals(B)} = {@code B.equals(A)}.
     */
    @Override
    public boolean equals(final Object other) {
        // See method javadoc for why we restrict to AbstractConverter instead of UnitConverter.
        return (other instanceof AbstractConverter) && ((AbstractConverter) other).isIdentity();
    }
}
