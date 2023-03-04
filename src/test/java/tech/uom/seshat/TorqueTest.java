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

import javax.measure.quantity.Energy;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests operations on {@link Torque}.
 * This class tests in particular that energy is not confused with torque.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.4
 * @since   1.4
 */
public final class TorqueTest {
    /**
     * Verifies that torque has the same dimensions as energy.
     */
    @Test
    public void testDimensions() {
        assertEquals(Units.JOULE.getDimension(), Units.TORQUE.getDimension());
    }

    /**
     * Tests the capability to distinguish between energy and torque.
     */
    @Test
    public void testDistinction() {
        assertNotSame(Units.JOULE, Units.TORQUE);
        assertSame(Units.TORQUE, Units.JOULE .asType(Torque.class));
        assertSame(Units.JOULE,  Units.TORQUE.asType(Energy.class));
    }

    /**
     * Verifies that basic operations return energy by default,
     * since this is assumed more frequently used than torque.
     */
    @Test
    public void testDefaultToEnergy() {
        assertSame(Units.JOULE, Units.NEWTON.multiply(Units.METRE));
        assertSame(Units.JOULE, Units.valueOf("N⋅m"));
    }

    /**
     * Tests the symbols associated to torque and energy.
     */
    @Test
    public void testSymbols() {
        assertEquals("N⋅m", Units.TORQUE.toString());
        assertEquals("J",   Units.JOULE .toString());
    }
}
