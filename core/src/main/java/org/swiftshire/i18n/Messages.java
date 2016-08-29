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

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Base interface for the i18n API. All message interfaces that extend this must observe the following rules:
 * <p/>
 * <ul>
 * <li>Only other interfaces are allowed to extend this interface</li>
 * <li>All methods in the extending interface must return {@code java.lang.String}</li>
 * </ul>
 * <p/>
 * To use this API, you must create your own interface that extends this interface and
 * yours must be annotated properly using {@link Message}
 * on each method which in turn must take the correct number and type of arguments for
 * each respective message.
 * <p/>
 * <pre class="code">
 * public interface ClientMessages extends Messages {
 * &#64;Message("This is an English message")
 * String welcome();
 * <p/>
 * &#64;Message("Hello {0}. You visited this website {1} times")
 * String hello(String name, int count);
 * }
 * </pre>
 *
 * @author swiftj
 * @since 1.0
 */
public interface Messages {
    /**
     * Format a string based on the given key.
     *
     * @param key key for the message in the bundle
     * @return localized message
     */
    String format(String key);

    /**
     * Format a string based on the given key and additional arguments.
     *
     * @param key  key for the message in the bundle
     * @param args arguments for the message
     * @return Localized message
     */
    String format(String key, Object... args);

    /**
     * Return the underlying <code>java.spring.ResourceBundle</code>.
     *
     * @return Resource bundle used for this handler
     * @see java.util.ResourceBundle
     */
    ResourceBundle getBundle();

    /**
     * Returns the locale of this messages object.
     *
     * @return The locale of this message object
     */
    Locale getLocale();
}