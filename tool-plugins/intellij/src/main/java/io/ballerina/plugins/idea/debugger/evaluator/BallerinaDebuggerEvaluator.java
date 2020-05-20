package io.ballerina.plugins.idea.debugger.evaluator;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import io.ballerina.plugins.idea.debugger.BallerinaDebugProcess;
import io.ballerina.plugins.idea.debugger.BallerinaXValue;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateResponse;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Expression evaluator implementation for ballerina debugger.
 */
public class BallerinaDebuggerEvaluator extends XDebuggerEvaluator {
    private final BallerinaDebugProcess process;
    private final StackFrame stackFrame;
    private static final Logger LOG = Logger.getInstance(BallerinaDebuggerEvaluator.class);

    public BallerinaDebuggerEvaluator(BallerinaDebugProcess process, StackFrame frame) {
        this.process = process;
        this.stackFrame = frame;
    }

    @Override
    public void evaluate(@NotNull String expression, @NotNull XEvaluationCallback callback,
                         @Nullable XSourcePosition expressionPosition) {
        try {
            EvaluateArguments arguments = new EvaluateArguments();
            arguments.setFrameId(stackFrame.getId());
            arguments.setExpression(expression);
            EvaluateResponse response = process.getDapClientConnector().getRequestManager().evaluate(arguments);
            if (response.getResult() != null) {
                Variable variable = new Variable();
                variable.setName(expression);
                variable.setValue(response.getResult());
                variable.setType(response.getType());
                variable.setVariablesReference(response.getVariablesReference());
                variable.setNamedVariables(response.getNamedVariables());
                variable.setIndexedVariables(response.getIndexedVariables());
                callback.evaluated(new BallerinaXValue(process, variable, AllIcons.Nodes.Field));
            } else {
                callback.errorOccurred("No value found for the expression: " + expression);
            }
        } catch (Exception e) {
            LOG.warn("Expression evaluation is failed for expression: \"" + expression + "\"", e);
            callback.errorOccurred(String.format("Expression evaluation is failed for the expression: \"%s\" due to: %s"
                    , expression, e.getMessage()));
        }
    }
}
