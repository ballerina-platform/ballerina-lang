package org.ballerinalang.debugadapter.evaluation;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionEvaluator {

    private EvaluatorBuilder evaluatorBuilder;
    private DebugExpressionCompiler expressionCompiler;
    private final SuspendedContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluator.class);

    public ExpressionEvaluator(SuspendedContext context) {
        this.context = context;
    }

    /**
     * Evaluates a given ballerina expression w.r.t. the debug context.
     */
    public Value evaluate(String expression) {
        try {
            if (expressionCompiler == null) {
                expressionCompiler = new DebugExpressionCompiler(context);
            }
            ExpressionNode compiledExprNode = expressionCompiler.validateAndCompile(expression);
            if (evaluatorBuilder == null) {
                evaluatorBuilder = new EvaluatorBuilder(context);
            }
            Evaluator evaluator = evaluatorBuilder.build(compiledExprNode);
            return evaluator.evaluate().getJdiValue();
        } catch (EvaluationException e) {
            return context.getAttachedVm().mirrorOf(e.getMessage());
        } catch (Exception e) {
            String message = EvaluationExceptionKind.PREFIX + "internal error";
            LOGGER.error(message, e);
            return context.getAttachedVm().mirrorOf(message);
        }
    }
}
