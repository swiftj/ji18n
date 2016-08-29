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

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for i18n package
 *
 * @author swiftj
 * @since 1.0
 */
public class PropertyBundleTest {
    @Before
    public void setup() {
    }

    @Test
    public void testLocaleIdentity() {
        GenericMessages en_US = MessageFactory.create(GenericMessages.class, new Locale("en", "us"));

        GenericMessages defaultLocale = MessageFactory.create(GenericMessages.class);

        assertEquals(defaultLocale.getBundle().getLocale(), en_US.getBundle().getLocale());

        GenericMessages es = MessageFactory.create(GenericMessages.class, "es");

        assertFalse(defaultLocale.getBundle().getLocale().equals(es.getBundle().getLocale()));
        assertEquals(es.getBundle().getLocale(), new Locale("es"));
    }

    @Test
    public void testDirectoryContainsMessageEnglish() {

        GenericMessages messages = MessageFactory.create(GenericMessages.class);

        int count = 5;
        final String directory = "/tmp";
        final String msg = "Directory " + directory + " contains " + count + " folders.";

        assertEquals(msg, messages.directoryContains(directory, count));
    }

    @Test
    public void testDirectoryContainsMessageSpanish() {

        GenericMessages messages = MessageFactory.create(GenericMessages.class, new Locale("es"));

        int count = 5;
        final String directory = "/tmp";
        final String msg = "El gabinete " + directory + " contiene " + count + " carpetas.";

        assertEquals(msg, messages.directoryContains(directory, count));
    }

    @Test
    public void testFolderEmptyMessageEnglish() {

        GenericMessages messages = MessageFactory.create(GenericMessages.class);

        final String folder = "My Documents";
        final String msg = "Folder " + folder + " is empty.";

        assertEquals(msg, messages.folderEmpty(folder));
    }

    @Test
    public void testFolderEmptyMessageISOEnglish() {

        GenericMessages messages = MessageFactory.create(GenericMessages.class, "en_US");

        final String folder = "My Documents";
        final String msg = "Folder " + folder + " is empty.";

        assertEquals(msg, messages.folderEmpty(folder));
    }

    @Test
    public void testFolderEmptyMessageSpanish() {

        GenericMessages messages = MessageFactory.create(GenericMessages.class, new Locale("es"));

        final String folder = "My Documents";
        final String msg = "La carpeta " + folder + " est\u00e1 vac\u00eda.";

        // Note this 
        assertEquals(msg, messages.folderEmpty(folder));
    }

    @Test
    public void testFolderEmptyMessageISOSpanish() {

        GenericMessages messages = MessageFactory.create(GenericMessages.class, "es");

        final String folder = "My Documents";
        final String msg = "La carpeta " + folder + " est\u00e1 vac\u00eda.";

        // Note this
        assertEquals(msg, messages.folderEmpty(folder));
    }

    @Test
    public void testAddedFilesEnglish() {

        GenericMessages messages = MessageFactory.create(GenericMessages.class);

        int count = 3;
        final String msg = "Added " + count + " files.";

        assertEquals(msg, messages.addedFiles(count));
    }

    @Test
    public void testAddedFilesSpanish() {

        GenericMessages messages = MessageFactory.create(GenericMessages.class, "es");

        int count = 3;
        final String msg = "" + count + " archivos a\u00f1adidos.";

        assertEquals(msg, messages.addedFiles(count));
    }
}
