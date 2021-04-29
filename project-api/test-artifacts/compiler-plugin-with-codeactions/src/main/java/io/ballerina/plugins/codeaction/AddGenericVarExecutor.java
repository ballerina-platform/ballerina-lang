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

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutor;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.projects.plugins.codeaction.ToolingCodeActionContext;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;

import java.util.Collections;
import java.util.List;

public class AddGenericVarExecutor implements CodeActionExecutor {

    public static final String COMMAND = "ADD_VAR";
    public static final String ARG_NODE_RANGE = "node.range";
    public static final String ARG_VAR_TYPE = "var.type";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public List<DocumentEdit> execute(ToolingCodeActionContext context, List<CommandArg> arguments) {
        LineRange lineRange = null;
        String type = null;
        for (CommandArg argument : arguments) {
            switch (argument.key()) {
                case ARG_NODE_RANGE:
                    lineRange = argument.valueAs(LineRange.class);
                    break;
                case ARG_VAR_TYPE:
                    type = argument.valueAs(String.class);
                    break;
            }
        }

        if (lineRange == null || type == null) {
            return Collections.emptyList();
        }

        SyntaxTree syntaxTree = context.currentDocument().syntaxTree();
        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(lineRange.startLine());
        int end = textDocument.textPositionFrom(lineRange.endLine());

        TextEdit edit = TextEdit.from(TextRange.from(start, 0), type + " myVar = ");
        TextDocumentChange textDocumentChange = TextDocumentChange.from(List.of(edit).toArray(new TextEdit[0]));
        TextDocument modified = syntaxTree.textDocument().apply(textDocumentChange);
        DocumentEdit documentEdit = new DocumentEdit(context.fileUri(), syntaxTree, SyntaxTree.from(modified));
        return List.of(documentEdit);
    }
}
