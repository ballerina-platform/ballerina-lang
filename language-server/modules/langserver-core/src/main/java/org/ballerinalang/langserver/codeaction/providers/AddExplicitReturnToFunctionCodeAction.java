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

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code action to add an explicit return statement.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddExplicitReturnToFunctionCodeAction extends AbstractCodeActionProvider {
    private static final String NAME = "Add Explicit Return Statement";
    private static final String DIAG_CODE = "BCE20350";

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!diagnostic.diagnosticInfo().code().equals(DIAG_CODE)) {
            return Collections.emptyList();
        }

        NonTerminalNode currentNode = positionDetails.matchedNode();
        Optional<FunctionDefinitionNode> functionDefinition = getFunctionDefinition(currentNode);
        if (functionDefinition.isEmpty()
                || functionDefinition.get().functionBody().kind() != SyntaxKind.FUNCTION_BODY_BLOCK) {
            return Collections.emptyList();
        }
        FunctionBodyBlockNode functionBody = (FunctionBodyBlockNode) functionDefinition.get().functionBody();
        Token closeBraceToken = functionBody.closeBraceToken();

        Range range = toRange(closeBraceToken.lineRange());
        String newText = getNewText(functionDefinition.get());
        String uri = context.filePath().toUri().toString();

        TextEdit textEdit = new TextEdit();
        textEdit.setRange(range);
        textEdit.setNewText(newText);
        List<TextEdit> textEdits = Collections.singletonList(textEdit);

        return Collections.singletonList(createQuickFixCodeAction(CommandConstants.ADD_EXPLICIT_RETURN_STATEMENT,
                textEdits, uri));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {
        return 999;
    }

    private Range toRange(LineRange lineRange) {
        LinePosition sLine = lineRange.startLine();
        LinePosition eLine = lineRange.endLine();
        Position start = new Position(sLine.line(), sLine.offset());
        Position end = new Position(eLine.line(), eLine.offset());

        return new Range(start, end);
    }

    private Optional<FunctionDefinitionNode> getFunctionDefinition(NonTerminalNode node) {
        NonTerminalNode functionDef = null;
        NonTerminalNode parent = node;
        for (int i = 0; i < 3; i++) {
            // Iterate three parents.
            parent = parent.parent();
            if (parent == null || parent.isMissing()) {
                return Optional.empty();
            }
            functionDef = parent;
        }
        return functionDef.kind() == SyntaxKind.FUNCTION_DEFINITION
                || functionDef.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION
                || functionDef.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION
                ? Optional.of((FunctionDefinitionNode) functionDef)
                : Optional.empty();
    }
    
    private String getNewText(FunctionDefinitionNode functionDef) {
        NodeList<Token> qualifiers = functionDef.qualifierList();
        Token kwFunction = functionDef.functionKeyword();
        // Here we cast to FunctionBodyBlockNode without checking, since before coming to 
        // this point/ using this function, we check
        FunctionBodyBlockNode functionBody = (FunctionBodyBlockNode) functionDef.functionBody();
        Token closeBraceToken = functionBody.closeBraceToken();
        
        LineRange startLineRange;
        if (qualifiers.isEmpty()) {
            startLineRange = kwFunction.lineRange();
        } else {
            startLineRange = qualifiers.get(0).lineRange();
        }
        
        StringBuilder newText = new StringBuilder();
        
        if (startLineRange.startLine().line() == closeBraceToken.lineRange().startLine().line()) {
            // Cursor is in the same line as the function start
            newText.append(CommonUtil.LINE_SEPARATOR)
                    .append(StringUtils.repeat(" ", startLineRange.startLine().offset() + 4))
                    .append("return;")
                    .append(CommonUtil.LINE_SEPARATOR)
                    .append(StringUtils.repeat(" ", startLineRange.startLine().offset()))
                    .append("}");
        } else {
            newText.append(StringUtils.repeat(" ", 4))
                    .append("return;")
                    .append(CommonUtil.LINE_SEPARATOR)
                    .append(StringUtils.repeat(" ", startLineRange.startLine().offset()))
                    .append("}");
        }
        
        return newText.toString();
    }
}
