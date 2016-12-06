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
package org.wso2.ballerina.model.expressions;

import org.wso2.ballerina.interpreter.Context;
import org.wso2.ballerina.interpreter.ControlStack;
import org.wso2.ballerina.interpreter.StackFrame;
import org.wso2.ballerina.model.Function;
import org.wso2.ballerina.model.Identifier;
import org.wso2.ballerina.model.Parameter;
import org.wso2.ballerina.model.VariableDcl;
import org.wso2.ballerina.model.values.BValueRef;
import org.wso2.ballerina.model.values.ValueFactory;

import java.util.List;

/**
 * {@code FunctionInvocationExpr} represents function invocation expression
 *
 * @since 1.0.0
 */
public class FunctionInvocationExpr extends AbstractExpression {

    private Identifier functionName;
    private List<Expression> expressionList;
    private Function calleeFunction;

    public FunctionInvocationExpr(Identifier functionName, List<Expression> expressionList) {
        this.functionName = functionName;
        this.expressionList = expressionList;
    }

    public void setFunction(Function function) {
//        if (expressionList.size() != function.getParameters().size()) {
//            throw new RuntimeException();
//        }
        this.calleeFunction = function;
    }

    public BValueRef evaluate(Context ctx) {
        List<Parameter> parameters = calleeFunction.getParameters();
        List<VariableDcl> variableDcls = calleeFunction.getVariableDcls();

        // Setting up function parameters.
        BValueRef[] funcParams = new BValueRef[parameters.size()];
        for (int index = 0; index < parameters.size(); index++) {
            // TODO Think about the copy-by-bValueRef aspect here.
            BValueRef value = expressionList.get(index).evaluate(ctx);
            funcParams[index] = value;
        }

        // Setting up local variables;
        BValueRef[] localVariables = new BValueRef[variableDcls.size()];
        for (int index = 0; index < variableDcls.size(); index++) {
            BValueRef value = ValueFactory.creteValue(variableDcls.get(index).getType());
            localVariables[index] = value;

        }

        // Return bValueRef
        // TODO Support multiple return types
        BValueRef returnValue = ValueFactory.creteValue(calleeFunction.getReturnTypes().get(0));

        ControlStack controlStack = ctx.getControlStack();
        StackFrame stackFrame = new StackFrame(funcParams, returnValue, localVariables);
        controlStack.pushFrame(stackFrame);

        calleeFunction.interpret(ctx);

        controlStack.popFrame();
        return returnValue;
    }
}
