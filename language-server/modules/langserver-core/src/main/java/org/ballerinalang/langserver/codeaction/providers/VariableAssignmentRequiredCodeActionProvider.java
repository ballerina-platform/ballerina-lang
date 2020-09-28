/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.builder.DiagBasedCodeAction;
import org.ballerinalang.langserver.codeaction.builder.impl.CreateVariableCodeAction;
import org.ballerinalang.langserver.codeaction.builder.impl.ErrorTypeCodeAction;
import org.ballerinalang.langserver.codeaction.builder.impl.IgnoreReturnCodeAction;
import org.ballerinalang.langserver.codeaction.builder.impl.TypeGuardCodeAction;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.ReferencesKeys;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel.Reference;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.ballerinalang.langserver.codeaction.CodeActionUtil.getSymbolAtCursor;

/**
 * Code Action provider for variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class VariableAssignmentRequiredCodeActionProvider extends AbstractCodeActionProvider {

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
        LSDocumentIdentifier document = null;
        try {
            document = documentManager.getLSDocument(filePath.get());
        } catch (WorkspaceDocumentException e) {
            // ignore
        }

        if (document != null) {
            for (Diagnostic diagnostic : diagnosticsOfRange) {
                String diagnosticMsg = diagnostic.getMessage().toLowerCase(Locale.ROOT);
                if (!(diagnosticMsg.contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED))) {
                    continue;
                }
                String diagnosedContent = getDiagnosedContent(diagnostic, lsContext, document);
                Position position = diagnostic.getRange().getStart();
                try {
                    lsContext.put(ReferencesKeys.OFFSET_CURSOR_N_TRY_NEXT_BEST, true);
                    lsContext.put(ReferencesKeys.ENABLE_FIND_LITERALS, true);
                    lsContext.put(ReferencesKeys.DO_NOT_SKIP_NULL_SYMBOLS, true);
                    Position afterAliasPos = offsetPositionToInvocation(diagnosedContent, position);
                    Reference refAtCursor = getSymbolAtCursor(lsContext, document, afterAliasPos);
                    if (refAtCursor == null) {
                        continue;
                    }

                    DiagBasedCodeAction createVariableCodeAction = new CreateVariableCodeAction(refAtCursor);
                    DiagBasedCodeAction errorTypeCodeAction = new ErrorTypeCodeAction(refAtCursor);
                    DiagBasedCodeAction ignoreReturnCodeAction = new IgnoreReturnCodeAction(refAtCursor);
                    DiagBasedCodeAction typeGuardCodeAction = new TypeGuardCodeAction(refAtCursor);

                    // Can result multiple code-actions since RHS is ambiguous
                    actions.addAll(createVariableCodeAction.get(diagnostic, allDiagnostics, lsContext));
                    // Check and CheckPanic
                    actions.addAll(errorTypeCodeAction.get(diagnostic, allDiagnostics, lsContext));
                    actions.addAll(ignoreReturnCodeAction.get(diagnostic, allDiagnostics, lsContext));
                    actions.addAll(typeGuardCodeAction.get(diagnostic, allDiagnostics, lsContext));
                } catch (LSCodeActionProviderException | WorkspaceDocumentException | CompilationFailedException
                        | TokenOrSymbolNotFoundException e) {
                    // ignore
                }
            }
        }
        return actions;
    }
}
