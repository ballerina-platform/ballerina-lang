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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for adding access modifiers.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddAccessModifierCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Add Access Modifier";

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic, 
                                                    DiagBasedPositionDetails positionDetails, 
                                                    CodeActionContext context) {
        if (!(DiagnosticErrorCode.MAIN_SHOULD_BE_PUBLIC.diagnosticId().equals(diagnostic.diagnosticInfo().code()))) {
            return Collections.emptyList();
        }
        
        Optional<FunctionDefinitionNode> funcDef = CodeActionUtil.getEnclosedFunction(positionDetails.matchedNode());
        if (funcDef.isEmpty()) {
            return Collections.emptyList();
        }
        Position funcBodyStart = CommonUtil.toPosition(funcDef.get().functionKeyword().lineRange().startLine());
        
        String editText = "public ";
        List<TextEdit> edits = Arrays.asList(new TextEdit(new Range(funcBodyStart, funcBodyStart), editText));
        String commandTitle = CommandConstants.CONVERT_FUNCTION_TO_PUBLIC;
        List<CodeAction> codeActions = Arrays.asList(createCodeAction(commandTitle, edits, context.fileUri(),
                CodeActionKind.QuickFix));
        return codeActions;
    }

    @Override
    public String getName() {
        return NAME; 
    }
}
