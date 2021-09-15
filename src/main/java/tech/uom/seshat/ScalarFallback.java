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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.measure.Quantity;
import javax.measure.Unit;


/**
 * Fallback used when no {@link Scalar} implementation is available for a given quantity type.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@SuppressWarnings("serial")
final class ScalarFallback<Q extends Quantity<Q>> extends Scalar<Q> implements InvocationHandler {
    /**
     * The type implemented by proxy instances.
     */
    private final Class<Q> type;

    /**
     * Creates a new scalar for the given value and unit of measurement.
     * Callers should ensure that all the arguments are non-null.
     */
    private ScalarFallback(final double value, final Unit<Q> unit, final Class<Q> type) {
        super(value, unit);
        this.type = type;
    }

    /**
     * Creates a new quantity of the same type than this quantity but a different value and/or unit.
     */
    @Override
    Quantity<Q> create(final double newValue, final Unit<Q> newUnit) {
        return factory(newValue, newUnit, type);
    }

    /**
     * Creates a new {@link ScalarFallback} instance implementing the given quantity type.
     * Callers should ensure that all the arguments are non-null.
     *
     * @param  value  the numerical value.
     * @param  unit   unit of measurement.
     * @param  type   interface implemented by proxy instances.
     */
    @SuppressWarnings("unchecked")
    static <Q extends Quantity<Q>> Q factory(final double value, final Unit<Q> unit, final Class<Q> type) {
        final ScalarFallback<Q> quantity = new ScalarFallback<>(value, unit, type);
        return (Q) Proxy.newProxyInstance(Scalar.class.getClassLoader(), new Class<?>[] {type}, quantity);
    }

    /**
     * Invoked when a method of the {@link Quantity} interface is invoked. Delegates to the same
     * method of the parent {@link Scalar} class. This works not only for the quantity methods,
     * but also for {@link #toString()}, {@link #hashCode()} and {@link #equals(Object)}.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws ReflectiveOperationException {
        return method.invoke(this, args);
    }
}
