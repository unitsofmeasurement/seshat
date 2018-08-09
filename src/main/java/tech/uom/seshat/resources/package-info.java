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
 * Localized resources for Seshat.
 * Resources are provided in binary files having the "{@code .utf}" extension.
 * The resource keys are numeric constants declared in the {@code Keys} static inner classes.
 * Values are strings which may optionally have slots for one or more parameters, identified
 * by the "<code>{</code><var>n</var><code>}</code>" characters sequences where <var>n</var>
 * is the parameter number (first parameter is "<code>{0}</code>").
 * If, and only if, a string value has slots for at least one parameter, then:
 *
 * <ul>
 *   <li>the key name ends with the {@code '_'} character followed by the expected number of parameters;</li>
 *   <li>the value string is compliant with the {@link java.text.MessageFormat} syntax.</li>
 * </ul>
 *
 * <div class="note"><b>Note:</b>
 * {@link java.util.Formatter java.util.Formatter} is an alternative to {@link java.text.MessageFormat} providing
 * similar functionalities with a C/C++ like syntax. However {@code MessageFormat} has two advantages: it provides
 * a {@code choice} format type (useful for handling plural forms), and localizes properly objects of unspecified type
 * (by contrast, the {@code Formatter} {@code "%s"} type always invoke {@code toString()}). The later advantage is
 * important for messages in which the same argument could receive {@link java.lang.Number} or {@link java.util.Date}
 * instances as well as {@link java.lang.String}. Furthermore, the {@link java.util.logging} framework is designed for
 * use with {@code MessageFormat} (see the {@code Formatter.formatMessage(LogRecord)} method).</div>
 *
 * Developers can add resources by editing the {@code *.properties} file in the source code directory,
 * then run the localized resources compiler provided in the {@code sis-build-helper} module.
 * Developers shall <strong>not</strong> apply the {@code MessageFormat} rules for using quotes,
 * since the resources compiler will apply itself the <cite>doubled single quotes</cite> when
 * necessary. This avoid the unfortunate confusion documented in the warning section of
 * {@link java.text.MessageFormat} javadoc.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 *
 * @see java.util.ResourceBundle
 * @see java.text.MessageFormat
 */
package tech.uom.seshat.resources;
