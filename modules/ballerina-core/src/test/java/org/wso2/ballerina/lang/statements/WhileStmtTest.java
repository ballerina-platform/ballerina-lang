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
package org.wso2.ballerina.lang.statements;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * This contains methods to test different behaviours of the while loop statement.
 *
 * @since 0.8.0
 */
public class WhileStmtTest {
    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/statements/while-stmt.bal");
    }

    @Test(description = "Test while loop with a condition which evaluates to true")
    public void testWhileStmtConditionTrue() {
        BValue[] args = {new BInteger(10), new BInteger(1)};
        BValue[] returns = Functions.invoke(bFile, "testWhileStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 100;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test while loop with a condition which evaluates to false")
    public void testWhileStmtConditionFalse() {
        BValue[] args = {new BInteger(10), new BInteger(11)};
        BValue[] returns = Functions.invoke(bFile, "testWhileStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 0;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Check the scope managing in while block")
    public void testWhileBlockScopes() {
        BValue[] args = { new BInteger(1) };
        BValue[] returns = Functions.invoke(bFile, "testWhileScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        BInteger actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 200, "mismatched output value");

        args = new BValue[] { new BInteger(2) };
        returns = Functions.invoke(bFile, "testWhileScope", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type mismatched");
        actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 400, "mismatched output value");
    }

    @Test(description = "Check the scope managing in while block with ifelse")
    public void testWhileBlockScopesWithIf() {
        BValue[] returns = Functions.invoke(bFile, "testWhileScopeWithIf");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Class type of return param1 mismatched");
        Assert.assertSame(returns[1].getClass(), BDouble.class, "Class type of return param2 mismatched");
        BInteger actual = (BInteger) returns[0];
        Assert.assertEquals(actual.intValue(), 2, "mismatched output value");
        BDouble sum = (BDouble) returns[1];
        Assert.assertEquals(sum.doubleValue(), 30.0, "mismatched output value");
    }

    @Test(description = "Test while statement with incompatible types",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "while-stmnt-with-incompatible-types.bal:2: incompatible types: " +
                    "expected 'boolean', found 'string'", dependsOnMethods = {"testWhileStmtConditionFalse",
            "testWhileStmtConditionTrue"})
    public void testMapAccessWithIndex() {
        ParserUtils.parseBalFile("lang/statements/while-stmnt-with-incompatible-types.bal");
    }
}
