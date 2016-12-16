package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;

/**
 * {@code ConnectorActionInvocationExpr} represents connector action invocation expression
 *
 * @since 1.0.0
 */
public class ActionInvocationExpr extends AbstractExpression {

    private SymbolName actionName;
    private Expression[] experssions;
    private Action calleeAction;

    public ActionInvocationExpr(SymbolName actionName, Expression[] experssions) {
        this.actionName = actionName;
        this.experssions = experssions;
    }

    public void setCalleeAction(Action calleeAction) {
        this.calleeAction = calleeAction;
    }

    public SymbolName getActionName() {
        return actionName;
    }

    public Expression[] getExperssions() {
        return experssions;
    }

    public Action getCalleeAction() {
        return calleeAction;
    }

    @Override
    public void accept(NodeVisitor visitor) {

        visitor.visit((BallerinaAction) calleeAction);
    }

    @Override
    public BValueRef evaluate(Context ctx) {

        ((AbstractNativeAction) calleeAction).execute(ctx);
        //TODO this can be remove after async invocation
        return ctx.getControlStack().getReturnValue(4);
    }
}
