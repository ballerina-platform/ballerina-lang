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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Language server context implementation.
 *
 * @since 1.2.0
 */
public class CodeActionContextImpl extends AbstractDocumentServiceContext implements CodeActionContext {

    private Position cursorPosition;
    private int cursorPositionInTree = -1;
    private List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics;
    private final CodeActionParams params;
    private Node nodeAtCursor;

    @Deprecated(forRemoval = true)
    public CodeActionContextImpl(LSOperation operation,
                                 String fileUri,
                                 WorkspaceManager wsManager,
                                 CodeActionParams params,
                                 LanguageServerContext serverContext) {
        super(operation, fileUri, wsManager, serverContext);
        this.params = params;
    }

    public CodeActionContextImpl(LSOperation operation,
                                 String fileUri,
                                 WorkspaceManager wsManager,
                                 CodeActionParams params,
                                 LanguageServerContext serverContext,
                                 CancelChecker cancelChecker) {
        super(operation, fileUri, wsManager, serverContext, cancelChecker);
        this.params = params;
    }

    @Override
    public Position cursorPosition() {
        if (this.cursorPosition == null) {
            int line = params.getRange().getStart().getLine();
            int col = params.getRange().getStart().getCharacter();
            this.cursorPosition = new Position(line, col);
        }

        return this.cursorPosition;
    }

    @Override
    public List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics(Path filePath) {
        this.checkCancelled();
        if (this.diagnostics != null) {
            return this.diagnostics;
        }
        PackageCompilation compilation;
        if (this.getCancelChecker().isPresent()) {
            compilation = workspace().waitAndGetPackageCompilation(filePath,
                    this.getCancelChecker().get()).orElseThrow();
        } else {
            compilation = workspace().waitAndGetPackageCompilation(filePath).orElseThrow();
        }
        Project project = this.workspace().project(this.filePath()).orElseThrow();
        Path projectRoot = (project.kind() == ProjectKind.SINGLE_FILE_PROJECT)
                ? project.sourceRoot().getParent() :
                project.sourceRoot();
        this.diagnostics = compilation.diagnosticResult().diagnostics().stream()
                .filter(diag -> projectRoot.resolve(diag.location().lineRange().filePath()).equals(filePath))
                .collect(Collectors.toList());
        return this.diagnostics;
    }

    @Override
    public List<Diagnostic> cursorDiagnostics() {
        return params.getContext().getDiagnostics();
    }

    @Override
    public int cursorPositionInTree() {
        if (this.cursorPositionInTree == -1) {
            Optional<Document> document = this.currentDocument();
            if (document.isEmpty()) {
                return this.cursorPositionInTree;
            }
            TextDocument textDocument = document.get().textDocument();
            Position position = this.cursorPosition();
            this.cursorPositionInTree =
                    textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        }
        return this.cursorPositionInTree;
    }

    /**
     * Represents Language server code action context Builder.
     *
     * @since 2.0.0
     */
    protected static class CodeActionContextBuilder extends AbstractContextBuilder<CodeActionContextBuilder> {

        private final CodeActionParams params;

        public CodeActionContextBuilder(CodeActionParams params,
                                        LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_CODE_ACTION, serverContext);
            this.params = params;
        }

        public CodeActionContext build() {
            return new CodeActionContextImpl(
                    this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.params,
                    this.serverContext,
                    this.cancelChecker);
        }

        @Override
        public CodeActionContextBuilder self() {
            return this;
        }
    }

    @Override
    public Node nodeAtCursor() {
        if (this.nodeAtCursor == null) {
            SyntaxTree syntaxTree = this.currentSyntaxTree().orElseThrow();
            this.nodeAtCursor = CommonUtil.findNode(range(), syntaxTree);
        }
        return this.nodeAtCursor;
    }

    @Override
    public Range range() {
        return this.params.getRange();
    }
}
