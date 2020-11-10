/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.statements.continuestatement;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test continue statement in a while loop.
 *
 * @since 0.89
 */
public class ContinueStmtTest {

    private CompileResult positiveCompileResult;
    private CompileResult negativeCompileResult;

    @BeforeClass
    public void setup() {
        positiveCompileResult = BCompileUtil.compile("test-src/statements/continuestatement/continue-stmt.bal");
        negativeCompileResult = BCompileUtil
                .compile("test-src/statements/continuestatement/continue-stmt-negative.bal");
    }

    @Test(description = "Test continue statement in a while loop.")
    public void testContinueStmtConditionTrue() {
        BValue[] args = {new BInteger(15), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 9;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test continue statement in a while loop, where continue not in execution path ")
    public void testContinueStmtConditionFalse() {
        BValue[] args = {new BInteger(25), new BInteger(15)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 10;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test continue statement in a nested while loop.")
    public void testContinueStmtInNestedWhileConditionTrue() {
        BValue[] args = {new BInteger(15), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "nestedContinueStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 666;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test continue statement in a nested while loop.")
    public void testContinueStmtInNestedWhileConditionFalse() {
        BValue[] args = {new BInteger(25), new BInteger(15)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "nestedContinueStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 2486;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check invalid continue statement location.")
    public void testNegative() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 2);
        BAssertUtil.validateError(negativeCompileResult, 0, "continue cannot be used outside of a loop", 15, 5);
        BAssertUtil.validateError(negativeCompileResult, 1, "unreachable code", 31, 13);
    }
}
