/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.compiler.workspace;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

/**
 * This represents a Document Manager for the workspace. Example, an in-memory document
 * manager that keeps dirty files in-memory and will match the collection of files currently
 * open in tool's workspace.
 */
public interface WorkspaceDocumentManager {
    /**
     * Checks whether the given file is open in workspace.
     *
     * @param filePath Path of the file
     * @return True if the given file is open
     */
    boolean isFileOpen(Path filePath);

    /**
     * Opens the given file in document manager.
     *
     * @param filePath Path of the file
     * @param content Content of the file
     */
    void openFile(Path filePath, String content);

    /**
     * Updates given file in document manager with new content.
     *
     * @param filePath Path of the file
     * @param updatedContent New content of the file
     */
    void updateFile(Path filePath, String updatedContent);

    /**
     * Close the given file in document manager.
     *
     * @param filePath Path of the file
     */
    void closeFile(Path filePath);

    /**
     * Gets uptodate content of the file.
     *
     * @param filePath Path of the file
     * @return Content of the file
     */
    String getFileContent(Path filePath);

    /**
     * Acquire a file lock.
     * @param filePath Path of the file
     * @return lock or null
     */
    Optional<Lock> lockFile(Path filePath);
}
