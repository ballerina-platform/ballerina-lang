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
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.IntValue;


/**
 * Test class to test the functionality of  {@link org.wso2.ballerina.core.model.statements.WhileStmt} class
 *
 * @since 1.0.0
 */
public class WhileStmtTest {

//    @BeforeTest
    public void setup() {
    }

//    @Test
    public void testFuncInvokeWithInt() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        BValueRef[] localVariables = new BValueRef[3];
        localVariables[0] = new BValueRef(new IntValue(10));
        localVariables[1] = new BValueRef(new IntValue(1));
        localVariables[2] = new BValueRef(new IntValue(0));
//        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
//        controlStack.pushFrame(stackFrame);

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
        SymbolName idX = new SymbolName("x");
        VariableRefExpr varRefExprX = new VariableRefExpr(idX);
        varRefExprX.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        SymbolName idY = new SymbolName("y");
        VariableRefExpr varRefExprY = new VariableRefExpr(idY);
        varRefExprY.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));

        SymbolName idZ = new SymbolName("z");
        VariableRefExpr varRefExprZ = new VariableRefExpr(idZ);
        varRefExprZ.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(2));


        AddExpression addExprZ = new AddExpression(varRefExprZ, new BasicLiteral(new BValueRef(new IntValue(10))));
        addExprZ.setEvalFunc(AddExpression.ADD_INT_FUNC);
        AssignStmt assignStmtZ = new AssignStmt(varRefExprZ, addExprZ);

        AddExpression addExprY = new AddExpression(varRefExprY, new BasicLiteral(new BValueRef(new IntValue(1))));
        addExprY.setEvalFunc(AddExpression.ADD_INT_FUNC);
        AssignStmt assignStmtY = new AssignStmt(varRefExprY, addExprY);

        Statement[] statements = new Statement[2];
        statements[0] = assignStmtZ;
        statements[1] = assignStmtY;
        BlockStmt whileBody = new BlockStmt(statements);

        NotEqualExpression whileCondition = new NotEqualExpression(varRefExprX, varRefExprY);
        whileCondition.setEvalFunc(NotEqualExpression.NOT_EQUAL_INT_FUNC);

        return new WhileStmt(whileCondition, whileBody);
    }

    public static void main(String[] args) {
        WhileStmtTest test = new WhileStmtTest();
        test.testFuncInvokeWithInt();
    }
}
