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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.FailStatementResolver;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        if (!(DiagnosticWarningCode.CHECKED_EXPR_NO_MATCHING_ERROR_RETURN_IN_ENCL_INVOKABLE.diagnosticId()
                .equals(diagnostic.diagnosticInfo().code())) &&
                !(DiagnosticErrorCode.FAIL_EXPR_NO_MATCHING_ERROR_RETURN_IN_ENCL_INVOKABLE.diagnosticId()
                        .equals(diagnostic.diagnosticInfo().code()))) {
            return Collections.emptyList();
        }

        Node nodeAtDiagnostic = positionDetails.matchedNode();
        List<TextEdit> edits = new ArrayList<>();
        String commandTitle;

        FailStatementResolver finder = new FailStatementResolver(diagnostic);
        Optional<Node> failStatementResolverNode = finder.getRegularCompoundStatementNode(nodeAtDiagnostic);
        
        if (failStatementResolverNode.isPresent()
                && failStatementResolverNode.get().kind() == SyntaxKind.WHILE_KEYWORD) {
            while (nodeAtDiagnostic.kind() == SyntaxKind.WHILE_STATEMENT) {
                nodeAtDiagnostic = nodeAtDiagnostic.parent();
            }
            edits.add(getSurroundWithOnFailEditText(nodeAtDiagnostic));
            commandTitle = CommandConstants.SURROUND_WITH_DO_ON_FAIL;
            return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, context.fileUri()));
        }
        
        if (failStatementResolverNode.isPresent()) {
            LinePosition regularCompoundStmtLinePosition = failStatementResolverNode.get().lineRange().endLine();
            Position onFailPosLine = new Position(regularCompoundStmtLinePosition.line(), 
                    regularCompoundStmtLinePosition.offset());

            String spaces = " ".repeat(regularCompoundStmtLinePosition.offset() - 1);
            String editText = " on fail var e {" + CommonUtil.LINE_SEPARATOR + spaces + "\t"
                    + CommonUtil.LINE_SEPARATOR + spaces + "}";
            edits.add(new TextEdit(new Range(onFailPosLine, onFailPosLine), editText));
            commandTitle = CommandConstants.CREATE_ON_FAIL_CLAUSE;
        } else {
            edits.add(getSurroundWithOnFailEditText(nodeAtDiagnostic));
            commandTitle = CommandConstants.SURROUND_WITH_DO_ON_FAIL;
        }

        return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, context.fileUri()));
    }
 
    public TextEdit getSurroundWithOnFailEditText(Node node) {
        while (!StatementNode.class.isAssignableFrom(node.getClass())) {
            node = node.parent();
        }
        LinePosition startLinePosition = node.lineRange().startLine();
        LinePosition endLinePosition = node.lineRange().endLine();
        Position positionDo = new Position(startLinePosition.line(), startLinePosition.offset());
        Position posCheckLineStart = new Position(endLinePosition.line(), endLinePosition.offset());

        String spaces = " ".repeat(startLinePosition.offset());
        String insideDoStatement = node.toSourceCode();
        String tabShiftedDoStatement = insideDoStatement.substring(0, insideDoStatement.length() - 1)
                .replace("\n", "\n\t") + "\n";
        String editTextDo = "do {" + CommonUtil.LINE_SEPARATOR + "\t"
                + tabShiftedDoStatement + spaces
                + "} on fail var e {" + CommonUtil.LINE_SEPARATOR + spaces + "\t"
                + CommonUtil.LINE_SEPARATOR + spaces + "}";
        TextEdit textEdit = new TextEdit(new Range(positionDo, posCheckLineStart), editTextDo);
        return textEdit;
    }
    
    @Override
    public String getName() {
        return NAME;
    }
}
