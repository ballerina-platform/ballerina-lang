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
package org.wso2.ballerina.lang.statements;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * This contains methods to test different behaviours of the while loop statement.
 *
 * @since 0.8.0
 */
public class BreakStmtTest {
    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/statements/break-stmt.bal");
    }

    @Test(description = "Test break statement in a while loop.")
    public void testBreakStmtConditionTrue() {
        BValue[] args = {new BInteger(15), new BInteger(5)};
        BValue[] returns = Functions.invoke(bFile, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 100;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test break statement in a while loop, where break in a ")
    public void testBreakStmtConditionElseIf() {
        BValue[] args = {new BInteger(25), new BInteger(15)};
        BValue[] returns = Functions.invoke(bFile, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 1000;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test break statement in a while loop, where break not hits")
    public void testBreakStmtConditionFalse() {
        BValue[] args = {new BInteger(8), new BInteger(5)};
        BValue[] returns = Functions.invoke(bFile, "calculateExp1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 40;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check not reachable statements.", expectedExceptions = SemanticException.class,
    expectedExceptionsMessageRegExp = ".*break-stmt-negative.bal:15: unreachable statement.*")
    public void testNegative() {
        ParserUtils.parseBalFile("lang/statements/break-stmt-negative.bal");
    }

}
