/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.let;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for let expression.
 *
 * @since 1.2.0
 */
public class LetExpressionTest {

    private CompileResult compileResult, negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/let/let-expression-test.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/let/let-expression-negative.bal");
    }

    @Test(description = "Positive tests for let expression", dataProvider = "FunctionList")
    public void testLetExpression(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @Test(description = "Negative test cases for let expression")
    public void testLetExpressionNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "redeclared symbol 'x'", 19, 21);
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'y'", 23, 27);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 27, 25);
        BAssertUtil.validateError(negativeResult, i, "incompatible types: expected 'string', found 'int'", 29, 28);
    }

    @DataProvider(name = "FunctionList")
    public Object[][] getTestFunctions() {
        return new Object[][]{
                {"testBasicLetExpr"},
                {"testBasicLetExprVar"},
                {"testMultipleVarDeclLetExpr"},
                {"testFunctionCallInVarDeclLetExpr"},
                {"testFunctionCallInLetExpr"},
                {"testMultipleVarDeclReuseLetExpr"},
                {"testLetExprAsFunctionArg"},
                {"testLetExprInIfStatement"},
                {"testLetExprInWhileStatement"},
                {"testLetExprInCompoundStatement"},
                {"testLetExpressionInMatch"},
                {"testLetExpressionInReturn"},
                {"testLetExprInElvis"},
                {"testLetExprInUnion"},
                {"testLetExprInTransaction"},
                {"testLetExprInArrowFunction"},
                {"testLetExprInJSON"},
                {"testLetExpresionInArrays"},
                {"testLetExpresionInTuples"},
                {"testLetExprInMap"},
        };
    }
}
