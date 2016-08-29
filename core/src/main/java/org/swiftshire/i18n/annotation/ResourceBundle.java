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
 * Annotation that allows user more control over the backing resource bundle generated and processed by
 * the {@link MessageFactory} and the Maven i18n plugin.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ResourceBundle {
    /**
     * Optional description for this resource bundle.
     *
     * @return Description of this bundle, if any.
     */
    String name() default "";

    /**
     * Optional description for this resource bundle.
     *
     * @return Description of this bundle, if any.
     */
    String locale() default "";

    /**
     * Optional bundle type specifier to control the generation
     * and format expectations of the backing resource bundle on
     * the filesystem.
     *
     * @return
     */
    BundleType type() default BundleType.PROPERTY;

    /**
     * Optionally include a set of messages that are not tied to any particular
     * method of the annotated {@link com.rsa.netwitness.carlos.i18n.Messages} class.
     * This allows one to statically include ad-hoc messages to the bundle without
     * the need for an annotated method. This is useful for JSF message bundles
     * where one overrides static JSF messages if they wish while still including
     * their own messages.
     *
     * @return
     */
    String[] define() default "";
}
