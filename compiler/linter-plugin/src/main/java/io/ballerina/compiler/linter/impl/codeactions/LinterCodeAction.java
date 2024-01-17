/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.linter.impl.codeactions;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;

import java.util.List;

/**
 * Base class for LinterCodeActions.
 *
 * @since 2.0.0
 */
public abstract class LinterCodeAction implements CodeAction {

    static NonTerminalNode getNodeFromDiagnostics(CodeActionContext context) {
        final LineRange lineRange = context.diagnostic().location().lineRange();
        SyntaxTree syntaxTree = context.currentDocument().syntaxTree();
        TextDocument textDocument = syntaxTree.textDocument();

        int start = textDocument.textPositionFrom(lineRange.startLine());
        int endOffset = textDocument.textPositionFrom(lineRange.endLine());
        return ((ModulePartNode) syntaxTree.rootNode()).findNode(TextRange.from(start, endOffset - start), true);
    }

    static DocumentEdit getDocumentEdit(CodeActionExecutionContext context, LineRange lineRange, String updatedText) {
        SyntaxTree syntaxTree = context.currentDocument().syntaxTree();
        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(lineRange.startLine());
        int endOffset = textDocument.textPositionFrom(lineRange.endLine());

        TextEdit edit = TextEdit.from(TextRange.from(start, endOffset - start), updatedText);
        TextDocumentChange textDocumentChange = TextDocumentChange.from(List.of(edit).toArray(new TextEdit[0]));
        TextDocument modified = syntaxTree.textDocument().apply(textDocumentChange);
        return new DocumentEdit(context.fileUri(), SyntaxTree.from(modified));
    }
}
