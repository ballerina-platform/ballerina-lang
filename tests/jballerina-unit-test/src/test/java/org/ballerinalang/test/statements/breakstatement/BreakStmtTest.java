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
package org.ballerinalang.test.statements.breakstatement;

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
public class BreakStmtTest {

    private CompileResult positiveCompileResult;
    private CompileResult negativeCompileResult;

    @BeforeClass
    public void setup() {
        positiveCompileResult = BCompileUtil.compile("test-src/statements/breakstatement/break-stmt.bal");
        negativeCompileResult = BCompileUtil.compile("test-src/statements/breakstatement/break-stmt-negative.bal");
    }

    @Test(description = "Test break statement in a while loop.")
    public void testBreakStmtConditionTrue() {
        BValue[] args = {new BInteger(15), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 100;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test break statement in a while loop, where break in a ")
    public void testBreakStmtConditionElseIf() {
        BValue[] args = {new BInteger(25), new BInteger(15)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1000;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test break statement in a while loop, where break not hits")
    public void testBreakStmtConditionFalse() {
        BValue[] args = {new BInteger(8), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 40;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test break statement in a nested while loop.")
    public void testBreakStmtInNestedWhile() {
        BValue[] args = {new BInteger(12), new BInteger(8)};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "nestedBreakStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 140;
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testBreakWithForeach() {
        BValue[] args = {new BString("break")};
        BValue[] returns = BRunUtil.invoke(positiveCompileResult, "testBreakWithForeach", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start->foreach0->foreachEnd0->foreach1->foreachEnd1->foreach2->foreachEnd2->foreach3->break->end");
    }

    @Test(description = "Check not reachable statements.")
    public void testNegative() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 2);
        BAssertUtil.validateError(negativeCompileResult, 0, "break cannot be used outside of a loop", 15, 5);
        BAssertUtil.validateError(negativeCompileResult, 1, "unreachable code", 31, 13);
    }
}
