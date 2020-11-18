/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.statements.whilestatement;

import org.ballerinalang.core.model.values.BFloat;
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
 * This contains methods to test different behaviours of the while loop statement.
 *
 * @since 0.8.0
 */
public class WhileStmtTest {

    private CompileResult positiveCompileResult;
    private CompileResult negativeCompileResult;
    private CompileResult onfailCompileResult;
    private CompileResult onfailNegativeCompileResult;

    @BeforeClass
    public void setup() {
        positiveCompileResult = BCompileUtil.compile("test-src/statements/whilestatement/while-stmt.bal");
        negativeCompileResult = BCompileUtil.compile("test-src/statements/whilestatement/while-stmt-negative.bal");
        onfailCompileResult = BCompileUtil.compile("test-src/statements/whilestatement/while-stmt-on-fail.bal");
        onfailNegativeCompileResult = BCompileUtil.compile("test-src/statements/whilestatement/"
                + "while-stmt-on-fail-negative.bal");
    }

    @Test(description = "Test while loop with a condition which evaluates to true")
    public void testWhileStmtConditionTrue() {
        BValue[] args = {new BInteger(10), new BInteger(1)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testWhileStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 100;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test while loop with a condition which evaluates to false")
    public void testWhileStmtConditionFalse() {
        BValue[] args = {new BInteger(10), new BInteger(11)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testWhileStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 0;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check the scope managing in while block")
    public void testWhileBlockScopes() {
        BValue[] args = {new BInteger(1)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testWhileScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        BInteger actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 200, "mismatched output value");

        args = new BValue[]{new BInteger(2)};
        returns = BRunUtil.invoke(positiveCompileResult, "testWhileScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 400, "mismatched output value");
    }

    @Test(description = "Check the scope managing in while block with ifelse")
    public void testWhileBlockScopesWithIf() {
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testWhileScopeWithIf");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type of return param1 mismatched");
        Assert.assertSame(returns[1].getClass(), BFloat.class, "Class type of return param2 mismatched");
        BInteger actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 2, "mismatched output value");
        BFloat sum = (BFloat) returns[1];
        Assert.assertEquals(sum.floatValue(), 30.0, "mismatched output value");
    }

    @Test(description = "Test while statement with default values inside the while block")
    public void testWhileWithDefaultValues() {
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testWhileStmtWithDefaultValues");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type of return param1 mismatched");
        Assert.assertSame(returns[1].getClass(), BString.class, "Class type of return param2 mismatched");
        Assert.assertSame(returns[2].getClass(), BFloat.class, "Class type of return param3 mismatched");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1, "mismatched output value");
        Assert.assertEquals(returns[1].stringValue(), "hello", "mismatched output value");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 1.0, "mismatched output value");
    }

    @Test(description = "Test while statement with incompatible types",
          dependsOnMethods = {"testWhileStmtConditionFalse", "testWhileStmtConditionTrue"})
    public void testWhileBlockNegative() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 4);
        BAssertUtil.validateError(negativeCompileResult, 0, "incompatible types: expected 'boolean', found 'string'", 2,
                                 9);
        BAssertUtil.validateError(negativeCompileResult, 1, "incompatible types: expected 'boolean', found 'string'", 6,
                8);
        BAssertUtil.validateError(negativeCompileResult, 2, "incompatible types: expected 'boolean', found 'int'", 10,
                8);
        BAssertUtil.validateError(negativeCompileResult, 3, "incompatible types: expected 'boolean', found " +
                        "'[int,string]'", 14, 8);
    }

    @Test(description = "Test nested while loop with break 1")
    public void testNestedWhileWithBreak1() {
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testNestedWhileWithBreak1");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "inner";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test nested while loop with break 2")
    public void testNestedWhileWithBreak2() {
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testNestedWhileWithBreak2");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "level3level2level1";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test while loop with continue")
    public void testWhileWithContinue() {
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testWhileWithContinue");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "inner2inner4inner5";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test nested while loop with continue")
    public void testNestedWhileWithContinue() {
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testNestedWhileWithContinue");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "level2level3level1";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test type narrowing inside the while body")
    public void testTypeNarrowingInWhileBody() {
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testTypeNarrowingInWhileBody");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "foo1foo2foo3";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test while loop with a condition which evaluates to true")
    public void testWhileStmtWithOnFail() {
        BValue[] args = {new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(onfailCompileResult, "testWhileStmtWithFail", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), " Value: 1 Value: 2 Value: 3-> error caught. " +
                "Hence value returning", "mismatched output value");
    }

    @Test(description = "Test while loop with a condition which evaluates to true")
    public void testWhileStmtWithCheck() {
        BValue[] args = {new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(onfailCompileResult, "testWhileStmtWithCheck", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), " Value: 1 Value: 2 Value: 3-> error caught. " +
                "Hence value returning", "mismatched output value");
    }

    @Test(description = "Test nested while loop with break 2")
    public void testNestedWhileStmtWithFail() {
        BValue[] returns = BRunUtil.invoke(onfailCompileResult, "testNestedWhileStmtWithFail");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "level3-> error caught at level 3, level2-> error caught at level 2, " +
                "level1-> error caught at level 1.";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test while loop termination with fail statement")
    public void testWhileStmtLoopEndingWithFail() {
        BValue[] args = {new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(onfailCompileResult, "testWhileStmtLoopEndingWithFail", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), " Value: 1-> error caught. " +
                "Hence value returning-> reached end", "mismatched output value");
    }

    @Test(description = "Test nested while loop termination with multiple fail statements")
    public void testNestedWhileStmtLoopTerminationWithFail() {
        BValue[] returns = BRunUtil.invoke(onfailCompileResult, "testNestedWhileStmtLoopTerminationWithFail");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "level3-> error caught at level 3, level2-> error caught at level 2, " +
                "level1-> error caught at level 1.";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check not incompatible types and reachable statements.")
    public void testNegative1() {
        Assert.assertEquals(onfailNegativeCompileResult.getErrorCount(), 7);
        BAssertUtil.validateError(onfailNegativeCompileResult, 0, "unreachable code", 17, 6);
        BAssertUtil.validateError(onfailNegativeCompileResult, 1, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 34, 4);
        BAssertUtil.validateError(onfailNegativeCompileResult, 2, "unreachable code", 68, 7);
        BAssertUtil.validateError(onfailNegativeCompileResult, 3, "this function must return a result", 74, 1);
        BAssertUtil.validateError(onfailNegativeCompileResult, 4, "incompatible error definition type: " +
                "'ErrorTypeB' will not be matched to 'ErrorTypeA'", 102, 4);
        BAssertUtil.validateError(onfailNegativeCompileResult, 5, "unreachable code", 116, 9);
        BAssertUtil.validateError(onfailNegativeCompileResult, 6, "unreachable code", 118, 5);
    }
}
