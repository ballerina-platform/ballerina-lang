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
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * This represents a Document Manager for the workspace. Example, an in-memory document manager that keeps dirty files
 * in-memory and will match the collection of files currently open in tool's workspace.
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
     * Usage example:
     *
     * Optional&lt;Lock&gt; lock = Optional.empty();
     * try {
     *     lock = documentManager.openFile(filePath, "");
     * } finally {
     *     lock.ifPresent(Lock:unlock);
     * }
     *
     * @param filePath Path of the file
     * @param content  Content of the file
     * @return {@link Lock} retrieving a lock for the file. You must call Lock.unlock() once you are done with the work.
     */
    Optional<Lock> openFile(Path filePath, String content) throws WorkspaceDocumentException;

    /**
     * Updates given file in document manager with new content.
     *
     * Usage example:
     * <pre>
     * Optional&lt;Lock&gt; lock = Optional.empty();
     * try {
     *     lock = documentManager.updateFile(filePath, "");
     * } finally {
     *     lock.ifPresent(Lock:unlock);
     * }
     * </pre>
     *
     * @param filePath       Path of the file
     * @param updatedContent New content of the file
     * @return {@link Lock} retrieving a lock for the file. You must call Lock.unlock() once you are done with the work.
     */
    Optional<Lock> updateFile(Path filePath, String updatedContent) throws WorkspaceDocumentException;

    /**
     * Close the given file in document manager.
     *
     * @param filePath Path of the file
     */
    void closeFile(Path filePath) throws WorkspaceDocumentException;

    /**
     * Returns the content of the file.
     *
     * @param filePath Path of the file
     * @return Content of the file
     */
    String getFileContent(Path filePath) throws WorkspaceDocumentException;

    /**
     * Acquire a file lock.
     *
     * Usage example:
     * <pre>
     * Optional&lt;Lock&gt; lock = documentManager.lockFile(filePath);
     * try {
     *     //your code
     * } finally {
     *     lock.ifPresent(Lock:unlock);
     * }
     * </pre>
     *
     * @param filePath Path of the file
     * @return {@link Lock} retrieving a lock for the file. You must call Lock.unlock() once you are done with the work.
     */
    Optional<Lock> lockFile(Path filePath);

    /**
     * Returns a list of all file paths.
     * @return set of {@link Path}
     */
    Set<Path> getAllFilePaths();

    /**
     * Clear all file paths.
     *
     */
    void clearAllFilePaths();
}
