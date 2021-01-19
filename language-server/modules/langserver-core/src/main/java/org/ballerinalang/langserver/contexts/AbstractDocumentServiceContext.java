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
package org.ballerinalang.langserver.contexts;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Language server context implementation.
 *
 * @since 1.2.0
 */
public class AbstractDocumentServiceContext implements DocumentServiceContext {

    private final LSOperation operation;

    private final Path filePath;

    private final String fileUri;

    private final WorkspaceManager workspaceManager;

    private List<Symbol> visibleSymbols;

    private List<ImportDeclarationNode> currentDocImports;

    private Module currentModule;
    
    private final LanguageServerContext languageServerContext;

    AbstractDocumentServiceContext(LSOperation operation,
                                   String fileUri,
                                   WorkspaceManager wsManager,
                                   LanguageServerContext serverContext) {
        this.operation = operation;
        this.fileUri = fileUri;
        this.workspaceManager = wsManager;
        this.languageServerContext = serverContext;
        Optional<Path> optFilePath = CommonUtil.getPathFromURI(this.fileUri);
        if (optFilePath.isEmpty()) {
            throw new RuntimeException("Invalid file uri: " + this.fileUri);
        }
        this.filePath = optFilePath.get();
    }

    /**
     * Get the file uri.
     *
     * @return {@link String} file uri
     */
    public String fileUri() {
        return this.fileUri;
    }

    /**
     * Get the file path.
     *
     * @return {@link Path} file path
     */
    @Nonnull
    public Path filePath() {
        return this.filePath;
    }

    @Override
    public LSOperation operation() {
        return this.operation;
    }

    @Override
    public List<Symbol> visibleSymbols(Position position) {
        if (this.visibleSymbols == null) {
            Optional<SemanticModel> semanticModel = this.workspaceManager.semanticModel(this.filePath);
            Optional<Document> srcFile = this.workspaceManager.document(filePath);

            if (semanticModel.isEmpty() || srcFile.isEmpty()) {
                return Collections.emptyList();
            }

            visibleSymbols = semanticModel.get().visibleSymbols(srcFile.get(),
                                                                LinePosition.from(position.getLine(),
                                                                                  position.getCharacter()));
        }

        return visibleSymbols;
    }

    @Override
    public WorkspaceManager workspace() {
        return this.workspaceManager;
    }

    @Override
    public List<ImportDeclarationNode> currentDocImports() {
        if (this.currentDocImports == null) {
            Optional<Document> document = this.workspace().document(this.filePath);
            if (document.isEmpty()) {
                throw new RuntimeException("Cannot find a valid document");
            }
            this.currentDocImports = ((ModulePartNode) document.get().syntaxTree().rootNode()).imports().stream()
                    .collect(Collectors.toList());
        }

        return this.currentDocImports;
    }

    @Override
    public Optional<Module> currentModule() {
        if (this.currentModule == null) {
            this.currentModule = this.workspaceManager.module(this.filePath).orElse(null);
        }

        return Optional.ofNullable(this.currentModule);
    }

    @Override
    public LanguageServerContext languageServercontext() {
        return this.languageServerContext;
    }

    /**
     * Represents Language server context Builder.
     *
     * @param <T> builder type
     * @since 2.0.0
     */
    protected abstract static class AbstractContextBuilder<T extends AbstractContextBuilder<T>> {
        protected final LSOperation operation;
        protected final LanguageServerContext serverContext;
        protected String fileUri;
        protected WorkspaceManager wsManager;

        /**
         * Context Builder constructor.
         *
         * @param lsOperation LS Operation for the particular invocation
         * @param serverContext Language server context
         */
        public AbstractContextBuilder(LSOperation lsOperation, LanguageServerContext serverContext) {
            this.operation = lsOperation;
            this.serverContext = serverContext;
        }

        public T withFileUri(String fileUri) {
            this.fileUri = fileUri;
            return self();
        }

        public T withWorkspaceManager(WorkspaceManager workspaceManager) {
            this.wsManager = workspaceManager;
            return self();
        }

        public DocumentServiceContext build() {
            return new AbstractDocumentServiceContext(this.operation, this.fileUri, this.wsManager, this.serverContext);
        }

        public abstract T self();
    }
}
