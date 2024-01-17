/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.toml.ballerinatoml.completion;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.toml.TomlCompletionContext;
import org.ballerinalang.langserver.commons.toml.common.TomlCommonUtil;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Ballerina toml completion context.
 */
public class BallerinaTomlCompletionContext implements TomlCompletionContext {

    private List<Symbol> visibleSymbols;
    private Map<ImportDeclarationNode, ModuleSymbol> currentDocImportsMap;
    private final LanguageServerContext languageServerContext;
    private final CompletionCapabilities capabilities;
    private final Position cursorPosition;
    private int cursorPosInTree = -1;
    private final LSOperation operation;
    private final Path filePath;
    private final String fileUri;
    private final WorkspaceManager workspaceManager;
    private NonTerminalNode nodeAtCursor;

    BallerinaTomlCompletionContext(CompletionContext context, LanguageServerContext serverContext) {
        this.operation = context.operation();
        this.fileUri = context.fileUri();
        this.workspaceManager = context.workspace();
        this.capabilities = context.getCapabilities();
        this.cursorPosition = context.getCursorPosition();
        this.languageServerContext = serverContext;
        Optional<Path> optFilePath = TomlCommonUtil.getPathFromURI(this.fileUri);
        if (optFilePath.isEmpty()) {
            throw new RuntimeException("Invalid file uri: " + this.fileUri);
        }
        this.filePath = optFilePath.get();
    }

    @Override
    public List<Symbol> visibleSymbols(Position position) {
        if (this.visibleSymbols == null) {
            Optional<SemanticModel> semanticModel = this.workspace().semanticModel(this.filePath());
            Optional<Document> srcFile = this.workspace().document(this.filePath());

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
    public List<ImportDeclarationNode> currentDocImports() {
        Optional<Document> document = this.workspace().document(this.filePath());
        if (document.isEmpty()) {
            throw new RuntimeException("Cannot find a valid document");
        }

        return ((ModulePartNode) document.get().syntaxTree().rootNode()).imports().stream()
                .collect(Collectors.toList());
    }

    @Override
    public Map<ImportDeclarationNode, ModuleSymbol> currentDocImportsMap() {
        Optional<SemanticModel> semanticModel = this.workspace().semanticModel(this.filePath());
        if (semanticModel.isEmpty()) {
            throw new RuntimeException("Semantic Model Cannot be Empty");
        }
        if (this.currentDocImportsMap == null) {
            this.currentDocImportsMap = new LinkedHashMap<>();
            Optional<Document> document = this.workspace().document(this.filePath());
            if (document.isEmpty()) {
                throw new RuntimeException("Cannot find a valid document");
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
        return this.workspace().document(this.filePath());
    }

    @Override
    public Optional<Module> currentModule() {
        return this.workspace().module(this.filePath());
    }

    @Override
    public Optional<SemanticModel> currentSemanticModel() {
        return this.workspace().semanticModel(this.filePath());
    }

    @Override
    public Optional<SyntaxTree> currentSyntaxTree() {
        return this.workspace().syntaxTree(this.filePath());
    }

    /**
     * Get the file path.
     *
     * @return {@link Path} file path
     */
    @NonNull
    public Path filePath() {
        return this.filePath;
    }

    @Override
    public String fileUri() {
        return this.fileUri;
    }

    @Override
    public LSOperation operation() {
        return this.operation;
    }

    @Override
    public void setCursorPositionInTree(int offset) {
        if (this.cursorPosInTree > -1) {
            throw new RuntimeException("Setting the cursor offset more than once is not allowed");
        }
        this.cursorPosInTree = offset;
    }

    @Override
    public int getCursorPositionInTree() {
        return this.cursorPosInTree;
    }

    @Override
    public LanguageServerContext languageServercontext() {
        return this.languageServerContext;
    }

    @Override
    public CompletionCapabilities getCapabilities() {
        return this.capabilities;
    }

    @Override
    public WorkspaceManager workspace() {
        return this.workspaceManager;
    }

    @Override
    public Position getCursorPosition() {
        return this.cursorPosition;
    }

    @Override
    public void setNodeAtCursor(NonTerminalNode nonTerminalNode) {
        if (this.nodeAtCursor != null) {
            throw new RuntimeException("Setting the node more than once is not allowed");
        }
        this.nodeAtCursor = nonTerminalNode;
    }

    @Override
    public Optional<NonTerminalNode> getNodeAtCursor() {
        return Optional.of(this.nodeAtCursor);
    }

    @Override
    public Optional<io.ballerina.toml.syntax.tree.SyntaxTree> getTomlSyntaxTree() {
        return Optional.of(this.workspace().project(filePath()).orElseThrow().currentPackage().ballerinaToml()
                .orElseThrow().tomlDocument().syntaxTree());
    }

    @Override
    public LanguageServerContext getLanguageServerContext() {
        return this.languageServerContext;
    }
}
