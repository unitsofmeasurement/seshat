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
package tech.uom.seshat.test;

import tech.uom.seshat.Quantities;
import tech.uom.seshat.Units;

/**
 * Executes some simple operations using Seshat.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("N⋅m = " + Units.NEWTON.multiply(Units.METRE));
        System.out.println("kg/m³ = " + Units.KILOGRAM.divide(Units.METRE.pow(3)));
        System.out.println("2.54 cm = " + Units.CENTIMETRE.multiply(2.54));
        System.out.println("2345678 cm = " + Units.CENTIMETRE.getConverterTo(Units.KILOMETRE).convert(2345678) + " km");
        System.out.println("valueOf(\"km/1000*N\") = " + Units.valueOf("km/1000*N"));
        System.out.println(Quantities.create(3, Units.CENTIMETRE).multiply(Quantities.create(4, Units.CENTIMETRE)));
    }
}
