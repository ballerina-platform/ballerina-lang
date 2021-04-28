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
package io.ballerina.plugins.codeaction;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutor;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.projects.plugins.codeaction.ToolingCodeActionContext;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;

import java.util.Collections;
import java.util.List;

public class AddResourceMethodExecutor implements CodeActionExecutor {

    public static final String COMMAND = "ADD_RESOURCE_METHOD";
    public static final String ARG_NODE_RANGE = "node.range";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public List<DocumentEdit> execute(ToolingCodeActionContext context, List<CodeActionExecutor.CommandArg> arguments) {
        LineRange lineRange = null;
        for (CommandArg argument : arguments) {
            if (ARG_NODE_RANGE.equals(argument.key())) {
                lineRange = argument.valueAs(LineRange.class);
            }
        }

        if (lineRange == null) {
            return Collections.emptyList();
        }

        SyntaxTree syntaxTree = context.currentDocument().syntaxTree();
        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(lineRange.startLine());
        int end = textDocument.textPositionFrom(lineRange.endLine());
        NonTerminalNode nodeAtCursor = ((ModulePartNode) syntaxTree.rootNode()).findNode(TextRange.from(start, end - start), true);

        if (nodeAtCursor.kind() == SyntaxKind.SERVICE_DECLARATION) {
            ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) nodeAtCursor;

            int position;
            if (!serviceDeclarationNode.members().isEmpty()) {
                Node lastMember = serviceDeclarationNode.members().get(serviceDeclarationNode.members().size() - 1);
                LinePosition linePosition = lastMember.lineRange().endLine();
                position = textDocument.textPositionFrom(lastMember.lineRange().endLine());
            } else {
                position = textDocument.textPositionFrom(serviceDeclarationNode.openBraceToken().lineRange().endLine());
            }

            TextEdit edit = TextEdit.from(TextRange.from(position, 0), "\n\n\tresource function get path() {\n\t\n\t}\n");
            TextDocumentChange textDocumentChange = TextDocumentChange.from(List.of(edit).toArray(new TextEdit[0]));
            TextDocument modified = syntaxTree.textDocument().apply(textDocumentChange);
            DocumentEdit documentEdit = new DocumentEdit(context.fileUri(), syntaxTree, SyntaxTree.from(modified));
            return List.of(documentEdit);
        }

        return Collections.emptyList();
    }
}
