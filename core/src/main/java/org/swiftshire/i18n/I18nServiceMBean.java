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

import java.util.List;
import java.util.Locale;

/**
 * Standard MBean for stats collection and minimal dynamic control for the {@link MessageFactory}.
 *
 * @author swiftj
 * @since 1.0
 */
public interface I18nServiceMBean {
    /**
     * Switch to enable or disable message bundle fabrication.
     *
     * @return Fabrication flag
     */
    boolean getFabricateBundles();

    /**
     * Switch to enable or disable message bundle fabrication.
     *
     * @param yesno
     */
    void setFabricateBundles(boolean yesno);

    /**
     * Number of resource bundles currently in use.
     *
     * @return Current bundle count
     */
    int getResourceBundleCount();

    /**
     * Number of requested resource bundles that were not found.
     *
     * @return Missing bundle count.
     */
    int getMissingResourceBundleCount();

    /**
     * Number of message bundles fabricated.
     *
     * @return Fabricated bundle count
     */
    int getFabricatedBundleCount();

    /**
     * Current application locale.
     *
     * @return Locale
     */
    Locale getApplicationLocale();

    /**
     * Returns a list of the active message bundles.
     *
     * @return List of bundle names
     */
    List<String> fetchBundles();
}
