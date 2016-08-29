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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for the {@link MessageFactory}
 *
 * @author swiftj
 * @since 1.0
 */
public class MessageFactoryTest {

    @Before
    public void setup() {
    }

    @Test
    public void testSettingLocaleExplicitly() {
        MessageFactory.setLocale("en", "us");

        assertEquals(MessageFactory.getLocale(), new Locale("en", "us"));
        assertTrue(MessageFactory.getLocale().toString().equals("en_US"));
    }

    @Test
    public void testSetingLocaleDifferently() {
        MessageFactory.setLocale("en", "");

        assertEquals(MessageFactory.getLocale(), new Locale("en", ""));
        assertFalse(MessageFactory.getLocale().toString().equals("en_US"));
    }

    @Test
    public void testGetLocale() {
        MessageFactory.setLocale("de", "de");
        assertEquals(new Locale("de", "de"), MessageFactory.getLocale());
    }

    @Test
    public void testSetThreadLocale() throws InterruptedException {
        MessageFactory.setThreadLocale("de", "de");
        Locale german = new Locale("de", "de");

        assertEquals(MessageFactory.getLocale(), german);

        final CountDownLatch done = new CountDownLatch(1);
        Thread testThread = new Thread() {
            public void run() {
                MessageFactory.setThreadLocale("en", "uk");
                assertEquals(new Locale("en", "uk"), MessageFactory.getLocale());
                done.countDown();
            }
        };

        testThread.start();
        done.await(5, TimeUnit.SECONDS);

        assertEquals(MessageFactory.getLocale(), german);
    }
}
