package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.ValueFactory;

import java.util.List;

/**
 * {@code ConnectorActionInvocationExpr} represents connector action invocation expression
 *
 * @since 1.0.0
 */
public class ConnectorActionInvocationExpr extends AbstractExpression {

    private SymbolName actionName;
    private List<Expression> expressionList;
    private BallerinaAction calleeAction;

    public ConnectorActionInvocationExpr(SymbolName actionName, List<Expression> expressionList) {
        this.actionName = actionName;
        this.expressionList = expressionList;
    }

    public void setCalleeAction(BallerinaAction calleeAction) {
        this.calleeAction = calleeAction;
    }

    @Override
    public void accept(NodeVisitor visitor) {

    }

    @Override
    public BValueRef evaluate(Context ctx) {

        /* ToDo: Logic is quite similar to function: Consider reusing the logic */
        Parameter[] parameters = calleeAction.getParameters();

        BValueRef[] funcParams = new BValueRef[parameters.length];
        for (int index = 0; index < parameters.length; index++) {
            // TODO Think about the copy-by-bValueRef aspect here.
            BValueRef value = expressionList.get(index).evaluate(ctx);
            funcParams[index] = value;
        }

        // Return bValueRef
        // TODO Support multiple return types
        BValueRef returnValue = null;
        if (calleeAction.getReturnTypes().length > 0) {
            returnValue = ValueFactory.creteValue(calleeAction.getReturnTypes()[0]);
        }
        VariableDcl[] variableDcls = calleeAction.getVariableDcls();

        // Setting up local variables;
        BValueRef[] localVariables = new BValueRef[variableDcls.length];
        for (int index = 0; index < variableDcls.length; index++) {
            BValueRef value = ValueFactory.creteValue(variableDcls[index].getType());
            localVariables[index] = value;
        }

        ControlStack controlStack = ctx.getControlStack();
//        StackFrame stackFrame = new StackFrame(funcParams, returnValue, localVariables);
//        controlStack.pushFrame(stackFrame);

        calleeAction.interpret(ctx);
        controlStack.popFrame();
        return returnValue;
    }
}
