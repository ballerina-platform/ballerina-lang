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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.IntValue;

/**
 * Here we test the functionality of {@link org.wso2.ballerina.core.model.statements.IfElseStmt} class
 *
 * @since 1.0.0
 */
public class IfElseStmtTest {

    @BeforeTest
    public void setup() {
    }

    @Test
    public void testIfElseStmtThenBody() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        BValueRef[] localVariables = new BValueRef[3];
        localVariables[0] = new BValueRef(new IntValue(10));
        localVariables[1] = new BValueRef(new IntValue(10));
        localVariables[2] = new BValueRef(new IntValue(0));
        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
        controlStack.pushFrame(stackFrame);

        IfElseStmt ifElseStmt = getIfStmtObject();

        ifElseStmt.interpret(ctx);

        int x = 10;
        int y = 10;
//        int z = 0;
//
//        if (x == y) {
//            z = z + 10;
//        } else {
//            z = z + 20;
//        }
        int z = 10;

        Assert.assertEquals(localVariables[0].getInt(), x);
        Assert.assertEquals(localVariables[1].getInt(), y);
        Assert.assertEquals(localVariables[2].getInt(), z);
    }

    @Test
    public void testIfElseStmtElseBody() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        BValueRef[] localVariables = new BValueRef[3];
        localVariables[0] = new BValueRef(new IntValue(10));
        localVariables[1] = new BValueRef(new IntValue(9));
        localVariables[2] = new BValueRef(new IntValue(0));
        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
        controlStack.pushFrame(stackFrame);

        IfElseStmt ifElseStmt = getIfStmtObject();

        ifElseStmt.interpret(ctx);

        int x = 10;
        int y = 9;
//        int z = 0;
//
//        if (x == y) {
//            z = z + 10;
//        } else {
//            z = z + 20;
//        }
        int z = 20;

        Assert.assertEquals(localVariables[0].getInt(), x);
        Assert.assertEquals(localVariables[1].getInt(), y);
        Assert.assertEquals(localVariables[2].getInt(), z);
    }

    /**
     * x = 10;
     * y = 1;
     * z = 0;
     * if ( x == y ) {
     * z = z + 10;
     * else {
     * z = z + 20;
     * }
     *
     * @return
     */
    private IfElseStmt getIfStmtObject() {
        SymbolName idX = new SymbolName("x");
        VariableRefExpr varRefExprX = new VariableRefExpr(idX);
        varRefExprX.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        SymbolName idY = new SymbolName("y");
        VariableRefExpr varRefExprY = new VariableRefExpr(idY);
        varRefExprY.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));

        SymbolName idZ = new SymbolName("z");
        VariableRefExpr varRefExprZ = new VariableRefExpr(idZ);
        varRefExprZ.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(2));


        EqualExpression ifCond = new EqualExpression(varRefExprX, varRefExprY);
        ifCond.setEvalFunc(EqualExpression.EQUAL_INT_FUNC);

        return new IfElseStmt(ifCond, getThenBody(varRefExprZ), getElseBody(varRefExprZ));
    }

    private Statement getThenBody(VariableRefExpr varRefExprZ) {
        AddExpression addExprZ = new AddExpression(varRefExprZ, new BasicLiteral(new BValueRef(new IntValue(10))));
        addExprZ.setEvalFunc(AddExpression.ADD_INT_FUNC);
        AssignStmt assignStmt = new AssignStmt(varRefExprZ, addExprZ);

        Statement[] statements = new Statement[1];
        statements[0] = assignStmt;
        return new BlockStmt(statements);
    }

    private Statement getElseBody(VariableRefExpr varRefExprZ) {
        AddExpression addExprZ = new AddExpression(varRefExprZ, new BasicLiteral(new BValueRef(new IntValue(20))));
        addExprZ.setEvalFunc(AddExpression.ADD_INT_FUNC);
        AssignStmt assignStmt = new AssignStmt(varRefExprZ, addExprZ);

        Statement[] statements = new Statement[1];
        statements[0] = assignStmt;
        return new BlockStmt(statements);
    }

    public static void main(String[] args) {
        IfElseStmtTest test = new IfElseStmtTest();
        test.testIfElseStmtElseBody();
    }
}
