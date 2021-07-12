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

import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * On fail suggestion Code Action for check expressions.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddOnFailCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Add on-fail clause";

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {

        if (!(DiagnosticErrorCode.CHECKED_EXPR_NO_MATCHING_ERROR_RETURN_IN_ENCL_INVOKABLE.diagnosticId()
                .equals(diagnostic.diagnosticInfo().code()))) {
            return Collections.emptyList();
        }

        CheckExpressionNode checkExpressionNode = (CheckExpressionNode) positionDetails.matchedNode();
        
        if (isDoStmtNodePresent(positionDetails.matchedNode())) {
            DoStatementNode doStatementNode = getDoStatementNode(checkExpressionNode);
            LinePosition doStmtLinePosition = doStatementNode.lineRange().endLine();
            Position onFailPosLine = new Position(doStmtLinePosition.line(), doStmtLinePosition.offset());
            
            String spaces = " ".repeat(doStmtLinePosition.offset() - 1);
            String editText = " on fail var e {" + CommonUtil.LINE_SEPARATOR + spaces + "\t"
                + CommonUtil.LINE_SEPARATOR + spaces + "}";
            List<TextEdit> edits = Arrays.asList(new TextEdit(new Range(onFailPosLine, onFailPosLine), editText));
            String commandTitle = CommandConstants.CREATE_ON_FAIL_CLAUSE;
            List<CodeAction> codeActions =
                    Arrays.asList(createQuickFixCodeAction(commandTitle, edits, context.fileUri()));
            return codeActions;
        } else {
            VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) checkExpressionNode.parent();
            LinePosition linePosition = variableDeclarationNode.lineRange().startLine();
            Position positionDo = new Position(linePosition.line(), linePosition.offset());
            Position posCheckLineStart = new Position(linePosition.line() + 1, 0);
            
            String spaces = " ".repeat(linePosition.offset());
            String insideDoStatement = variableDeclarationNode.toString();
            String editTextDo = "do {" + CommonUtil.LINE_SEPARATOR + "\t" + insideDoStatement 
                    + spaces + "} on fail var e {" + CommonUtil.LINE_SEPARATOR + spaces + "\t"
                    + CommonUtil.LINE_SEPARATOR + spaces + "}" + CommonUtil.LINE_SEPARATOR;
            List<TextEdit> edits = Arrays.asList(new TextEdit(new Range(positionDo, posCheckLineStart), editTextDo));
            String commandTitle = CommandConstants.CREATE_ON_FAIL_WITH_DO;
            List<CodeAction> codeActions =
                    Arrays.asList(createQuickFixCodeAction(commandTitle, edits, context.fileUri()));
            return codeActions;
        }
    }   
    
    private boolean isDoStmtNodePresent (Node node) {
        if (node == null) {
            return false;
        } else if (node.kind() == SyntaxKind.DO_STATEMENT) {
            return true;
        } else {
            return isDoStmtNodePresent(node.parent());
        }
    }

    private DoStatementNode getDoStatementNode(Node node) {
        if (node.kind() == SyntaxKind.DO_STATEMENT) {
            return (DoStatementNode) node;
        } else {
            return getDoStatementNode(node.parent());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
