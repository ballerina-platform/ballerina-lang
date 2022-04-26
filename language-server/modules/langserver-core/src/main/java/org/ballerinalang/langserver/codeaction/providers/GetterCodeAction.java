/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.*;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Code Action for getters.
 *
 * @since 2201.1.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class GetterCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Getter";
    public GetterCodeAction() {
        super(Arrays.asList(CodeActionNodeType.OBJECT_FIELD));
    }

    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context,
                                                    NodeBasedPositionDetails posDetails) {

        boolean isInitPresent = false;
        FunctionDefinitionNode initNode = null;
        NonTerminalNode matchedNode = posDetails.matchedCodeActionNode();
        if (!(matchedNode.kind() == SyntaxKind.OBJECT_FIELD) || matchedNode.hasDiagnostics()) {
            return Collections.emptyList();
        }

        ObjectFieldNode objectFieldNode = (ObjectFieldNode) matchedNode;
        if (!isWithinVarName(context, objectFieldNode)) {
            return Collections.emptyList();
        }

        String commandTitle = String.format("Create a getter for '%s'", objectFieldNode.fieldName().toString());
        String fieldName = String.valueOf(objectFieldNode.fieldName());
        String typeName = String.valueOf(objectFieldNode.typeName());
        String functionName = "get" + fieldName.substring(0, 1).toUpperCase(Locale.ROOT) + fieldName.substring(1);
        for (Node node: ((ClassDefinitionNode) objectFieldNode.parent()).members()) {
            if (node.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                if (((FunctionDefinitionNode) node).functionName().toString().equals("init")) {
                    isInitPresent = true;
                    initNode = (FunctionDefinitionNode) node;
                }

                if (((FunctionDefinitionNode) node).functionName().toString().equals(functionName)) {
                    return Collections.emptyList();
                }
            }
        }

        int startLine;
        int startOffset;
        int textOffset;
        if (!isInitPresent) {
            startLine = ((ClassDefinitionNode) objectFieldNode.parent()).
                    members().get(((ClassDefinitionNode) objectFieldNode.parent()).members().size() -1).
                    lineRange().endLine().line();
            startOffset = ((ClassDefinitionNode) objectFieldNode.parent()).
                    members().get(((ClassDefinitionNode) objectFieldNode.parent()).members().size() -1).
                    lineRange().endLine().offset();
            textOffset = objectFieldNode.lineRange().startLine().offset();
        } else {
            startLine = initNode.lineRange().endLine().line();
            startOffset = initNode.lineRange().endLine().offset();
            textOffset = initNode.lineRange().startLine().offset();
        }

        Position startPos = new Position(startLine, startOffset);
        Range newTextRange = new Range(startPos, startPos);
        List<TextEdit> edits = CodeActionUtil.addGettersCodeActionEdits(fieldName, newTextRange, textOffset, typeName);
        return Collections.singletonList(createCodeAction(commandTitle, edits, context.fileUri()));
    }

    @Override
    public String getName() {
        return null;
    }

    private boolean isWithinVarName(CodeActionContext context, ObjectFieldNode objectFieldNode) {
         return objectFieldNode.fieldName().lineRange().startLine().offset() <= context.cursorPosition().getCharacter()
                 && context.cursorPosition().getCharacter() <=
                 objectFieldNode.fieldName().lineRange().endLine().offset();
    }
}
