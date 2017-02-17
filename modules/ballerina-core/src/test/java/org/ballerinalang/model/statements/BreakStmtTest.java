/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test different behaviours of the while loop statement.
 *
 * @since 0.8.0
 */
public class BreakStmtTest {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/statements/break-stmt.bal");
    }

    @Test(description = "Test break statement in a while loop.")
    public void testBreakStmtConditionTrue() {
        BValue[] args = {new BInteger(15), new BInteger(5)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 100;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test break statement in a while loop, where break in a ")
    public void testBreakStmtConditionElseIf() {
        BValue[] args = {new BInteger(25), new BInteger(15)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 1000;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test break statement in a while loop, where break not hits")
    public void testBreakStmtConditionFalse() {
        BValue[] args = {new BInteger(8), new BInteger(5)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 40;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check invalid break statement location.", expectedExceptions = SemanticException.class,
    expectedExceptionsMessageRegExp = ".*break statement is not allowed here*")
    public void testNegative() {
        BTestUtils.parseBalFile("lang/statements/break-stmt-negative.bal");
    }

    @Test(description = "Check not reachable statements.", expectedExceptions = SemanticException.class,
            expectedExceptionsMessageRegExp = ".*break-stmt-unreachable.bal:11.*.*unreachable statement*")
    public void testNegativeUnreachable() {
        BTestUtils.parseBalFile("lang/statements/break-stmt-unreachable.bal");
    }

}
