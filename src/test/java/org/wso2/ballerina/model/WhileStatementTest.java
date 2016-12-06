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
package org.wso2.ballerina.model;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.interpreter.Context;
import org.wso2.ballerina.interpreter.ControlStack;
import org.wso2.ballerina.interpreter.StackFrame;
import org.wso2.ballerina.model.expressions.AddExpression;
import org.wso2.ballerina.model.expressions.BasicLiteral;
import org.wso2.ballerina.model.expressions.NotEqualExpression;
import org.wso2.ballerina.model.expressions.VariableRefExpr;
import org.wso2.ballerina.model.statements.AssignStmt;
import org.wso2.ballerina.model.statements.BlockStmt;
import org.wso2.ballerina.model.statements.Statement;
import org.wso2.ballerina.model.statements.WhileStmt;
import org.wso2.ballerina.model.values.BValueRef;
import org.wso2.ballerina.model.values.IntValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class to test the functionality of  {@link org.wso2.ballerina.model.statements.WhileStmt} class
 *
 * @since 1.0.0
 */
public class WhileStatementTest {

    @BeforeTest
    public void setup() {
    }

    @Test
    public void testFuncInvokeWithInt() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        BValueRef[] localVariables = new BValueRef[3];
        localVariables[0] = new BValueRef(new IntValue(10));
        localVariables[1] = new BValueRef(new IntValue(1));
        localVariables[2] = new BValueRef(new IntValue(0));
        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
        controlStack.pushFrame(stackFrame);

        WhileStmt whileStmt = getWhileStmtObject();

        whileStmt.interpret(ctx);


        int x = 10;
        int y = 1;
        int z = 0;

        while (x != y) {
            z = z + 10;
            y = y + 1;
        }

        Assert.assertEquals(x, localVariables[0].getInt());
        Assert.assertEquals(y, localVariables[1].getInt());
        Assert.assertEquals(z, localVariables[2].getInt());
    }

    /**
     * x = 10;
     * y = 1;
     * z = 0;
     * while ( x != y ) {
     * z = z + 10;
     * y = y + 1;
     * }
     *
     * @return
     */
    private WhileStmt getWhileStmtObject() {
        Identifier idX = new Identifier("x");
        VariableRefExpr varRefExprX = new VariableRefExpr(idX);
        varRefExprX.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        Identifier idY = new Identifier("y");
        VariableRefExpr varRefExprY = new VariableRefExpr(idY);
        varRefExprY.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));

        Identifier idZ = new Identifier("z");
        VariableRefExpr varRefExprZ = new VariableRefExpr(idZ);
        varRefExprZ.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(2));


        AddExpression addExprZ = new AddExpression(varRefExprZ, new BasicLiteral(new BValueRef(new IntValue(10))));
        addExprZ.setEvalFunc(AddExpression.ADD_INT_FUNC);
        AssignStmt assignStmtZ = new AssignStmt(varRefExprZ, addExprZ);

        AddExpression addExprY = new AddExpression(varRefExprY, new BasicLiteral(new BValueRef(new IntValue(1))));
        addExprY.setEvalFunc(AddExpression.ADD_INT_FUNC);
        AssignStmt assignStmtY = new AssignStmt(varRefExprY, addExprY);

        List<Statement> whileStmtList = new ArrayList<>(2);
        whileStmtList.add(assignStmtZ);
        whileStmtList.add(assignStmtY);
        BlockStmt whileBody = new BlockStmt(whileStmtList);

        NotEqualExpression whileCondition = new NotEqualExpression(varRefExprX, varRefExprY);
        whileCondition.setEvalFunc(NotEqualExpression.NOT_EQUAL_INT_FUNC);

        return new WhileStmt(whileCondition, whileBody);
    }

    public static void main(String[] args) {
        WhileStatementTest test = new WhileStatementTest();
        test.testFuncInvokeWithInt();
    }
}
