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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for let expression.
 *
 * @since 1.2.0
 */
@Test
public class LetExpressionTest {

    private CompileResult compileResult, negativeResult, notSupportedResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/let/let-expression-test.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/let/let-expression-negative.bal");
        notSupportedResult = BCompileUtil.compile("test-src/expressions/let/let-not-supported.bal");
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
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 28, 39);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 29, 28);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 30, 42);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'byte', found 'int'", 41, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 46, 48);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'float'", 48, 14);
        BAssertUtil.validateError(negativeResult, i++, "too many arguments in call to 'new()'", 57, 37);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 's'", 61, 35);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a'", 62, 47);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'n'", 63, 47);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'x1'", 64, 33);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'y1'", 65, 43);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 65, 55);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'y2'", 65, 55);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'y2'", 65, 73);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'z1'", 66, 39);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'v3'", 67, 64);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a1'", 69, 61);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a4'", 70, 61);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a3'", 70, 76);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a5'", 71, 60);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a3'", 71, 75);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found '[string,string]'",
                72, 54);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a2'", 72, 54);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a7'", 73, 39);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a7'", 73, 56);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'map<string>'",
                74, 44);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a8'", 74, 44);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a9'", 74, 62);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a10'", 75, 55);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'map<string>'",
                76, 55);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a8'", 76, 55);
        BAssertUtil.validateError(negativeResult, i++, "self referenced variable 'a11'", 76, 74);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                "'(int|float|decimal|string|boolean|xml)', found 'A'", 92, 41);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(description = "Test cases for scenarios where let expression is not yet supported")
    public void testLetExpressionNotSupported() {
        int i = 0;
        BAssertUtil.validateError(notSupportedResult, i++, "let expressions are not yet supported for record fields",
                18, 17);
        BAssertUtil.validateError(notSupportedResult, i, "let expressions are not yet supported for object fields",
                22, 22);
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
                {"testGloballyDefinedLetExpr"},
                {"testLetExprAsFunctionArg"},
                {"testLetExprInIfStatement"},
                {"testLetExprInWhileStatement"},
                {"testLetExprInCompoundStatement"},
                {"testLetExpressionInMatch"},
                {"testLetExpressionInReturn"},
                {"testLetExprInElvis"},
                {"testLetExprInUnion"},
//                {"testLetExprInTransaction"},
//                {"testLetExprInArrowFunction"},
                {"testLetExprInJSON"},
                {"testLetExpresionInArrays"},
                {"testLetExpresionInTuples"},
                {"testLetExprInMap"},
                {"testLetExpressionTupleSimple"},
                {"testLetExpressionTupleBinding"},
                {"testLetExpressionTupleComplex"},
                {"testLetExpressionTupleBindingComplex"},
                {"testLetExpressionTupleBindingRef"},
                {"testLetExpressionRecordBindingSimple"},
                {"testLetExpressionRecordBindingComplexVar"},
                {"testLetExpressionErrorBindingSimple"},
                {"testLetExpressionErrorBindingVar"},
                {"testLetExpressionRecordConstrainedErrorBinding"},
//                {"testLetExprInRecord"},
//                {"testLetExprInObj"},
                {"testAnonymousRecordWithLetExpression"},
                {"testRecordWithLetExpression"},
                {"testLetWithClass"},
                {"testLetWithBLangXMLTextLiteral"},
                {"testLetExprWithBLangXMLCommentLiteral"},
                {"testLetExprWithBLangXMLQuotedString"}
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        negativeResult = null;
        notSupportedResult = null;
    }
}
