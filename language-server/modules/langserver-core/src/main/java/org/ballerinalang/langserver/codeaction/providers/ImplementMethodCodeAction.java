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
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Code Action for implementing functions of an object.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ImplementMethodCodeAction extends AbstractImplementMethodCodeAction
        implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "Implement Method";

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return CodeActionNodeValidator.validate(context.nodeAtCursor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        List<TextEdit> edits = new ArrayList<>(getDiagBasedTextEdits(diagnostic, positionDetails, context));
        if (!edits.isEmpty()) {
            if (positionDetails.diagnosticProperty(DIAG_PROPERTY_NAME_INDEX).isPresent()) {
                String commandTitle = String.format(CommandConstants.IMPLEMENT_FUNCS_TITLE,
                        positionDetails.diagnosticProperty(DIAG_PROPERTY_NAME_INDEX).get());
                CodeAction quickFixCodeAction = CodeActionUtil.createCodeAction(commandTitle, edits, context.fileUri(),
                        CodeActionKind.QuickFix);
                quickFixCodeAction.setDiagnostics(CodeActionUtil.toDiagnostics(Collections.singletonList(diagnostic)));
                return Collections.singletonList(quickFixCodeAction);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
