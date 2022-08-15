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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
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
        onfailNegativeCompileResult = BCompileUtil.compile(
                "test-src/statements/whilestatement/while-stmt-on-fail-negative.bal");
    }

    @Test(description = "Test while loop with a condition which evaluates to true")
    public void testWhileStmtConditionTrue() {
        Object[] args = {(10), (1)};
        Object returns = BRunUtil.invoke(positiveCompileResult, "testWhileStmt", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 100;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test while loop with a condition which evaluates to false")
    public void testWhileStmtConditionFalse() {
        Object[] args = {(10), (11)};
        Object returns = BRunUtil.invoke(positiveCompileResult, "testWhileStmt", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 0;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check the scope managing in while block")
    public void testWhileBlockScopes() {
        Object[] args = {(1)};
        Object returns = BRunUtil.invoke(positiveCompileResult, "testWhileScope", args);

        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        long actual = (long) returns;
        Assert.assertEquals(actual, 200, "mismatched output value");

        args = new Object[]{(2)};
        returns = BRunUtil.invoke(positiveCompileResult, "testWhileScope", args);

        Assert.assertSame(returns.getClass(), Long.class, "Class type mismatched");
        actual = (long) returns;
        Assert.assertEquals(actual, 400, "mismatched output value");
    }

    @Test(description = "Check the scope managing in while block with ifelse")
    public void testWhileBlockScopesWithIf() {
        BArray returns = (BArray) BRunUtil.invoke(positiveCompileResult, "testWhileScopeWithIf");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Long.class, "Class type of return param1 mismatched");
        Assert.assertSame(returns.get(1).getClass(), Double.class, "Class type of return param2 mismatched");
        long actual = (long) returns.get(0);
        Assert.assertEquals(actual, 2, "mismatched output value");
        double sum = (double) returns.get(1);
        Assert.assertEquals(sum, 30.0, "mismatched output value");
    }

    @Test(description = "Test while statement with default values inside the while block")
    public void testWhileWithDefaultValues() {
        BArray returns = (BArray) BRunUtil.invoke(positiveCompileResult, "testWhileStmtWithDefaultValues");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertSame(returns.get(0).getClass(), Long.class, "Class type of return param1 mismatched");
        Assert.assertTrue(returns.get(1) instanceof BString, "Class type of return param2 mismatched");
        Assert.assertSame(returns.get(2).getClass(), Double.class, "Class type of return param3 mismatched");

        Assert.assertEquals(returns.get(0), 1L, "mismatched output value");
        Assert.assertEquals(returns.get(1).toString(), "hello", "mismatched output value");
        Assert.assertEquals(returns.get(2), 1.0, "mismatched output value");
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
        Object returns = BRunUtil.invoke(positiveCompileResult, "testNestedWhileWithBreak1");

        Assert.assertTrue(returns instanceof BString);

        String actual = returns.toString();
        String expected = "inner";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test nested while loop with break 2")
    public void testNestedWhileWithBreak2() {
        Object returns = BRunUtil.invoke(positiveCompileResult, "testNestedWhileWithBreak2");

        Assert.assertTrue(returns instanceof BString);

        String actual = returns.toString();
        String expected = "level3level2level1";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test while loop with continue")
    public void testWhileWithContinue() {
        Object returns = BRunUtil.invoke(positiveCompileResult, "testWhileWithContinue");

        Assert.assertTrue(returns instanceof BString);

        String actual = returns.toString();
        String expected = "inner2inner4inner5";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test nested while loop with continue")
    public void testNestedWhileWithContinue() {
        Object returns = BRunUtil.invoke(positiveCompileResult, "testNestedWhileWithContinue");

        Assert.assertTrue(returns instanceof BString);

        String actual = returns.toString();
        String expected = "level2level1";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test type narrowing inside the while body")
    public void testTypeNarrowingInWhileBody() {
        Object returns = BRunUtil.invoke(positiveCompileResult, "testTypeNarrowingInWhileBody");

        Assert.assertTrue(returns instanceof BString);

        String actual = returns.toString();
        String expected = "foo1foo2foo3";
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testWhileStmtWithOnFailWithoutVariable() {
        BRunUtil.invoke(onfailCompileResult, "testWhileStmtWithOnFailWithoutVariable");
    }

    @Test(description = "Test while loop with a condition which evaluates to true")
    public void testWhileStmtWithOnFail() {
        Object[] args = {(5)};
        Object returns = BRunUtil.invoke(onfailCompileResult, "testWhileStmtWithFail", args);

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), " Value: 1 Value: 2 Value: 3-> error caught. " +
                "Hence value returning", "mismatched output value");
    }

    @Test(description = "Test while loop with a condition which evaluates to true")
    public void testWhileStmtWithCheck() {
        Object[] args = {(5)};
        Object returns = BRunUtil.invoke(onfailCompileResult, "testWhileStmtWithCheck", args);

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), " Value: 1 Value: 2 Value: 3-> error caught. " +
                "Hence value returning", "mismatched output value");
    }

    @Test(description = "Test nested while loop with break 2")
    public void testNestedWhileStmtWithFail() {
        Object returns = BRunUtil.invoke(onfailCompileResult, "testNestedWhileStmtWithFail");

        Assert.assertTrue(returns instanceof BString);

        String actual = returns.toString();
        String expected = "level3-> error caught at level 3, level2-> error caught at level 2, " +
                "level1-> error caught at level 1.";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test while loop termination with fail statement")
    public void testWhileStmtLoopEndingWithFail() {
        Object[] args = {(5)};
        Object returns = BRunUtil.invoke(onfailCompileResult, "testWhileStmtLoopEndingWithFail", args);

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), " Value: 1-> error caught. " +
                "Hence value returning-> reached end", "mismatched output value");
    }

    @Test(description = "Test nested while loop termination with multiple fail statements")
    public void testNestedWhileStmtLoopTerminationWithFail() {
        Object returns = BRunUtil.invoke(onfailCompileResult, "testNestedWhileStmtLoopTerminationWithFail");

        Assert.assertTrue(returns instanceof BString);

        String actual = returns.toString();
        String expected = "level3-> error caught at level 3, level2-> error caught at level 2, " +
                "level1-> error caught at level 1.";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check not incompatible types and reachable statements.")
    public void testNegative1() {
        int index = 0;
        BAssertUtil.validateError(onfailNegativeCompileResult, index++, "unreachable code", 17, 6);
        BAssertUtil.validateWarning(onfailNegativeCompileResult, index++, "unused variable 'e'", 19, 4);
        BAssertUtil.validateError(onfailNegativeCompileResult, index++, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 34, 4);
        BAssertUtil.validateWarning(onfailNegativeCompileResult, index++, "unused variable 'e'", 34, 4);
        BAssertUtil.validateWarning(onfailNegativeCompileResult, index++, "unused variable 'e'", 49, 4);
        BAssertUtil.validateWarning(onfailNegativeCompileResult, index++, "unused variable 'e'", 65, 4);
        BAssertUtil.validateError(onfailNegativeCompileResult, index++, "unreachable code", 68, 7);
        BAssertUtil.validateError(onfailNegativeCompileResult, index++, "this function must return a result", 83, 1);
        BAssertUtil.validateError(onfailNegativeCompileResult, index++, "incompatible error definition type: " +
                "'ErrorTypeB' will not be matched to 'ErrorTypeA'", 102, 4);
        BAssertUtil.validateError(onfailNegativeCompileResult, index++, "unreachable code", 116, 9);
        BAssertUtil.validateError(onfailNegativeCompileResult, index++, "unreachable code", 118, 5);
        Assert.assertEquals(onfailNegativeCompileResult.getDiagnostics().length, index);
    }

    @Test(description = "Test type narrowing for while statement")
    public void testWhileStmtTypeNarrowing() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/statements/whilestatement/while_stmt_type_narrowing.bal");
        int index = 0;
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'int', found '(int|string|true)?'", 43, 13);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'int', found '(int|string)'", 53, 17);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'float', found '(float|boolean)'", 56, 19);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'int', found '(int|string)'", 65, 17);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'int', found '(int|string)'", 69, 17);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'string', found '(int|string)'", 83, 16);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'A', found 'B'", 153, 15);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'B', found 'A'", 161, 15);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected '(X|V)', found '(X|Y)'", 261, 17);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected '[int]', found '[string]'", 271, 19);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected '[int]', found '([string] & readonly)'", 282, 19);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'int[]', found 'string[]'", 288, 19);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'int[]', found '(string[] & readonly)'", 298, 19);
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'string', found '(boolean|string)'", 314, 20); // issue #34307
        BAssertUtil.validateError(compileResult, index++,
                "incompatible types: expected 'string', found '(boolean|string)'", 327, 20); // issue #34307
        Assert.assertEquals(compileResult.getDiagnostics().length, index);
    }

    @Test(description = "Test type narrowing for while statement with no errors")
    public void testWhileStmtTypeNarrowPositive() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/statements/whilestatement/while_stmt_type_narrowing_positive.bal");
        Object returns = BRunUtil.invoke(compileResult, "testWhileStmtTypeNarrow");
        Assert.assertTrue((Boolean) returns);
    }
}
