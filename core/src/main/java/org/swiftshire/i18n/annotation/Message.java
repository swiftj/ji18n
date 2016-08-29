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

import java.lang.annotation.*;

/**
 * Identifies a message as an entry in a resource bundle. Keys in resource bundles are generated from the
 * name of the annotated method itself.
 *
 * @author swiftj
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Message {
    /**
     * A parameter to optionally override the generated key used in the resource bundle.
     * 
     * @return
     */
    String key() default "";

    /**
     * The actual message text. This is the value that is stored in the resource
     * bundle properties file (on the right side of the equals sign).
     *
     * @return the message text
     */
    String value();
}
