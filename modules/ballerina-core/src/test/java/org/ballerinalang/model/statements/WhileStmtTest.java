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
package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
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
public class WhileStmtTest {
    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/statements/while-stmt.bal");
    }

    @Test(description = "Test while loop with a condition which evaluates to true")
    public void testWhileStmtConditionTrue() {
        BValue[] args = {new BInteger(10), new BInteger(1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testWhileStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 100;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test while loop with a condition which evaluates to false")
    public void testWhileStmtConditionFalse() {
        BValue[] args = {new BInteger(10), new BInteger(11)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testWhileStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 0;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check the scope managing in while block")
    public void testWhileBlockScopes() {
        BValue[] args = { new BInteger(1) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testWhileScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        BInteger actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 200, "mismatched output value");

        args = new BValue[] { new BInteger(2) };
        returns = BLangFunctions.invokeNew(programFile, "testWhileScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 400, "mismatched output value");
    }

    @Test(description = "Check the scope managing in while block with ifelse")
    public void testWhileBlockScopesWithIf() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testWhileScopeWithIf");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type of return param1 mismatched");
        Assert.assertSame(returns[1].getClass(), BFloat.class, "Class type of return param2 mismatched");
        BInteger actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 2, "mismatched output value");
        BFloat sum = (BFloat) returns[1];
        Assert.assertEquals(sum.floatValue(), 30.0, "mismatched output value");
    }

    @Test(description = "Test while statement with incompatible types",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "while-stmnt-with-incompatible-types.bal:2: incompatible types: " +
                    "expected 'boolean', found 'string'", dependsOnMethods = {"testWhileStmtConditionFalse",
            "testWhileStmtConditionTrue"})
    public void testMapAccessWithIndex() {
        BTestUtils.getProgramFile("lang/statements/while-stmnt-with-incompatible-types.bal");
    }
}
