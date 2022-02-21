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

import io.ballerina.runtime.api.utils.StringUtils;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object[] args = {(15), 5};
        Object returns = BRunUtil.invoke(positiveCompileResult, "calculateExp1", args);

        
        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 100;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test break statement in a while loop, where break in a ")
    public void testBreakStmtConditionElseIf() {
        Object[] args = {(25), (15)};
        Object returns = BRunUtil.invoke(positiveCompileResult, "calculateExp1", args);

        
        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 1000;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test break statement in a while loop, where break not hits")
    public void testBreakStmtConditionFalse() {
        Object[] args = {(8), (5)};
        Object returns = BRunUtil.invoke(positiveCompileResult, "calculateExp1", args);

        
        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 40;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test break statement in a nested while loop.")
    public void testBreakStmtInNestedWhile() {
        Object[] args = {(12), (8)};
        Object returns = BRunUtil.invoke(positiveCompileResult, "nestedBreakStmt", args);

        
        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 140;
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testBreakWithForeach() {
        Object[] args = {StringUtils.fromString("break")};
        Object returns = BRunUtil.invoke(positiveCompileResult, "testBreakWithForeach", args);
        
        Assert.assertEquals(returns.toString(),
                "start->foreach0->foreachEnd0->foreach1->foreachEnd1->foreach2->foreachEnd2->foreach3->break->end");
    }

    @Test(description = "Check not reachable statements.")
    public void testNegative() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 3);
        BAssertUtil.validateError(negativeCompileResult, 0, "break cannot be used outside of a loop", 15, 5);
        BAssertUtil.validateError(negativeCompileResult, 1, "unreachable code", 31, 13);
        BAssertUtil.validateError(negativeCompileResult, 2, "break not allowed here", 45, 17);
    }

    @AfterClass
    public void tearDown() {
        positiveCompileResult = null;
        negativeCompileResult = null;
    }
}
