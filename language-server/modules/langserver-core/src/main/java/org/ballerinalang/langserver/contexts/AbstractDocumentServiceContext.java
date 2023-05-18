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
import io.ballerina.compiler.api.symbols.DiagnosticState;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private Map<ImportDeclarationNode, ModuleSymbol> currentDocImportsMap;

    private Module currentModule;

    private SemanticModel currentSemanticModel;

    private Document currentDocument;

    private final LanguageServerContext languageServerContext;

    private CancelChecker cancelChecker;
    
    private static final boolean ON_DEBUG_MODE = System.getenv("BAL_JAVA_DEBUG") != null;

    AbstractDocumentServiceContext(LSOperation operation,
                                   String fileUri,
                                   WorkspaceManager wsManager,
                                   LanguageServerContext serverContext) {
        this.operation = operation;
        this.fileUri = fileUri;
        this.workspaceManager = wsManager;
        this.languageServerContext = serverContext;
        Optional<Path> optFilePath = PathUtil.getPathFromURI(this.fileUri);
        if (optFilePath.isEmpty()) {
            throw new RuntimeException("Invalid file uri: " + this.fileUri);
        }
        this.filePath = optFilePath.get();
    }

    AbstractDocumentServiceContext(LSOperation operation,
                                   String fileUri,
                                   WorkspaceManager wsManager,
                                   LanguageServerContext serverContext,
                                   CancelChecker cancelChecker) {
        this(operation, fileUri, wsManager, serverContext);
        // This is to facilitate the development in debug mode. 
        if (!ON_DEBUG_MODE) {
            this.cancelChecker = cancelChecker;
        }
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
        Optional<SemanticModel> semanticModel;
        if (this.cancelChecker == null) {
            semanticModel = this.workspaceManager.semanticModel(this.filePath);
        } else {
            semanticModel = this.workspaceManager.semanticModel(this.filePath, this.cancelChecker);
        }
        Optional<Document> srcFile = this.workspaceManager.document(filePath);

        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            return Collections.emptyList();
        }

        this.checkCancelled();

        return semanticModel.get().visibleSymbols(srcFile.get(),
                LinePosition.from(position.getLine(),
                        position.getCharacter()), DiagnosticState.VALID, DiagnosticState.REDECLARED);
    }

    @Override
    public WorkspaceManager workspace() {
        return this.workspaceManager;
    }

    @Override
    public List<ImportDeclarationNode> currentDocImports() {
        Optional<Document> document = this.workspace().document(this.filePath);
        if (document.isEmpty()) {
            throw new RuntimeException("Cannot find a valid document");
        }
        return ((ModulePartNode) document.get().syntaxTree().rootNode()).imports().stream()
                .collect(Collectors.toList());
    }

    @Override
    public Map<ImportDeclarationNode, ModuleSymbol> currentDocImportsMap() {
        if (this.currentDocImportsMap == null) {
            this.currentDocImportsMap = new LinkedHashMap<>();
            Optional<Document> document = this.workspace().document(this.filePath);
            if (document.isEmpty()) {
                throw new RuntimeException("Cannot find a valid document");
            }
            Optional<SemanticModel> semanticModel = this.currentSemanticModel();
            if (semanticModel.isEmpty()) {
                throw new RuntimeException("Semantic Model Cannot be Empty");
            }
            ModulePartNode modulePartNode = document.get().syntaxTree().rootNode();
            for (ImportDeclarationNode importDeclaration : modulePartNode.imports()) {
                Optional<Symbol> symbol = semanticModel.get().symbol(importDeclaration);
                if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.MODULE) {
                    continue;
                }
                currentDocImportsMap.put(importDeclaration, (ModuleSymbol) symbol.get());
            }
        }

        return this.currentDocImportsMap;
    }

    @Override
    public Optional<Document> currentDocument() {
        if (this.currentDocument == null) {
            Optional<Document> document;
            if (this.cancelChecker == null) {
                document = this.workspace().document(this.filePath());
            } else {
                document = this.workspace().document(this.filePath(), this.cancelChecker);
            }
            document.ifPresent(value -> this.currentDocument = value);
        }

        return Optional.ofNullable(this.currentDocument);
    }

    @Override
    public Optional<Module> currentModule() {
        if (this.currentModule == null) {
            Optional<Module> module;
            if (this.cancelChecker == null) {
                module = this.workspaceManager.module(this.filePath);
            } else {
                module = this.workspaceManager.module(this.filePath, this.cancelChecker);
            }
            module.ifPresent(value -> this.currentModule = value);
        }

        return Optional.ofNullable(this.currentModule);
    }

    @Override
    public Optional<SemanticModel> currentSemanticModel() {
        if (this.currentSemanticModel == null) {
            Optional<SemanticModel> semanticModel;
            if (this.cancelChecker == null) {
                semanticModel = this.workspaceManager.semanticModel(this.filePath);
            } else {
                semanticModel = this.workspaceManager.semanticModel(this.filePath, this.cancelChecker);
            }
            semanticModel.ifPresent(value -> this.currentSemanticModel = value);
        }

        return Optional.ofNullable(this.currentSemanticModel);
    }

    @Override
    public Optional<SyntaxTree> currentSyntaxTree() {
        if (this.cancelChecker == null) {
            return this.workspaceManager.syntaxTree(this.filePath);
        }

        return this.workspaceManager.syntaxTree(this.filePath, this.cancelChecker);
    }

    @Override
    public LanguageServerContext languageServercontext() {
        return this.languageServerContext;
    }

    @Override
    public boolean isCancelled() {
        return cancelChecker != null && cancelChecker.isCanceled();
    }

    @Override
    public void checkCancelled() {
        if (this.cancelChecker != null) {
            cancelChecker.checkCanceled();
        }
    }

    @Override
    public Optional<CancelChecker> getCancelChecker() {
        return Optional.ofNullable(this.cancelChecker);
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
        protected CancelChecker cancelChecker;

        /**
         * Context Builder constructor.
         *
         * @param lsOperation   LS Operation for the particular invocation
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

        public T withCancelChecker(CancelChecker cancelChecker) {
            this.cancelChecker = cancelChecker;
            return self();
        }

        public DocumentServiceContext build() {
            return new AbstractDocumentServiceContext(
                    this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.serverContext,
                    this.cancelChecker);
        }

        public abstract T self();
    }
}
