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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutor;
import io.ballerina.projects.plugins.codeaction.CodeActionProvider;
import io.ballerina.projects.plugins.codeaction.ToolingCodeActionContext;

import java.util.Collections;
import java.util.List;

/**
 * A dummy code action provider.
 */
public class AddResourceMethodCodeAction extends CodeActionProvider {

    @Override
    public List<CodeAction> getNodeBasedCodeActions(ToolingCodeActionContext context, Node nodeAtCursor) {
        if (nodeAtCursor.kind() != SyntaxKind.SERVICE_DECLARATION) {
            return Collections.emptyList();
        }

        ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) nodeAtCursor;

        int cursorPosition = context.currentDocument().textDocument().textPositionFrom(context.cursorPosition());
        if (serviceDeclarationNode.openBraceToken().textRange().endOffset() <= cursorPosition &&
                serviceDeclarationNode.closeBraceToken().textRange().startOffset() >= cursorPosition) {

            List<CodeActionExecutor.CommandArg> args = List.of(CodeActionExecutor.CommandArg.from(AddResourceMethodExecutor.ARG_NODE_RANGE, nodeAtCursor.lineRange()));
            return Collections.singletonList(CodeAction.from("Add resource method", AddResourceMethodExecutor.COMMAND, args));
        }

        return Collections.emptyList();
    }
}
