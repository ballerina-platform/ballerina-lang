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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] args = {new BInteger(10), new BInteger(10), new BInteger(20)};
        BValue[] returns = BRunUtil.invoke(result, funcName, args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 110;
        Assert.assertEquals(actual, expected);

        actual = ((BInteger) returns[1]).intValue();
        expected = 21;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check a == b + 1")
    public void testElseIfFirstBlock() {
        BValue[] args = {new BInteger(11), new BInteger(10), new BInteger(20)};
        BValue[] returns = BRunUtil.invoke(result, funcName, args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 210;
        Assert.assertEquals(actual, expected);

        actual = ((BInteger) returns[1]).intValue();
        expected = 21;
        Assert.assertEquals(actual, expected);

    }

    @Test(description = "Check a == b + 2")
    public void testElseIfSecondBlock() {
        BValue[] args = {new BInteger(12), new BInteger(10), new BInteger(20)};
        BValue[] returns = BRunUtil.invoke(result, funcName, args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 310;
        Assert.assertEquals(actual, expected);

        actual = ((BInteger) returns[1]).intValue();
        expected = 21;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check else")
    public void testElseBlock() {
        BValue[] args = {new BInteger(10), new BInteger(100), new BInteger(20)};
        BValue[] returns = BRunUtil.invoke(result, funcName, args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 410;
        Assert.assertEquals(actual, expected);

        actual = ((BInteger) returns[1]).intValue();
        expected = 21;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check If Stmt Without Parentheses")
    public void testIfStmtWithoutParentheses() {
        BValue[] args = {new BInteger(10), new BInteger(100), new BInteger(20)};
        BValue[] returns = BRunUtil.invoke(result, "testIfStmtWithoutParentheses", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 410;
        Assert.assertEquals(actual, expected);

        actual = ((BInteger) returns[1]).intValue();
        expected = 21;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check simple ifElse")
    public void testAge() {
        BValue[] args = {new BInteger(21)};
        BValue[] returns = BRunUtil.invoke(result, "testAgeGroup", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        String actual = returns[0].stringValue();
        String expected = "elder";
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BInteger(16)};
        returns = BRunUtil.invoke(result, "testAgeGroup", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        actual = returns[0].stringValue();
        expected = "minor";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check the scope managing in ifelse block")
    public void testIfElseBlockScopes() {
        BValue[] args = { new BInteger(1) };
        BValue[] returns = BRunUtil.invoke(result, "ifElseScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        BInteger actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 200, "mismatched output value");

        args = new BValue[] { new BInteger(2) };
        returns = BRunUtil.invoke(result, "ifElseScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 400, "mismatched output value");

        args = new BValue[] { new BInteger(16) };
        returns = BRunUtil.invoke(result, "ifElseScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 500, "mismatched output value");
    }

    @Test(description = "Check the scope managing in nested ifelse block")
    public void testNestedIfElseBlockScopes() {
        BValue[] args = { new BInteger(1), new BInteger(1) };
        BValue[] returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        BInteger actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 100, "mismatched output value");

        args = new BValue[] { new BInteger(1), new BInteger(2) };
        returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 200, "mismatched output value");

        args = new BValue[] { new BInteger(2), new BInteger(2) };
        returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 300, "mismatched output value");

        args = new BValue[] { new BInteger(2), new BInteger(3) };
        returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 400, "mismatched output value");

        args = new BValue[] { new BInteger(3), new BInteger(3) };
        returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 500, "mismatched output value");

        args = new BValue[] { new BInteger(3), new BInteger(4) };
        returns = BRunUtil.invoke(result, "nestedIfElseScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 600, "mismatched output value");
    }

    @Test(description = "Test if condition parameter resolver scope")
    public void testIfConditionScope() {
        BValue[] args1 = { new BInteger(3)};
        BValue[] returns = BRunUtil.invoke(result, "testConditionScope", args1);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        BInteger actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 10, "if condition scope not set properly");

        BValue[] args2 = new BValue[] { new BInteger(6) };
        returns = BRunUtil.invoke(result, "testConditionScope", args2);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 20, "elseif condition scope not set properly");
    }

    @Test()
    public void ifStmtNegativeTest() {
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
        BAssertUtil.validateError(negativeResult, 0, "this function must return a result", 1, 1);
    }

    @Test()
    public void ifStmtNegativeTestSemanticsNegative() {
        Assert.assertEquals(semanticsNegativeResult.getErrorCount(), 6);
        BAssertUtil.validateError(semanticsNegativeResult, 0,
                "incompatible types: expected 'boolean', found 'int'", 2, 7);
        BAssertUtil.validateError(semanticsNegativeResult, 1,
                "incompatible types: expected 'boolean', found 'string'", 22, 16);
        BAssertUtil.validateError(semanticsNegativeResult, 2,
                "incompatible types: expected 'boolean', found 'string'", 28, 9);
        BAssertUtil.validateError(semanticsNegativeResult, 3,
                "incompatible types: expected 'boolean', found 'string'", 35, 8);
        BAssertUtil.validateError(semanticsNegativeResult, 4,
                "incompatible types: expected 'boolean', found 'int'", 37, 15);
        BAssertUtil.validateError(semanticsNegativeResult, 5,
                "incompatible types: expected 'boolean', found '[int,string]'", 41, 8);
    }
}
