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

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.junit.Test;
import tech.units.tck.TCKRunner;
import tech.units.tck.util.ServiceConfiguration;

import static org.junit.Assert.*;


/**
 * Run the TCK tests.
 *
 * @version 1.2
 */
public final strictfp class TCKTest {
    /**
     * Run the TCK tests.
     */
    @Test
    public void runTCK() {
        /*
         * Needs to amend the content of `module-info.java` file.
         */
        final Module module = getClass().getModule();
        assertTrue(module.isNamed());
        module.addUses(ServiceConfiguration.class);
        /*
         * Attempt to find the configuration ourselves before to let TCK tests run.
         * This provides more useful error message in case of failure and avoid the
         * same TestNG message to be repeated many times.
         */
        boolean found = false;
        try {
            for (ServiceConfiguration config : ServiceLoader.load(ServiceConfiguration.class)) {
                found |= (config instanceof TCK);
            }
        } catch (ServiceConfigurationError e) {
            fail("Target directory must be clean before Seshat can run TCK tests, "
                 + "otherwise the following exception happen:\n" + e + '\n'
                 + "See https://github.com/unitsofmeasurement/unit-tck/pull/32");
        }

        org.junit.Assume.assumeTrue(found);     // Temporarily disable the TCK tests until pull #22 is merged.

        assertTrue("Service configuration not found.", found);
        /*
         * Configuration seems okay. Let TCK running.
         */
        final TCKRunner runner = new TCKRunner();
        int returnCode = runner.run(null, null, null);
        assertEquals("Some TCK tests failed.", 0, returnCode);
    }
}
