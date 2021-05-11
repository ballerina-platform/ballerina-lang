/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.evaluation;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.debugadapter.SuspendedContext;

/**
 * A ballerina expression-specific implementation for validating, parsing and compiling ballerina expressions.
 *
 * @since 2.0.0
 */
public class DebugExpressionCompiler {

    private final SuspendedContext context;
    private Document document;

    public DebugExpressionCompiler(SuspendedContext context) {
        this.context = context;
    }

    /**
     * Returns module compilation for a given expression by injecting it into the given debugger source.
     *
     * @param expr expression string
     * @return module compilation
     */
    public PackageCompilation getModuleCompilation(String expr) {
        if (document == null) {
            document = context.getDocument();
        }

        if (expr == null || expr.isBlank()) {
            return document.module().packageInstance().getCompilation();
        }

        // As expressions cannot be compiled standalone, coverts into a compilable statement.
        String exprStatement = getExpressionStatement(expr);
        // Injects the expression into the source file content, at the end of the current debug point line.
        int startOffset = document.textDocument().line(context.getLineNumber() - 1).endOffset();
        TextEdit[] textEdit = {TextEdit.from(TextRange.from(startOffset, 0), exprStatement)};
        String newContent = new String(document.textDocument().apply(TextDocumentChange.from(textEdit)).toCharArray());
        Document newDocument = document.modify().withContent(newContent).apply();
        return newDocument.module().packageInstance().getCompilation();
    }

    public SemanticModel getSemanticInfo() {
        if (document == null) {
            document = context.getDocument();
        }
        return document.module().packageInstance().getCompilation().getSemanticModel(document.module().moduleId());
    }

    /**
     * Returns the expression input wrapped inside a statement.
     *
     * @param expression user expression input.
     * @return parsable unit which wraps the expression.
     */
    private static String getExpressionStatement(String expression) {
        // Adds missing semicolon, if required.
        if (!expression.trim().endsWith(SyntaxKind.SEMICOLON_TOKEN.stringValue())) {
            expression += SyntaxKind.SEMICOLON_TOKEN.stringValue();
        }
        // Creates an statement in the format of "_ = <EXPRESSION>" and injects into the existing source, in order to
        // be compilable.
        return "_ = " + expression;
    }
}
