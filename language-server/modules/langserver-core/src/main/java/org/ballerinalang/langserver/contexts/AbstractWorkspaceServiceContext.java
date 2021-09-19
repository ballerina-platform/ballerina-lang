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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.WorkspaceServiceContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Language server workspace service context implementation.
 *
 * @since 2.0.0
 */
public class AbstractWorkspaceServiceContext implements WorkspaceServiceContext {

    private final LSOperation operation;

    private final WorkspaceManager workspaceManager;

    private final LanguageServerContext languageServerContext;

    private final Map<String, List<Symbol>> visibleSymbols = new HashMap<>();

    AbstractWorkspaceServiceContext(LSOperation operation,
                                    WorkspaceManager wsManager,
                                    LanguageServerContext serverContext) {
        this.operation = operation;
        this.workspaceManager = wsManager;
        this.languageServerContext = serverContext;
    }

    @Override
    public LSOperation operation() {
        return this.operation;
    }

    @Override
    public LanguageServerContext languageServercontext() {
        return this.languageServerContext;
    }

    @Override
    public List<Symbol> visibleSymbols(Path filePath, Position position) {
        return visibleSymbols.computeIfAbsent(filePath.toUri().toString(), k -> {
            Optional<SemanticModel> semanticModel = this.workspaceManager.semanticModel(filePath);
            Optional<Document> srcFile = this.workspaceManager.document(filePath);

            if (semanticModel.isEmpty() || srcFile.isEmpty()) {
                return Collections.emptyList();
            }

            return semanticModel.get()
                    .visibleSymbols(srcFile.get(), 
                            LinePosition.from(position.getLine(), position.getCharacter()),
                            DiagnosticState.VALID, DiagnosticState.REDECLARED);
        });
    }

    @Override
    public WorkspaceManager workspace() {
        return this.workspaceManager;
    }

    /**
     * Represents Language server workspace service context Builder.
     *
     * @param <T> builder type
     * @since 2.0.0
     */
    protected abstract static class AbstractContextBuilder<T extends AbstractContextBuilder<T>> {
        protected final LSOperation operation;
        protected WorkspaceManager wsManager;
        protected LanguageServerContext serverContext;

        /**
         * Context Builder constructor.
         *
         * @param lsOperation LS Operation for the particular invocation
         */
        public AbstractContextBuilder(LSOperation lsOperation, LanguageServerContext serverContext) {
            this.operation = lsOperation;
            this.serverContext = serverContext;
        }

        public T withWorkspaceManager(WorkspaceManager workspaceManager) {
            this.wsManager = workspaceManager;
            return self();
        }

        public WorkspaceServiceContext build() {
            return new AbstractWorkspaceServiceContext(this.operation, this.wsManager, this.serverContext);
        }

        public abstract T self();
    }
}
