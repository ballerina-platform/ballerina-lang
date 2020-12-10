/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the common class for the default Ballerina Code Action Providers.
 *
 * @since 1.1.1
 */
public abstract class AbstractCodeActionProvider implements LSCodeActionProvider {
    private List<CodeActionNodeType> codeActionNodeTypes;
    private final boolean isNodeTypeBased;

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Create a diagnostic based code action provider.
     */
    public AbstractCodeActionProvider() {
        this.isNodeTypeBased = false;
    }

    /**
     * Create a node type based code action provider.
     *
     * @param nodeTypes code action node types list
     */
    public AbstractCodeActionProvider(List<CodeActionNodeType> nodeTypes) {
        this.codeActionNodeTypes = nodeTypes;
        this.isNodeTypeBased = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic, CodeActionContext context) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isNodeBasedSupported() {
        return this.isNodeTypeBased;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isDiagBasedSupported() {
        return !this.isNodeTypeBased;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<CodeActionNodeType> getCodeActionNodeTypes() {
        return this.codeActionNodeTypes;
    }

    /**
     * Returns a QuickFix Code action.
     *
     * @param commandTitle title of the code action
     * @param uri          uri
     * @return {@link CodeAction}
     */
    public static CodeAction createQuickFixCodeAction(String commandTitle, List<TextEdit> edits, String uri) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        CodeAction action = new CodeAction(commandTitle);
        action.setKind(CodeActionKind.QuickFix);
        action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), edits)))));
        action.setDiagnostics(diagnostics);
        return action;
    }

    /**
     * Allows convenient transformation of ImportDeclarationNode node model representation for org-name, module-name,
     * version and alias.
     */
    protected static class ImportModel {
        private static final String ORG_SEPARATOR = "/";
        protected final String orgName;
        protected final String moduleName;
        protected final String version;
        protected final String alias;

        private ImportModel(String orgName, String moduleName, String alias, String version) {
            this.orgName = orgName;
            this.moduleName = moduleName;
            this.alias = alias;
            this.version = version;
        }

        static ImportModel from(ImportDeclarationNode importPkg) {
            String orgName = importPkg.orgName().isPresent() ?
                    importPkg.orgName().get().orgName().text() + ORG_SEPARATOR : "";
            StringBuilder pkgNameBuilder = new StringBuilder();
            importPkg.moduleName().forEach(name -> pkgNameBuilder.append(name.text()));
            String pkgName = pkgNameBuilder.toString();
            String alias = importPkg.prefix().isEmpty() ? "" : importPkg.prefix().get().prefix().text();
            return new ImportModel(orgName, pkgName, alias, "");
        }
    }
}
