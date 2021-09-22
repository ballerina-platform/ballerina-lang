package org.ballerinalang.debugadapter.evaluation.engine.expression;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;

/**
 * Anonymous function expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class AnonFunctionExpressionEvaluator extends ExpressionAsProgramEvaluator {

    public AnonFunctionExpressionEvaluator(EvaluationContext context, ExpressionNode anonFunctionExpressionNode) {
        super(context, anonFunctionExpressionNode);
    }
}
