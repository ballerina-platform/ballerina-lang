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

import io.ballerinalang.compiler.syntax.tree.BasicLiteralNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluationUtils;

/**
 * Basic literal evaluator implementation.
 *
 * @since 2.0.0
 */
public class BasicLiteralEvaluator extends Evaluator {

    private final BasicLiteralNode syntaxNode;
    private final String literalString;

    public BasicLiteralEvaluator(SuspendedContext context, BasicLiteralNode node) {
        super(context);
        this.syntaxNode = node;
        this.literalString = node.toSourceCode().trim();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        SyntaxKind basicLiteralKind = syntaxNode.kind();
        switch (basicLiteralKind) {
            // int literal
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
                return EvaluationUtils.make(context, Long.parseLong(literalString));
            // float literal
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                return EvaluationUtils.make(context, Double.parseDouble(literalString));
            // boolean literal
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
                return EvaluationUtils.make(context, Boolean.parseBoolean(literalString));
            // string literal
            case STRING_LITERAL:
                return EvaluationUtils.make(context, literalString);
            default:
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "Unsupported basic literal detected: " + literalString));
        }
    }
}
