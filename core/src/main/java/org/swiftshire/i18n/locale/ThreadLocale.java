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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ThreadLocale stores a locale. The locale can be either thread local or global.
 *
 * @author swiftj
 * @since 1.0
 */
public class ThreadLocale {
    /**
     * True if thread local storage should be used
     */
    private AtomicBoolean thread = new AtomicBoolean(true);

    /**
     * Thread's current locale
     */
    private Locale locale;

    /**
     * Store a locale for each thread with a thread local object.
     */
    private ThreadLocal<Locale> threadLocal = new ThreadLocal<Locale>() {
        protected Locale initialValue() {
            return Locale.getDefault();
        }
    };

    /**
     * Set if the locale should be global or thread local
     *
     * @param useThread true if the locale is thread local
     */
    public void setThread(boolean useThread) {
        thread.set(useThread);
    }

    /**
     * Return the current locale, either the global
     * locale or the thread locale
     *
     * @return current locale
     */
    public Locale get() {
        if (thread.get()) {
            return threadLocal.get();
        }
        else {
            return locale;
        }
    }

    /**
     * Set the locale which should be used, either
     * the globale locale or the thread locale
     *
     * @param locale locale to use
     */
    public void set(Locale locale) {
        if (thread.get()) {
            threadLocal.set(locale);
        }
        else {
            this.locale = locale;
        }
    }
}
