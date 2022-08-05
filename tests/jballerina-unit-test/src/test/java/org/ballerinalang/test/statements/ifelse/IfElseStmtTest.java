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
package org.ballerinalang.test.statements.ifelse;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test different behaviours of the if-else statement.
 *
 * @since 0.8.0
 */
public class IfElseStmtTest {

    private CompileResult result, negativeResult, semanticsNegativeResult;
    private final String funcName = "testIfStmt";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/ifelse/if-stmt.bal");
        negativeResult = BCompileUtil.compile("test-src/statements/ifelse/if-stmt-negative.bal");
        semanticsNegativeResult = BCompileUtil.compile("test-src/statements/ifelse/if-stmt-semantics-negative.bal");
    }

    @Test(description = "Check a == b")
    public void testIfBlock() {
        Object[] args = {(10), (10), (20)};
        BArray returns = (BArray) BRunUtil.invoke(result, funcName, args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertSame(returns.get(1).getClass(), Long.class);

        long actual = (long) returns.get(0);
        long expected = 110;
        Assert.assertEquals(actual, expected);

        actual = (long) returns.get(1);
        expected = 21;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check a == b + 1")
    public void testElseIfFirstBlock() {
        Object[] args = {(11), (10), (20)};
        BArray returns = (BArray) BRunUtil.invoke(result, funcName, args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertSame(returns.get(1).getClass(), Long.class);

        long actual = (long) returns.get(0);
        long expected = 210;
        Assert.assertEquals(actual, expected);

        actual = (long) returns.get(1);
        expected = 21;
        Assert.assertEquals(actual, expected);

    }

    @Test(description = "Check a == b + 2")
    public void testElseIfSecondBlock() {
        Object[] args = {(12), (10), (20)};
        BArray returns = (BArray) BRunUtil.invoke(result, funcName, args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertSame(returns.get(1).getClass(), Long.class);

        long actual = (long) returns.get(0);
        long expected = 310;
        Assert.assertEquals(actual, expected);

        actual = (long) returns.get(1);
        expected = 21;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check else")
    public void testElseBlock() {
        Object[] args = {(10), (100), (20)};
        BArray returns = (BArray) BRunUtil.invoke(result, funcName, args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertSame(returns.get(1).getClass(), Long.class);

        long actual = (long) returns.get(0);
        long expected = 410;
        Assert.assertEquals(actual, expected);

        actual = (long) returns.get(1);
        expected = 21;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check If Stmt Without Parentheses")
    public void testIfStmtWithoutParentheses() {
        Object[] args = {(10), (100), (20)};
        BArray returns = (BArray) BRunUtil.invoke(result, "testIfStmtWithoutParentheses", args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertSame(returns.get(1).getClass(), Long.class);

        long actual = (long) returns.get(0);
        long expected = 410;
        Assert.assertEquals(actual, expected);

        actual = (long) returns.get(1);
        expected = 21;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check simple ifElse")
    public void testAge() {
        Object[] args = {(21)};
        Object returns = BRunUtil.invoke(result, "testAgeGroup", args);

        
        Assert.assertTrue(returns instanceof BString);
        String actual = returns.toString();
        String expected = "elder";
        Assert.assertEquals(actual, expected);

        args = new Object[]{(16)};
        returns = BRunUtil.invoke(result, "testAgeGroup", args);

        
        Assert.assertTrue(returns instanceof BString);

        actual = returns.toString();
        expected = "minor";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check the scope managing in ifelse block")
    public void testIfElseBlockScopes() {
        Object[] args = { (1) };
        Object returns = BRunUtil.invoke(result, "ifElseScope", args);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        long actual = (long) returns;
        Assert.assertEquals(actual, 200, "mismatched output value");

        args = new Object[] { (2) };
        returns = BRunUtil.invoke(result, "ifElseScope", args);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        actual = (long) returns;
        Assert.assertEquals(actual, 400, "mismatched output value");

        args = new Object[] { (16) };
        returns = BRunUtil.invoke(result, "ifElseScope", args);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        actual = (long) returns;
        Assert.assertEquals(actual, 500, "mismatched output value");
    }

    @Test(description = "Check the scope managing in nested ifelse block")
    public void testNestedIfElseBlockScopes() {
        Object[] args = { (1), (1) };
        Object returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        long actual = (long) returns;
        Assert.assertEquals(actual, 100, "mismatched output value");

        args = new Object[] { (1), (2) };
        returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        actual = (long) returns;
        Assert.assertEquals(actual, 200, "mismatched output value");

        args = new Object[] { (2), (2) };
        returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        actual = (long) returns;
        Assert.assertEquals(actual, 300, "mismatched output value");

        args = new Object[] { (2), (3) };
        returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        actual = (long) returns;
        Assert.assertEquals(actual, 400, "mismatched output value");

        args = new Object[] { (3), (3) };
        returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        actual = (long) returns;
        Assert.assertEquals(actual, 500, "mismatched output value");

        args = new Object[] { (3), (4) };
        returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        actual = (long) returns;
        Assert.assertEquals(actual, 600, "mismatched output value");
    }

    @Test(description = "Test if condition parameter resolver scope")
    public void testIfConditionScope() {
        Object[] args1 = { (3)};
        Object returns = BRunUtil.invoke(result, "testConditionScope", args1);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        long actual = (long) returns;
        Assert.assertEquals(actual, 10, "if condition scope not set properly");

        Object[] args2 = new Object[] { (6) };
        returns = BRunUtil.invoke(result, "testConditionScope", args2);
        
        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        actual = (long) returns;
        Assert.assertEquals(actual, 20, "elseif condition scope not set properly");
    }

    @Test(description = "Test is condition with circular tuples inside a if block")
    public void testCustomCircularTupleTypeWithIsCheck() {
        BRunUtil.invoke(result, "testCustomCircularTupleTypeWithIsCheck");
    }

    @Test()
    public void ifStmtNegativeTest() {
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
        BAssertUtil.validateError(negativeResult, 0, "this function must return a result", 7, 1);
    }

    @Test()
    public void ifStmtNegativeTestSemanticsNegative() {
        int i = 0;
        BAssertUtil.validateError(semanticsNegativeResult, i++,
                "incompatible types: expected 'boolean', found 'int'", 2, 7);
        BAssertUtil.validateError(semanticsNegativeResult, i++,
                "incompatible types: expected 'boolean', found 'string'", 22, 16);
        BAssertUtil.validateError(semanticsNegativeResult, i++,
                "incompatible types: expected 'boolean', found 'string'", 28, 9);
        BAssertUtil.validateError(semanticsNegativeResult, i++,
                "incompatible types: expected 'boolean', found 'string'", 35, 8);
        BAssertUtil.validateError(semanticsNegativeResult, i++,
                "incompatible types: expected 'boolean', found 'int'", 37, 15);
        BAssertUtil.validateError(semanticsNegativeResult, i++,
                "incompatible types: expected 'boolean', found '[int,string]'", 41, 8);
        BAssertUtil.validateError(semanticsNegativeResult, i++, "operator '+' not defined for '(int|string)' and 'int'",
                52, 17);
        BAssertUtil.validateError(semanticsNegativeResult, i++,
                                  "incompatible types: expected 'int', found '(int|string)'", 57, 17);
        BAssertUtil.validateError(semanticsNegativeResult, i++, "incompatible types: expected 'string', " +
                        "found '(int|string)'", 59, 20);
        Assert.assertEquals(semanticsNegativeResult.getErrorCount(), i);
    }

    @Test()
    public void ifStmtTypeNarrowingTest() {
        Object[] args = {StringUtils.fromString("ballerina")};
        BRunUtil.invoke(result, "testTypeNarrowingWithLambda");
        BRunUtil.invoke(result, "testResetTypeNarrowingForCompoundAssignment");
        BRunUtil.invoke(result, "testResetTypeNarrowing");
        BRunUtil.invoke(result, "testResetTypeNarrowingWithBlockStmt");
        Object returns = BRunUtil.invoke(result, "testTypeNarrowing", args);
        
        Assert.assertEquals(returns.toString(), "ballerina");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
        semanticsNegativeResult = null;
    }
}
