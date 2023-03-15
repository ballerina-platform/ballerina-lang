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

package org.ballerinalang.debugadapter.evaluation;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.validator.SerialExpressionValidator;

import java.util.Map;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;

/**
 * Ballerina expression evaluator.
 *
 * @since 2.0.0
 */
public class DebugExpressionEvaluator extends Evaluator {

    private final EvaluationContext evaluationContext;
    private String expression;

    public DebugExpressionEvaluator(EvaluationContext context) {
        super(context);
        this.evaluationContext = context;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * Evaluates the given ballerina expression w.r.t. the provided context.
     */
    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // Parses the given string expression.
            SerialExpressionValidator expressionValidator = new SerialExpressionValidator();
            ExpressionNode parsedExpression = expressionValidator.validateAndParse(expression);

            EvaluationImportResolver importResolver = new EvaluationImportResolver(context);
            Map<String, BImport> resolvedImports = importResolver.getAllImports();
            evaluationContext.setResolvedImports(resolvedImports);
            evaluationContext.setExpression(expression);
            // Validates the import prefixes (qualified name references) within the expression.
            importResolver.detectUsedImports(parsedExpression);

            // Uses `ExpressionIdentifierModifier` to modify and encode all the identifiers within the expression.
            parsedExpression = (ExpressionNode) parsedExpression.apply(new IdentifierModifier());

            EvaluatorBuilder evaluatorBuilder = new EvaluatorBuilder(evaluationContext);
            Evaluator evaluator = evaluatorBuilder.build(parsedExpression);
            return evaluator.evaluate();
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException("internal error");
        }
    }
}
