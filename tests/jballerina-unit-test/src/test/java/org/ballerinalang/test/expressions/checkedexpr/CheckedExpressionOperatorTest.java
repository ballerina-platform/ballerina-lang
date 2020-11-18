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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignmentBasics1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Invalid boolean value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignmentBasics2() {
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignmentBasics2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        BError errorStruct = (BError) returns[0];
        Assert.assertEquals(errorStruct.getReason(), "file not found error: /home/sameera/bar.txt",
                "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignmentBasics3() {
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignmentBasics3", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        BError errorStruct = (BError) returns[0];
        Assert.assertEquals(errorStruct.getReason(), "file not found error: /home/sameera/bar.txt",
                "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignmentBasics4() {
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignmentBasics4", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        BError errorStruct = (BError) returns[0];
        Assert.assertEquals(errorStruct.getReason(), "file not found error: /home/sameera/bar.txt",
                "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement1() {
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Invalid boolean value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement2() {
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        BError errorStruct = (BError) returns[0];
        Assert.assertEquals(errorStruct.getReason(), "file not found error: /home/sameera/foo.txt",
                "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement3() {
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement3", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Invalid boolean value returned.");

    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement4() {
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement4", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Invalid boolean value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement5() {
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement5", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        BError errorStruct = (BError) returns[0];
        Assert.assertEquals(errorStruct.getReason(), "file not found error: /home/sameera/bar.txt",
                "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement6() {
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement6", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Invalid boolean value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testSafeAssignOpInAssignmentStatement7() {
        BValue[] returns = BRunUtil.invoke(result, "testSafeAssignOpInAssignmentStatement7", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Diayasena", "Invalid string value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr1() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckExprInBinaryExpr1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        BError errorStruct = (BError) returns[0];
        Assert.assertEquals(errorStruct.getReason(), "io error", "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr2() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckExprInBinaryExpr2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        BError errorStruct = (BError) returns[0];
        Assert.assertEquals(errorStruct.getReason(), "io error", "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr3() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckExprInBinaryExpr3", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "hello, Ballerina", "Invalid string value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr4() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckExprInBinaryExpr4", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        BError errorStruct = (BError) returns[0];
        Assert.assertEquals(errorStruct.getReason(), "io error", "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr5() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckExprInBinaryExpr5", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        BError errorStruct = (BError) returns[0];
        Assert.assertEquals(errorStruct.getReason(), "io error", "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr6() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckExprInBinaryExpr6", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        BError errorStruct = (BError) returns[0];
        Assert.assertEquals(errorStruct.getReason(), "custom io error", "Invalid error message value returned.");
        BMap<String, BValue> errorDetails = (BMap<String, BValue>) errorStruct.getDetails();
        Assert.assertEquals(errorDetails.get(ERROR_DATA_FIELD).stringValue(), "foo.txt",
                "Invalid error message value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckExprInBinaryExpr8() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckExprInBinaryExpr8", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "hello, Hello, World!!!", "Invalid string value returned.");
    }

    @Test(description = "Test basics of safe assignment statement")
    public void testCheckedExprAsFuncParam1() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckedExprAsFuncParam1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "((((S|S)|(S|S))|((S|S)|(S|S)))|(((S|S)|(S|S))|" +
                "((S|S)|(S|S)))) ((A|A)|(A|A)) (((M|M)|(M|M))|((M|M)|(M|M))) done", "Invalid string value returned.");
    }

//    @Test(description = "Test checked expressions in binary and expression")
//    public void testCheckInBinaryAndExpression() {
//        BValue[] returns = BRunUtil.invoke(result, "testCheckInBinaryAndExpression", new BValue[]{});
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertSame(returns[0].getClass(), BBoolean.class);
//        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
//    }

    @Test(description = "Test checked expressions in binary add expression")
    public void testCheckInBinaryAddExpression() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckInBinaryAddExpression", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 30);
    }

    @Test(description = "Test checked expressions in binary div expression")
    public void testCheckInBinaryDivExpression() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckInBinaryDivExpression", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(description = "Test checked expressions in binary LT expression")
    public void testCheckInBinaryLTExpression() {
        BValue[] returns = BRunUtil.invoke(result, "testCheckInBinaryLTExpression", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test service resource that returns an error containing check expression",
            groups = "brokenOnErrorChange")
    public void testSemanticErrorsWithResources() {
        CompileResult compile = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_expr_within_resource.bal");
        Assert.assertEquals(compile.getErrorCount(), 0);
    }
}
