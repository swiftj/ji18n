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

package org.swiftshire.i18n.locale;

import java.util.Locale;

/**
 * A simple thread aware locale manager class.
 *
 * @author jswift
 */
public class LocaleManager {
    /**
     *
     */
    private static LocaleManager manager = new LocaleManager();

    /**
     *
     */
    private static ThreadLocale locale = new ThreadLocale();

    /**
     * @return
     */
    public static LocaleManager getManager() {
        return manager;
    }

    /**
     * Set the locale with the language and country for the
     * current thread.
     *
     * @param locale locale to set
     */
    public void setThreadLocale(Locale locale) {
        LocaleManager.locale.setThread(true);
        LocaleManager.locale.set(locale);
    }

    /**
     * Set globally the locale with the language and country
     *
     * @param locale locale to set
     */
    public void setLocale(Locale locale) {
        LocaleManager.locale.setThread(false);
        LocaleManager.locale.set(locale);
    }

    /**
     * Return currently used locale, either for thread or global.
     *
     * @return currently used locale
     */
    public Locale getLocale() {
        return locale.get();
    }
}
