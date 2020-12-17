/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleCompilation;
import io.ballerina.projects.Project;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Contains a set of utility methods to manage projects.
 *
 * @since 2.0.0
 */
public interface WorkspaceManager {

    /**
     * Returns a project root from the path provided.
     *
     * @param path ballerina project or standalone file path
     * @return project root
     */
    Path projectRoot(Path path);

    /**
     * Returns project from the path provided.
     *
     * @param filePath ballerina project or standalone file path
     * @return project of applicable type
     */
    Optional<Project> project(Path filePath);

    /**
     * Returns module from the path provided.
     *
     * @param filePath file path of the document
     * @return project of applicable type
     */
    Optional<Module> module(Path filePath);

    /**
     * Returns document of the project of this path.
     *
     * @param filePath file path of the document
     * @return {@link Document}
     */
    Optional<Document> document(Path filePath);

    /**
     * Returns syntax tree from the path provided.
     *
     * @param filePath file path of the document
     * @return {@link io.ballerina.compiler.syntax.tree.SyntaxTree}
     */
    Optional<SyntaxTree> syntaxTree(Path filePath);

    /**
     * Returns semantic model from the path provided.
     *
     * @param filePath file path of the document
     * @return project of applicable type
     */
    Optional<SemanticModel> semanticModel(Path filePath);

    /**
     * Returns module compilation from the file path provided.
     *
     * @param filePath file path of the document
     * @return {@link ModuleCompilation}
     */
    Optional<ModuleCompilation> waitAndGetModuleCompilation(Path filePath);

    /**
     * Returns module compilation from the module provided.
     *
     * @param module {@link Module}
     * @return {@link ModuleCompilation}
     */
    Optional<ModuleCompilation> waitAndGetModuleCompilation(Module module);

    /**
     * The document open notification is sent from the client to the server to signal newly opened text documents.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidOpenTextDocumentParams}
     */
    void didOpen(Path filePath, DidOpenTextDocumentParams params);

    /**
     * The document change notification is sent from the client to the server to signal changes to a text document.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidChangeTextDocumentParams}
     * @throws WorkspaceDocumentException when project or document not found
     */
    void didChange(Path filePath, DidChangeTextDocumentParams params) throws WorkspaceDocumentException;

    /**
     * The document close notification is sent from the client to the server when the document got closed in the
     * client.
     *
     * @param filePath {@link Path} of the document
     * @param params   {@link DidCloseTextDocumentParams}
     * @throws WorkspaceDocumentException project not found
     */
    void didClose(Path filePath, DidCloseTextDocumentParams params) throws WorkspaceDocumentException;
}
