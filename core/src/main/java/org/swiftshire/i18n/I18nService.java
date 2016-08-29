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

import org.swiftshire.i18n.locale.LocaleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MBean implementation for the {@link MessageFactory}
 *
 * @author swiftj
 * @since 1.0
 */
public class I18nService implements I18nServiceMBean {
    /**
     * {@inheritDoc}
     *
     * @return
     */
    public boolean getFabricateBundles() {
        return MessageFactory.doBundleFabrication.get();
    }

    /**
     * {@inheritDoc}
     *
     * @param yesno
     */
    public void setFabricateBundles(boolean yesno) {
        MessageFactory.doBundleFabrication.set(yesno);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public int getResourceBundleCount() {
        return MessageFactory.handlers.size();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public int getMissingResourceBundleCount() {
        return MessageFactory.missingBundleCount.get();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public int getFabricatedBundleCount() {
        return MessageFactory.fabricatedBundleCount.get();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public Locale getApplicationLocale() {
        return LocaleManager.getManager().getLocale();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public List<String> fetchBundles() {
        List<String> bundles = new ArrayList<>();

        for (String name : MessageFactory.handlers.keySet()) {
            bundles.add(name);
        }

        return bundles;
    }
}
