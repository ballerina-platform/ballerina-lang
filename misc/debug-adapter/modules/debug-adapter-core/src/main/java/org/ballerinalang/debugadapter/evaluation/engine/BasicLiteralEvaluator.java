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

package org.ballerinalang.debugadapter.evaluation.engine;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluationUtils;

/**
 * Evaluator implementation for Basic literals.
 *
 * @since 2.0.0
 */
public class BasicLiteralEvaluator extends Evaluator {

    private final Node syntaxNode;
    private final String literalString;

    public BasicLiteralEvaluator(SuspendedContext context, BasicLiteralNode node) {
        super(context);
        this.syntaxNode = node;
        this.literalString = node.toSourceCode().trim();
    }

    public BasicLiteralEvaluator(SuspendedContext context, NilLiteralNode node) {
        super(context);
        this.syntaxNode = node;
        this.literalString = node.toSourceCode().trim();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        SyntaxKind basicLiteralKind = syntaxNode.kind();
        switch (basicLiteralKind) {
            case NIL_LITERAL:
                return new BExpressionValue(context, null);
            case NUMERIC_LITERAL:
                SyntaxKind literalTokenKind = ((BasicLiteralNode) syntaxNode).literalToken().kind();
                if (literalTokenKind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN) {
                    // int literal
                    // Todo - Add hex int literal support
                    return EvaluationUtils.make(context, Long.parseLong(literalString));
                } else {
                    // float literal
                    // Todo - Add hex float literal support
                    return EvaluationUtils.make(context, Double.parseDouble(literalString));
                }
            case BOOLEAN_LITERAL:
                return EvaluationUtils.make(context, Boolean.parseBoolean(literalString));
            case STRING_LITERAL:
                return EvaluationUtils.make(context, literalString);
            default:
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "Unsupported basic literal detected: " + literalString));
        }
    }
}
