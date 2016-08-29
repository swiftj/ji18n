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

import org.swiftshire.i18n.annotation.Message;

public interface GenericMessages extends Messages {

    @Message("Directory {0} contains {1} folders.")
    String directoryContains(String dirName, int folderCount);

    @Message("Folder {0} is empty.")
    String folderEmpty(String folderName);

    @Message("File \"My Stuff\" deleted.")
    String myStuffDeleted();

    @Message("Added {0,number} files.")
    String addedFiles(int fileCount);

    @Message("No files were removed while processing current folder.")
    String noFilesRemoved();
}
