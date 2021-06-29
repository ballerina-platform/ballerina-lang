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

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A dummy code action provider.
 */
public class CreateVarCodeAction implements CodeAction {

    public static final String ARG_NODE_RANGE = "node.range";
    public static final String ARG_VAR_TYPE = "var.type";
    public static final String VAR_ASSIGNMENT_REQUIRED = "variable assignment is required";

    @Override
    public List<String> supportedDiagnosticCodes() {
        return List.of("BCE2526");
    }

    @Override
    public Optional<CodeActionInfo> codeActionInfo(CodeActionContext context) {
        if (!(context.diagnostic().message().startsWith(VAR_ASSIGNMENT_REQUIRED))) {
            return Optional.empty();
        }

        if (context.diagnostic().properties().isEmpty()) {
            return Optional.empty();
        }

        TypeSymbol typeSymbol = ((DiagnosticProperty<TypeSymbol>) context.diagnostic().properties().get(0)).value();
        if (typeSymbol == null) {
            return Optional.empty();
        }

        List<CodeActionArgument> args = List.of(
                CodeActionArgument.from(ARG_NODE_RANGE, context.diagnostic().location().lineRange()),
                CodeActionArgument.from(ARG_VAR_TYPE, typeSymbol.signature())
        );
        return Optional.of(CodeActionInfo.from("Introduce Variable", args));
    }

    @Override
    public List<DocumentEdit> execute(CodeActionExecutionContext context) {
        LineRange lineRange = null;
        String type = null;
        for (CodeActionArgument argument : context.arguments()) {
            switch (argument.key()) {
                case ARG_NODE_RANGE:
                    lineRange = argument.valueAs(LineRange.class);
                    break;
                case ARG_VAR_TYPE:
                    type = argument.valueAs(String.class);
                    break;
                default:
                    // Nothing
            }
        }

        if (lineRange == null || type == null) {
            return Collections.emptyList();
        }

        SyntaxTree syntaxTree = context.currentDocument().syntaxTree();
        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(lineRange.startLine());

        TextEdit edit = TextEdit.from(TextRange.from(start, 0), type + " myVar = ");
        TextDocumentChange textDocumentChange = TextDocumentChange.from(List.of(edit).toArray(new TextEdit[0]));
        TextDocument modified = syntaxTree.textDocument().apply(textDocumentChange);
        DocumentEdit documentEdit = new DocumentEdit(context.fileUri(), SyntaxTree.from(modified));
        return List.of(documentEdit);
    }

    @Override
    public String name() {
        return "CREATE_VAR";
    }
}
