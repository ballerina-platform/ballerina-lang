/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.model;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.types.IntType;
import org.wso2.ballerina.core.model.types.StringType;
import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.utils.TriFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class to test the functionality of the {@link BallerinaFunction} class
 *
 * @since 1.0.0
 */
public class FunctionInvocationTest {

    @BeforeTest
    public void setup() {
    }

    /**
     * function addInternal(int arg1, int arg2) (int) {
     * return arg1 + arg2;
     * }
     */
    private BallerinaFunction getAddInternalFunc(Type type,
                                                 TriFunction<Context, Expression, Expression, BValueRef> evalFunc) {
        Annotation[] annotations = new Annotation[0];

        Parameter paramX = new Parameter(type, new Identifier("arg1"));
        Parameter paramY = new Parameter(type, new Identifier("arg2"));
        Parameter[] parameters = new Parameter[2];
        parameters[0] = paramX;
        parameters[1] = paramY;

        VariableDcl[] variableDcls = new VariableDcl[0];

        Type[] returnTypes = new Type[1];
        returnTypes[0] = type;

        ConnectorDcl[] connectorDcls = new ConnectorDcl[0];

        Worker[] workers = new Worker[0];

        // BallerinaFunction Body
        Identifier idArg1 = new Identifier("arg1");
        VariableRefExpr varRefExprArg1 = new VariableRefExpr(idArg1);
        varRefExprArg1.setEvalFunction(VariableRefExpr.createGetParamValueFunc(0));

        Identifier idArg2 = new Identifier("arg2");
        VariableRefExpr varRefExprArg2 = new VariableRefExpr(idArg2);
        varRefExprArg2.setEvalFunction(VariableRefExpr.createGetParamValueFunc(1));

        AddExpression addExpression = new AddExpression(varRefExprArg1, varRefExprArg2);
        addExpression.setEvalFunc(evalFunc);

        ReturnStmt returnStmt = new ReturnStmt(addExpression);

        Statement[] statements = new Statement[1];
        statements[0] = returnStmt;
        BlockStmt funcBody = new BlockStmt(statements);

        BallerinaFunction function = new BallerinaFunction(new Identifier("addInternal"), false, annotations,
                parameters, returnTypes, connectorDcls, variableDcls, workers, funcBody);

        return function;

    }

    /**
     * function add(int x, int y) (int) {
     * int sum;
     * sum = addInternal(x, y);
     * return sum;
     * }
     */
    private BallerinaFunction getAddFunc(Type type,
                                         TriFunction<Context, Expression, Expression, BValueRef> evalFunc) {
        // Defining the "echoInt" function
        Annotation[] annotations = new Annotation[0];

        Parameter paramX = new Parameter(type, new Identifier("x"));
        Parameter paramY = new Parameter(type, new Identifier("y"));
        Parameter[] parameters = new Parameter[2];
        parameters[0] = paramX;
        parameters[1] = paramY;

        VariableDcl varDclSUM = new VariableDcl(type, new Identifier("sum"), "");
        VariableDcl[] variableDcls = new VariableDcl[1];
        variableDcls[0] = varDclSUM;

        Type[] returnTypes = new Type[1];
        returnTypes[0] = type;

        ConnectorDcl[] connectorDcls = new ConnectorDcl[0];

        Worker[] workers = new Worker[0];

        // BallerinaFunction body
        Identifier idX = new Identifier("x");
        VariableRefExpr varRefExprX = new VariableRefExpr(idX);
        varRefExprX.setEvalFunction(VariableRefExpr.createGetParamValueFunc(0));

        Identifier idY = new Identifier("y");
        VariableRefExpr varRefExprY = new VariableRefExpr(idY);
        varRefExprY.setEvalFunction(VariableRefExpr.createGetParamValueFunc(1));

        Identifier idSUM = new Identifier("sum");
        VariableRefExpr varRefExprSUM = new VariableRefExpr(idSUM);
        varRefExprSUM.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

//        AddExpression addExpression = new AddExpression(varRefExprX, varRefExprY);
//        addExpression.setEvalFunc(evalFunc);
        List<Expression> fInvoExprs = new ArrayList<>(2);
        fInvoExprs.add(varRefExprX);
        fInvoExprs.add(varRefExprY);
        FunctionInvocationExpr fInvoExpr = new FunctionInvocationExpr(new Identifier("addNumbers"), fInvoExprs);
        BallerinaFunction addInternalFunc = getAddInternalFunc(type, evalFunc);
        fInvoExpr.setFunction(addInternalFunc);

        AssignStmt assignStmt = new AssignStmt(varRefExprSUM, fInvoExpr);
        ReturnStmt returnStmt = new ReturnStmt(varRefExprSUM);

        Statement[] statements = new Statement[2];
        statements[0] = assignStmt;
        statements[1] = returnStmt;

        BlockStmt funcBody = new BlockStmt(statements);
        BallerinaFunction function = new BallerinaFunction(new Identifier("addNumbers"), false, annotations, parameters,
                returnTypes, connectorDcls, variableDcls, workers, funcBody);

        return function;
    }

    /**
     * function add(int x, int y) (int) {
     * int sum;
     * sum = addInternal(x, y);
     * return sum;
     * }
     */
    private FunctionInvocationExpr getFunctionInvocationExpr(
            Type type,
            TriFunction<Context, Expression, Expression, BValueRef> evalFunc) {

        // BallerinaFunction invocation
        Identifier idA = new Identifier("argA");
        VariableRefExpr varRefExprA = new VariableRefExpr(idA);
        varRefExprA.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        Identifier idB = new Identifier("argB");
        VariableRefExpr varRefExprB = new VariableRefExpr(idB);
        varRefExprB.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));

        List<Expression> expressions = new ArrayList<>(1);
        expressions.add(varRefExprA);
        expressions.add(varRefExprB);

        FunctionInvocationExpr invocationExpr = new FunctionInvocationExpr(new Identifier("add"), expressions);

        BallerinaFunction function = getAddFunc(type, evalFunc);
        invocationExpr.setFunction(function);

        return invocationExpr;
    }

    /**
     * function add(string x, string y) (string) {
     * string sum;
     * sum = addInternal(x, y);
     * return sum;
     * }
     * <p>
     * function addInternal(string arg1, string arg2) (string) {
     * return arg1 + arg2;
     * }
     */
    @Test
    public void testFuncInvokeWithString() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        BValueRef[] localVariables = new BValueRef[2];
        localVariables[0] = new BValueRef(new StringValue("Hello "));
        localVariables[1] = new BValueRef(new StringValue("world!!!"));
        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
        controlStack.pushFrame(stackFrame);

        FunctionInvocationExpr invocationExpr = getFunctionInvocationExpr(new StringType(),
                AddExpression.ADD_STRING_FUNC);
        BValueRef returnValue = invocationExpr.evaluate(ctx);

//        stackFrame = controlStack.popFrame();
        String returnVal = ((StringValue) returnValue.getBValue()).getValue();
        Assert.assertEquals("Hello world!!!", returnVal);
    }

    /**
     * function add(int x, int y) (int) {
     * int sum;
     * sum = addInternal(x, y);
     * return sum;
     * }
     * <p>
     * function addInternal(int arg1, int arg2) (int) {
     * return arg1 + arg2;
     * }
     */
    @Test
    public void testFuncInvokeWithInt() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        BValueRef[] localVariables = new BValueRef[2];
        localVariables[0] = new BValueRef(new IntValue(100));
        localVariables[1] = new BValueRef(new IntValue(23));
        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
        controlStack.pushFrame(stackFrame);

        FunctionInvocationExpr invocationExpr = getFunctionInvocationExpr(new IntType(),
                AddExpression.ADD_INT_FUNC);
        BValueRef returnValue = invocationExpr.evaluate(ctx);

//        stackFrame = controlStack.popFrame();
        int returnVal = ((IntValue) returnValue.getBValue()).getValue();
        Assert.assertEquals(123, returnVal);
    }


    public static void main(String[] args) {
        FunctionInvocationTest test = new FunctionInvocationTest();
        test.testFuncInvokeWithString();
    }
}
