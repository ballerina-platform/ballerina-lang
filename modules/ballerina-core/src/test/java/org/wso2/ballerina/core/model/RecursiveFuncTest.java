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
package org.wso2.ballerina.core.model;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.SubstractExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.types.LongType;
import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.LongValue;

import java.util.ArrayList;
import java.util.List;

/**
 * This class tests recursive function invocations
 *
 * @since 1.0.0
 */
public class RecursiveFuncTest {

    /**
     * function fibonacci(long n) (long) {
     * if (n <= 1) {
     * return n;
     * } else {
     * return fibonacci(n - 1) + fibonacci(n - 2);
     * }
     * }
     *
     * @return
     */
    public Function getfibonacciFunc() {
        // Function parameters
        Parameter paramN = new Parameter(new LongType(), new Identifier("arg2"));
        List<Parameter> parameters = new ArrayList<>(1);
        parameters.add(paramN);

        // Return types
        List<Type> returnTypes = new ArrayList<>(1);
        returnTypes.add(new LongType());

        // Variable reference expressions
        VariableRefExpr varRefExprN = new VariableRefExpr(new Identifier("n"));
        varRefExprN.setEvalFunction(VariableRefExpr.createGetParamValueFunc(0));

        // If else statement
        BasicLiteral intLiteral1 = new BasicLiteral(new BValueRef(new LongValue(1)));
        BasicLiteral intLiteral2 = new BasicLiteral(new BValueRef(new LongValue(2)));

        // (n <= 1)
        LessEqualExpression lessEqualExpr = new LessEqualExpression(varRefExprN, intLiteral1);
        lessEqualExpr.setEvalFunc(LessEqualExpression.LESS_EQUAL_LONG_FUNC);

        // Then body
        // return n;
        ReturnStmt returnStmtThen = new ReturnStmt(varRefExprN);
        Statement[] thenStmts = new Statement[1];
        thenStmts[0] = returnStmtThen;
        BlockStmt thenBody = new BlockStmt(thenStmts);

        // Else body
        // fibonacci(n-1)
        SubstractExpression subExpr1 = new SubstractExpression(varRefExprN, intLiteral1);
        subExpr1.setEvalFunc(SubstractExpression.SUB_LONG_FUNC);
        List<Expression> fInvoExprs = new ArrayList<>(1);
        fInvoExprs.add(subExpr1);
        FunctionInvocationExpr fInvoExpr1 = new FunctionInvocationExpr(new Identifier("fibonacci"), fInvoExprs);

        // fibonacci(n-2)
        SubstractExpression subExpr2 = new SubstractExpression(varRefExprN, intLiteral2);
        subExpr2.setEvalFunc(SubstractExpression.SUB_LONG_FUNC);
        fInvoExprs = new ArrayList<>(1);
        fInvoExprs.add(subExpr2);
        FunctionInvocationExpr fInvoExpr2 = new FunctionInvocationExpr(new Identifier("fibonacci"), fInvoExprs);

        // fibonacci(n-1) + fibonacci(n-2)
        AddExpression addExpression = new AddExpression(fInvoExpr1, fInvoExpr2);
        addExpression.setEvalFunc(AddExpression.ADD_LONG_FUNC);

        // return fibonacci(n-1) + fibonacci(n-2);
        ReturnStmt returnStmtElse = new ReturnStmt(addExpression);

        Statement[] elseStmts = new Statement[1];
        elseStmts[0] = returnStmtElse;
        BlockStmt elseBody = new BlockStmt(elseStmts);

        // IfElse statement
        IfElseStmt ifElseStmt = new IfElseStmt(lessEqualExpr, thenBody, elseBody);

        // Function body
        Statement[] funcBodyStmts = new Statement[1];
        funcBodyStmts[0] = ifElseStmt;
        BlockStmt funcBody = new BlockStmt(funcBodyStmts);

        // Create function
        Function function = new Function(new Identifier("fibonacci"), false, null, parameters, returnTypes,
                null, new ArrayList<>(), null, funcBody);

        fInvoExpr1.setFunction(function);
        fInvoExpr2.setFunction(function);
        return function;
    }

    @Test
    public void testRecursiveFInvocation() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        BValueRef[] parameters = new BValueRef[1];
        parameters[0] = new BValueRef(new LongValue(20));
        StackFrame stackFrame = new StackFrame(parameters, new BValueRef(null), new BValueRef[0]);
        controlStack.pushFrame(stackFrame);

        // Function invocation
        VariableRefExpr varRefExprA = new VariableRefExpr(new Identifier("argA"));
        varRefExprA.setEvalFunction(VariableRefExpr.createGetParamValueFunc(0));

        List<Expression> expressions = new ArrayList<>(1);
        expressions.add(varRefExprA);

        FunctionInvocationExpr invocationExpr = new FunctionInvocationExpr(new Identifier("fibonacci"), expressions);
        invocationExpr.setFunction(getfibonacciFunc());


        BValueRef bValueRef = invocationExpr.evaluate(ctx);
        long actual = bValueRef.getLong();

        long expected = fibonacci(20);

        Assert.assertEquals(actual, expected);
    }


    public static long fibonacci(int n) {
        if (n <= 1) {
            return n;
        } else {
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
    }

    public static void main(String[] args) {
        RecursiveFuncTest recursiveFuncTest = new RecursiveFuncTest();
        recursiveFuncTest.testRecursiveFInvocation();

    }
}
