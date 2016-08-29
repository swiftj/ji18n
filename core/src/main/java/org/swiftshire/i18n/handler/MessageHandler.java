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

package org.swiftshire.i18n.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftshire.i18n.Messages;
import org.swiftshire.i18n.annotation.Message;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Implementation of the {@link Messages} interface as well as derived interfaces
 * that represent message bundles.
 *
 * @author swiftj
 * @since 1.0
 */
public class MessageHandler implements Messages, InvocationHandler {
    /**
     * Logging handle
     */
    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    /**
     * Decorator string used to prefix and suffix bogus message keys
     */
    private static final String BOGUS_KEY_DECORATOR = "!!";

    /**
     * Internal resource bundle this handler delegates to.
     */
    private final ResourceBundle bundle;

    /**
     * Ctor takes a given bundle to use directly.
     *
     * @param bundle bundle to use internally.
     */
    public MessageHandler(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Creates a new {@code MessageHandler}
     *
     * @param bundleName  Base name of the bundle to use
     * @param locale      Locale of bundle to use for this handler
     * @param classLoader Class loader to use to load bundle with
     * @see java.util.PropertyResourceBundle
     */
    public MessageHandler(String bundleName, Locale locale, ClassLoader classLoader) {
        this.bundle = ResourceBundle.getBundle(bundleName, locale, classLoader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return bundle.getLocale();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(String key) {
        return formatArgs(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(String key, Object... args) {
        return formatArgs(key, args);
    }

    /**
     * Formats the message with the given arguments.
     *
     * @param key  Bundle key identifying message to use
     * @param args Var args to insert into bundle message.
     * @return Localized string
     * @see java.text.MessageFormat
     */
    private String formatArgs(String key, Object... args) {

        if (bundle != null) {

            try {
                return MessageFormat.format(bundle.getString(key), args);
            }
            catch (MissingResourceException ex) {
                log.error(ex.getMessage());
            }
        }

        return BOGUS_KEY_DECORATOR + key + BOGUS_KEY_DECORATOR;
    }

    /**
     * Proxy method handler to implement all message methods and return a
     * resulting l10n string.
     *
     * @param proxy  Proxy object held by the caller
     * @param method Method (aka 'message') caller wants localized
     * @param args   Optional arguments to include in the localized message.
     * @return Localized message string
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Message annotation = method.getAnnotation(Message.class);

        if (annotation != null) {
            final String key = annotation.key();

            return format(key.isEmpty() ? method.getName() : key, args);
        }
        else {
            // This is a Messages method call so dispatch to internal impl
            return method.invoke(this, args);
        }
    }
}
