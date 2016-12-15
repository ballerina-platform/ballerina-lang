/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.ValueFactory;

import java.util.List;

/**
 * {@code FunctionInvocationExpr} represents function invocation expression
 *
 * @since 1.0.0
 */
public class FunctionInvocationExpr extends AbstractExpression {

    private SymbolName functionName;
    private List<Expression> expressionList;
    private Expression[] exprs;
    private Function calleeFunction;

    public FunctionInvocationExpr(SymbolName functionName, List<Expression> expressionList) {
        this.functionName = functionName;
        this.expressionList = expressionList;
    }

    public FunctionInvocationExpr(SymbolName functionName, Expression[] exprs) {
        this.functionName = functionName;
        this.exprs = exprs;
    }

    public SymbolName getFunctionName() {
        return functionName;
    }

    public Expression[] getExprs() {
        return exprs;
    }

    public Function getFunction() {
        return calleeFunction;
    }

    public void setFunction(Function function) {
//        if (expressionList.size() != function.getParameters().size()) {
//            throw new RuntimeException();
//        }
        this.calleeFunction = function;
    }

    public BValueRef evaluate(Context ctx) {
        Parameter[] parameters = calleeFunction.getParameters();

        // Setting up function parameters.
        BValueRef[] funcParams = new BValueRef[parameters.length];
        for (int index = 0; index < parameters.length; index++) {
            // TODO Think about the copy-by-bValueRef aspect here.
            BValueRef value = expressionList.get(index).evaluate(ctx);
            funcParams[index] = value;
        }

        // Return bValueRef
        // TODO Support multiple return types
        BValueRef returnValue = null;
        if (calleeFunction.getReturnTypes().length > 0) {
            returnValue = ValueFactory.creteValue(calleeFunction.getReturnTypes()[0]);
        }

        BValueRef[] localVariables = new BValueRef[0];
        if (calleeFunction instanceof BallerinaFunction) {
            BallerinaFunction function = (BallerinaFunction) calleeFunction;

            VariableDcl[] variableDcls = function.getVariableDcls();

            // Setting up local variables;
            localVariables = new BValueRef[variableDcls.length];
            for (int index = 0; index < variableDcls.length; index++) {
                BValueRef value = ValueFactory.creteValue(variableDcls[index].getType());
                localVariables[index] = value;
            }
        }

        ControlStack controlStack = ctx.getControlStack();
//        StackFrame stackFrame = new StackFrame(funcParams, returnValue, localVariables);
//        controlStack.pushFrame(stackFrame);

        calleeFunction.interpret(ctx);
        controlStack.popFrame();
        return returnValue;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
