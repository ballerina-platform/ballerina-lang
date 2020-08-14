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
package org.ballerinalang.datamapper;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.ballerinalang.datamapper.AIDataMapperCodeActionUtil.getAIDataMapperCodeActionEdits;
import static org.ballerinalang.langserver.util.references.ReferencesUtil.getReferenceAtCursor;

/**
 * Code Action provider for automatic data mapping.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AIDataMapperCodeAction extends AbstractCodeActionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        WorkspaceDocumentManager documentManager = lsContext.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(lsContext.get(DocumentServiceKeys.FILE_URI_KEY));
        try {
            if (!filePath.isPresent()) {
                return actions;
            }
            LSDocumentIdentifier document = documentManager.getLSDocument(filePath.get());
            for (Diagnostic diagnostic : diagnosticsOfRange) {
                if (diagnostic.getMessage().toLowerCase(Locale.ROOT).contains(CommandConstants.INCOMPATIBLE_TYPES)) {
                    getAIDataMapperCommand(document, diagnostic, lsContext).map(actions::add);
                }
            }
        } catch (WorkspaceDocumentException e) {
            // ignore
        }
        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> allDiagnostics) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return LSClientConfigHolder.getInstance().getConfig().getDataMapper().isEnabled();
    }

    /**
     * Return data mapping code action.
     *
     * @param document   {@link LSDocumentIdentifier}
     * @param diagnostic {@link Diagnostic}
     * @param context    {@link LSContext}
     * @return data mapper code action
     */
    private static Optional<CodeAction> getAIDataMapperCommand(LSDocumentIdentifier document, Diagnostic diagnostic,
                                                               LSContext context) {
        Position startingPosition = diagnostic.getRange().getStart();
        Position endingPosition = diagnostic.getRange().getEnd();
        try {
            Position diagnosticPosition;
            if (endingPosition.getCharacter() - startingPosition.getCharacter() > 1) {
                diagnosticPosition = new Position(startingPosition.getLine(),
                        (startingPosition.getCharacter() + endingPosition.getCharacter()) / 2);
            } else {
                diagnosticPosition = startingPosition;
            }
            SymbolReferencesModel.Reference refAtCursor = getReferenceAtCursor(context, document, diagnosticPosition);
            BType symbolAtCursorType = refAtCursor.getSymbol().type;
            if (refAtCursor.getbLangNode().parent instanceof BLangFieldBasedAccess) {
                return Optional.empty();
            }
            if (symbolAtCursorType instanceof BRecordType) {
                CodeAction action = new CodeAction("Generate mapping function");
                action.setKind(CodeActionKind.QuickFix);

                String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
                List<TextEdit> fEdits = getAIDataMapperCodeActionEdits(context, refAtCursor, diagnostic);
                action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                        new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), fEdits)))));
                action.setDiagnostics(new ArrayList<>());
                return Optional.of(action);
            }
        } catch (CompilationFailedException | WorkspaceDocumentException | IOException |
                TokenOrSymbolNotFoundException e) {
            // ignore
        }
        return Optional.empty();
    }
}

