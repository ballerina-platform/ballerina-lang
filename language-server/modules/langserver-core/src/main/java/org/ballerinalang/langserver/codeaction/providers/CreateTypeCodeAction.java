/*
 *  Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code action to create a record for an unknown type.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateTypeCodeAction implements DiagnosticBasedCodeActionProvider {

    private static final int DIAG_PROP_UNKNOWN_TYPE_NAME_INDEX = 0;

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        // Validate diagnostic code and syntax correctness. 
        // Additionally, we don't support creating types in other modules (qualified name references)
        return DiagnosticErrorCode.UNKNOWN_TYPE.diagnosticId().equals(diagnostic.diagnosticInfo().code()) &&
                positionDetails.matchedNode().kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE &&
                CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Optional<Name> name = positionDetails.diagnosticProperty(DIAG_PROP_UNKNOWN_TYPE_NAME_INDEX);
        Range diagRange = PathUtil.getRange(diagnostic.location());
        Optional<NonTerminalNode> node = context.currentSyntaxTree()
                .map(syntaxTree -> CommonUtil.findNode(diagRange, syntaxTree));
        if (node == null || node.isEmpty() || name.isEmpty()) {
            return Collections.emptyList();
        }

        Node tlNode = node.get();
        boolean isReturnType = false;
        while (tlNode.parent().kind() != SyntaxKind.MODULE_PART) {
            if (tlNode.kind() == SyntaxKind.RETURN_TYPE_DESCRIPTOR) {
                isReturnType = true;
            }
            tlNode = tlNode.parent();
        }

        Range range = new Range(PositionUtil.toPosition(tlNode.lineRange().startLine()),
                PositionUtil.toPosition(tlNode.lineRange().startLine()));
        String paddingStr = StringUtils.repeat(" ", 4);

        // Here, we create a closed record if the unknown type is in the return type descriptor.
        // This is to be aligned with the open-by-default principle where we become conservative 
        // on what we send (return)
        StringBuilder sb = new StringBuilder();
        sb.append("type ").append(name.get())
                .append(" record {").append(isReturnType ? "|" : "")
                .append(CommonUtil.LINE_SEPARATOR);
        sb.append(paddingStr).append(CommonUtil.LINE_SEPARATOR);
        sb.append(isReturnType ? "|" : "").append("};").append(CommonUtil.LINE_SEPARATOR);
        sb.append(CommonUtil.LINE_SEPARATOR);

        String title = String.format("Create record '%s'", name.get());
        CodeAction codeAction = CodeActionUtil.createCodeAction(title, List.of(new TextEdit(range, sb.toString())),
                context.fileUri(), CodeActionKind.QuickFix);
        return List.of(codeAction);
    }

    @Override
    public String getName() {
        return "CreateType";
    }
}
