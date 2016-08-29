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

package org.swiftshire.i18n.annotation;

/**
 * A resource bundle type. These constants are used with the {@link ResourceBundle} annotation type
 * to identify to the what type of resource bundle should be used. The default is {@code PROPERTY}.
 *
 * @author swiftj
 * @since 1.0
 */
public enum BundleType {
    /**
     * No bundle should be generated or expected. This means that only the {@link Message} annotated
     * messages are necessary.
     */
    NONE,

    /**
     * The default bundle type that indicates a <code>java.spring.PropertyResourceBundle</code> should
     * be expected or generated.
     */
    PROPERTY,

    /**
     * Bundle type that indicates that the backing resource bundle should be expected to be or generated in the XML
     * format produced by <code>java.spring.Properties#storeToXML()</code>.
     */
    XML,

    /**
     * Bundle type that indicates that the backing resource bundle should be expected to be or generated using the
     * standard XLIFF format as defined by the <a href="http://docs.oasis-open.org/xliff/xliff-core/xliff-core.html">
     * XLIFF Specification</a>.
     */
    XLIFF
}

