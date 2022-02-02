/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.checkedexpr;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contain safe assignment operator '=?' related test scenarios.
 */
public class CheckedExpressionOperatorTest {

    private CompileResult result;
    private static final String ERROR_DATA_FIELD = "data";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_expr_operator_basics.bal");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignmentBasics1() {
        Object returns = JvmRunUtil.invoke(result, "testSafeAssignmentBasics1", new Object[]{});
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns, "Invalid boolean value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignmentBasics2() {
        JvmRunUtil.invoke(result, "testSafeAssignmentBasics2");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignmentBasics3() {
        JvmRunUtil.invoke(result, "testSafeAssignmentBasics3");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignmentBasics4() {
        JvmRunUtil.invoke(result, "testSafeAssignmentBasics4");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement1() {
        Object returns = JvmRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement1", new Object[]{});
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns, "Invalid boolean value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement2() {
        JvmRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement2", new Object[]{});
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement3() {
        Object returns = JvmRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement3", new Object[]{});
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns, "Invalid boolean value returned.");

    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement4() {
        Object returns = JvmRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement4", new Object[]{});
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns, "Invalid boolean value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement5() {
        JvmRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement5", new Object[]{});
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement6() {
        Object returns = JvmRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement6", new Object[]{});
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns, "Invalid boolean value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement7() {
        Object returns = JvmRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement7", new Object[]{});
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Diayasena", "Invalid string value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr1() {
        Object returns = JvmRunUtil.invoke(result, "testCheckExprInBinaryExpr1", new Object[]{});
        Assert.assertTrue(returns instanceof BError);
        BError errorStruct = (BError) returns;
        Assert.assertEquals(errorStruct.getMessage(), "io error", "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr2() {
        Object returns = JvmRunUtil.invoke(result, "testCheckExprInBinaryExpr2", new Object[]{});
        Assert.assertTrue(returns instanceof BError);
        BError errorStruct = (BError) returns;
        Assert.assertEquals(errorStruct.getMessage(), "io error", "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr3() {
        Object returns = JvmRunUtil.invoke(result, "testCheckExprInBinaryExpr3", new Object[]{});
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "hello, Ballerina", "Invalid string value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr4() {
        Object returns = JvmRunUtil.invoke(result, "testCheckExprInBinaryExpr4", new Object[]{});
        Assert.assertTrue(returns instanceof BError);
        BError errorStruct = (BError) returns;
        Assert.assertEquals(errorStruct.getMessage(), "io error", "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr5() {
        Object returns = JvmRunUtil.invoke(result, "testCheckExprInBinaryExpr5", new Object[]{});
        Assert.assertTrue(returns instanceof BError);
        BError errorStruct = (BError) returns;
        Assert.assertEquals(errorStruct.getMessage(), "io error", "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr6() {
        Object returns = JvmRunUtil.invoke(result, "testCheckExprInBinaryExpr6", new Object[]{});
        Assert.assertTrue(returns instanceof BError);
        BError errorStruct = (BError) returns;
        Assert.assertEquals(errorStruct.getMessage(), "custom io error", "Invalid error message value returned.");
        BMap<String, Object> errorDetails = (BMap<String, Object>) errorStruct.getDetails();
        Assert.assertEquals(errorDetails.get(StringUtils.fromString(ERROR_DATA_FIELD)).toString(), "foo.txt",
                "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr8() {
        Object returns = JvmRunUtil.invoke(result, "testCheckExprInBinaryExpr8", new Object[]{});
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "hello, Hello, World!!!", "Invalid string value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckedExprAsFuncParam1() {
        Object returns = JvmRunUtil.invoke(result, "testCheckedExprAsFuncParam1", new Object[]{});
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "((((S|S)|(S|S))|((S|S)|(S|S)))|(((S|S)|(S|S))|" +
                "((S|S)|(S|S)))) ((A|A)|(A|A)) (((M|M)|(M|M))|((M|M)|(M|M))) done", "Invalid string value returned.");
    }

//    @Test(description = "Test checked expressions in binary and expression")
//    public void testCheckInBinaryAndExpression() {
//        Object returns = JvmRunUtil.invoke(result, "testCheckInBinaryAndExpression", new Object[]{});
//        Assert.assertTrue(returns instanceof Boolean);
//        Assert.assertTrue((Boolean) returns);
//    }

    @Test(description = "Test checked expressions in binary add expression")
    public void testCheckInBinaryAddExpression() {
        Object returns = JvmRunUtil.invoke(result, "testCheckInBinaryAddExpression", new Object[]{});
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 30L);
    }

    @Test(description = "Test checked expressions in binary div expression")
    public void testCheckInBinaryDivExpression() {
        Object returns = JvmRunUtil.invoke(result, "testCheckInBinaryDivExpression", new Object[]{});
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 2L);
    }

    @Test(description = "Test checked expressions in binary LT expression")
    public void testCheckInBinaryLTExpression() {
        Object returns = JvmRunUtil.invoke(result, "testCheckInBinaryLTExpression", new Object[]{});
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testCheckedErrorsWithReadOnlyInUnion() {
        JvmRunUtil.invoke(result, "testCheckedErrorsWithReadOnlyInUnion");
    }

    @Test
    public void testCheckWithMixOfDefaultErrorAndDistinctErrors() {
        JvmRunUtil.invoke(result, "testCheckWithMixOfDefaultErrorAndDistinctErrors");
    }

    @Test(description = "Test checked expressions in let expression")
    public void testCheckInLetExpression() {
        JvmRunUtil.invoke(result, "testCheckInLetExpression");
    }

    @Test
    public void testCheckedExprWithNever() {
        JvmRunUtil.invoke(result, "testCheckedExprWithNever");
    }

    @Test(description = "Test service resource that returns an error containing check expression")
    public void testSemanticErrorsWithResources() {
        CompileResult compile = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_expr_within_resource.bal");
        Assert.assertEquals(compile.getErrorCount(), 0);
    }
}
