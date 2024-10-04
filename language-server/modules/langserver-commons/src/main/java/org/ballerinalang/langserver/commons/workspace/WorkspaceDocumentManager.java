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
package org.ballerinalang.langserver.commons.workspace;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;

import java.nio.file.Path;
import java.util.List;
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
     * Optional&lt;Lock&gt; lock = documentManager.lockFile(filePath);
     * try {
     *     lock = documentManager.openFile(filePath, "");
     * } finally {
     *     lock.ifPresent(Lock:unlock);
     * }
     *
     * @param filePath Path of the file
     * @param content  Content of the file
     */
    void openFile(Path filePath, String content);

    /**
     * Updates given file in document manager with new content.
     *
     * Usage example:
     * <pre>
     * Optional&lt;Lock&gt; lock = documentManager.lockFile(filePath);
     * try {
     *     documentManager.updateFile(filePath, range, "");
     * } finally {
     *     lock.ifPresent(Lock:unlock);
     * }
     * </pre>
     *
     * @param filePath       Path of the file
     * @param updatedContent New content of the file
     */
    void updateFile(Path filePath, List<TextDocumentContentChangeEvent> updatedContent);

    /**
     * Updates code lenses of a given file in document manager with new code lenses sent to client.
     *
     * Usage example:
     * <pre>
     * Optional&lt;Lock&gt; lock = documentManager.lockFile(filePath);
     * try {
     *     documentManager.setCodeLenses(filePath, lenses);
     * } finally {
     *     lock.ifPresent(Lock:unlock);
     * }
     * </pre>
     *
     * @param filePath Path of the file
     * @param codeLens New code lenses of the file
     */
    void setCodeLenses(Path filePath, List<CodeLens> codeLens);

    /**
     * Returns the code lenses of the file.
     *
     * @param filePath Path of the file
     * @return Code lenses of the file
     */
    List<CodeLens> getCodeLenses(Path filePath);

    /**
     * Returns the LSDocument associated with the file.
     *
     * @param filePath Path of the file
     * @return LSDocument of the file
     */
    LSDocumentIdentifier getLSDocument(Path filePath);

    /**
     * Close the given file in document manager.
     *
     * @param filePath Path of the file
     */
    void closeFile(Path filePath);

    /**
     * Returns the content of the file.
     *
     * @param filePath Path of the file
     * @return Content of the file
     * @deprecated Use #getTree(Path filePath) instead
     */
    @Deprecated
    String getFileContent(Path filePath);

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
     *
     * @return set of {@link Path}
     */
    Set<Path> getAllFilePaths();

    /**
     * Clear all file paths.
     */
    void clearAllFilePaths();

    /**
     * Returns a syntax tree.
     *
     * @param filePath Path of the file
     * @return SyntaxTree
     */
    SyntaxTree getTree(Path filePath);

    /**
     * Set the new tree.
     *
     * @param filePath Path of the file
     * @param newTree  new tree
     */
    void setTree(Path filePath, SyntaxTree newTree);
}
