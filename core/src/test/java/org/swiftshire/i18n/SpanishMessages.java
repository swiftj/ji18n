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

package org.swiftshire.i18n;

import org.swiftshire.i18n.annotation.BundleType;
import org.swiftshire.i18n.annotation.Message;
import org.swiftshire.i18n.annotation.ResourceBundle;

/**
 * Test messages in Spanish. Note that there are Unicode diacritics used here
 * however no true bundle can be generated for this set of messages because the platform
 * encoding will almost certainly be incompatible and will result in resource bundles with
 * corrupted characters (i.e. Unicode code points don't equate to MacRoman in many cases).
 * <p/>
 * If you truly want to use Unicode code points in your {@link Message} annotations in
 * production code, you must escape them in situ. For example...
 * <p/>
 * <pre class="code">
 * interface SpanishMessages extends Messages {
 * &#64;Message("Ning\u00FAn archivo fue eliminado al procesar la carpeta actual.")
 * String noFilesRemoved();
 * ...
 * }
 * </pre>
 * <p/>
 * Escaping is not done in this code because we have chosen a bundle type of NONE which
 * prevents automatic generation of the resource bundle on the filesystem so it's safe.
 * Naturally this is only good for testing purposes.
 */
@ResourceBundle(locale = "es", type = BundleType.NONE)
public interface SpanishMessages extends Messages {

    @Message("El gabinete {0} contiene {1} carpetas.")
    String directoryContains(String dirName, int folderCount);

    @Message("La carpeta {0} est\u00E1 vac\u00EDa.")
    String folderEmpty(String folderName);

    @Message("Archivo \"{0}\" eliminado.")
    String myStuffDeleted();

    @Message("{0,number} archivos a\u00F1adidos.")
    String addedFiles(int fileCount);

    @Message("Ning\u00FAn archivo fue eliminado al procesar la carpeta actual.")
    String noFilesRemoved();
}
