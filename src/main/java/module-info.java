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

/**
 * Implementation of Units of Measurement API defined by JSR 363.
 * Seshat is a subset of Apache Spatial Information System (SIS)
 * library keeping only the classes required for JSR 363, with
 * geospatial-specific functionalities omitted.
 *
 * @author  Martin Desruisseaux (MPO, IRD, Geomatys)
 * @version 1.0
 * @since   1.0
 */
module tech.uom.seshat {
    requires unit.api;          // Temporary name, to be renamed after JSR-385 is released.
    requires java.logging;

    exports tech.uom.seshat;
    /*
     * Do not export tech.uom.seshat.math and tech.uom.seshat.util packages; it is not the
     * purpose of Seshat to publish additional collection implementations. Those classes are
     * public in Apache SIS if needed.
     */
}
