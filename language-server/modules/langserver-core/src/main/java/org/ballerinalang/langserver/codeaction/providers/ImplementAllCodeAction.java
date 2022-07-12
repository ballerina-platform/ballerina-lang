/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.codeaction.CodeActionUtil.computePositionDetails;

/**
 * Code Action provider for implementing all the functions of an object.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ImplementAllCodeAction extends AbstractImplementMethodCodeAction {

    public static final String NAME = "Implement All";

    public ImplementAllCodeAction() {
        super(Arrays.asList(CodeActionNodeType.CLASS,
                CodeActionNodeType.SERVICE,
                CodeActionNodeType.CLASS_FUNCTION,
                CodeActionNodeType.MODULE_VARIABLE,
                CodeActionNodeType.OBJECT_FUNCTION,
                CodeActionNodeType.LOCAL_VARIABLE));
    }

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails, 
                            CodeActionContext context) {
        return super.validate(diagnostic, positionDetails, context);
    }

    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context,
                                                    NodeBasedPositionDetails posDetails) {

        if (posDetails.matchedCodeActionNode().kind() != SyntaxKind.CLASS_DEFINITION
                && posDetails.matchedCodeActionNode().kind() != SyntaxKind.OBJECT_METHOD_DEFINITION
                && posDetails.matchedCodeActionNode().kind() != SyntaxKind.MODULE_VAR_DECL
                && posDetails.matchedCodeActionNode().kind() != SyntaxKind.LOCAL_VAR_DECL
                && posDetails.matchedCodeActionNode().kind() != SyntaxKind.SERVICE_DECLARATION) {
            return Collections.emptyList();
        }

        List<Diagnostic> diags = context.diagnostics(context.filePath()).stream()
                .filter(diag -> PositionUtil
                        .isWithinRange(context.cursorPosition(), PositionUtil.toRange(diag.location().lineRange()))
                )
                .filter(diagnostic -> DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code()))
                .collect(Collectors.toList());
        
        if (diags.isEmpty() || diags.size() == 1) {
            return Collections.emptyList();
        }

        SyntaxTree syntaxTree = context.workspace().syntaxTree(context.filePath()).orElseThrow();
        List<TextEdit> edits = new ArrayList<>();

        diags.forEach(diagnostic -> {
            DiagBasedPositionDetails positionDetails = computePositionDetails(syntaxTree, diagnostic, context);
            edits.addAll(getDiagBasedTextEdits(diagnostic, positionDetails, context));
        });

        CodeAction quickFixCodeAction = createCodeAction(CommandConstants.IMPLEMENT_ALL, edits, context.fileUri(),
                CodeActionKind.QuickFix);
        quickFixCodeAction.setDiagnostics(CodeActionUtil.toDiagnostics(diags));
        return Collections.singletonList(quickFixCodeAction);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
