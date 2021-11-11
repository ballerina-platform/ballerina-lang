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

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents the common class for the default Ballerina Code Action Providers.
 *
 * @since 1.1.1
 */
public abstract class AbstractCodeActionProvider implements LSCodeActionProvider {

    protected List<CodeActionNodeType> codeActionNodeTypes;
    protected boolean isNodeTypeBased;

    @Override
    public boolean isEnabled(LanguageServerContext serverContext) {
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
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context,
                                                    NodeBasedPositionDetails posDetails) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
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
        action.setDiagnostics(CodeActionUtil.toDiagnostics(diagnostics));
        return action;
    }

    /**
     * Get a particular diagnostic property given the name.
     * @param diagnosticCode Diagnostic property
     * @param positionDetails Diagnostic position details.
     * @param propertyName Property name.
     * @return Value of the property.
     */
    public static <T> Optional<T> diagnosticProperty(String diagnosticCode, DiagBasedPositionDetails positionDetails,
                                                     DiagnosticPropertyKey propertyName) {
        Optional<Integer> index = getPropertyIndex(diagnosticCode, propertyName);
        if (index.isEmpty()) {
            return Optional.empty();
        }
        return positionDetails.diagnosticProperty(index.get());
    }

    private static Optional<Integer> getPropertyIndex(String diagnosticCode, DiagnosticPropertyKey propertyName) {
        switch (propertyName) {
            case DIAG_PROP_INCOMPATIBLE_TYPES_FOUND:
                if ("BCE2066".equals(diagnosticCode)) {
                    return Optional.of(1);
                } else if ("BCE2068".equals(diagnosticCode)) {
                    return Optional.of(2);
                }
                break;
            case DIAG_PROP_INCOMPATIBLE_TYPES_EXPECTED:
                if ("BCE2066".equals(diagnosticCode) || "BCE2068".equals(diagnosticCode)) {
                    return Optional.of(0);
                }
                break;
            default:
                return Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * Diagnostic Property Names.
     */
    public enum DiagnosticPropertyKey {
        DIAG_PROP_INCOMPATIBLE_TYPES_EXPECTED,
        DIAG_PROP_INCOMPATIBLE_TYPES_FOUND
    }

}
