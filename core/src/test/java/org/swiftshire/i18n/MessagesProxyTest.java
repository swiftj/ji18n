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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.BeforeClass;

import java.util.Locale;

/**
 * Tests for the {@link Messages} API.
 *
 * @author swiftj
 * @since 1.0
 */
public class MessagesProxyTest {
    /**
     * Object under test
     */
    static TestMessages messages = null;

    @BeforeClass
    public static void setupOnce() {
        // Use explicit en_US locale for this test
        messages = MessageFactory.create(TestMessages.class, new Locale("en", "us"));
    }

    @Test
    public void testWelcomeMessage() {
        assertEquals(messages.welcome(), "This is an English welcome message.");
    }

    @Test
    public void testHelloMessage() {
        assertEquals(messages.hello("John Doe", 1), "Hello John Doe. You visited this website 1 times.");
    }

    @Test
    public void testFormatingHelloMessage() {
        MessageFactory.setLocale("en", "");
        TestMessages messages = MessageFactory.create(TestMessages.class);

        String msg = messages.format("hello", "Tom Foolery", 2);

        assertEquals("Hello Tom Foolery. You visited this website 2 times.", msg);
    }
}
