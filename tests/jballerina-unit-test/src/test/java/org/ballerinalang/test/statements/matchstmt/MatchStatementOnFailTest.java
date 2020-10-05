/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.matchstmt;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the static/constant value patterns with match statement in Ballerina.
 *
 * @since 0.985.0
 */
public class MatchStatementOnFailTest {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/matchstmt_on_fail.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/matchstmt_on_fail_negative.bal");
    }

    @Test(description = "Test basics of static pattern match statement with fail statement")
    public void testStaticMatchPatternsWithFailStmt() {
        BValue[] returns = BRunUtil.invoke(result, "testStaticMatchPatternsWithFailStmt", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);

        BValueArray results = (BValueArray) returns[0];

        int i = -1;
        String msg = "Value is ";
        Assert.assertEquals(results.getString(++i), msg + "'12'");
        Assert.assertEquals(results.getString(++i), msg + "'Hello'");
        Assert.assertEquals(results.getString(++i), msg + "'true'");
        Assert.assertEquals(results.getString(++i), msg + "'15'");
        Assert.assertEquals(results.getString(++i), msg + "'error'");
        Assert.assertEquals(results.getString(++i), msg + "'false'");
        Assert.assertEquals(results.getString(++i), msg + "'Default'");
    }

    @Test(description = "Test basics of static pattern match statement with check expression")
    public void testStaticMatchPatternsWithCheckExpr() {

        BValue[] returns = BRunUtil.invoke(result, "testStaticMatchPatternsWithCheckExpr", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);

        BValueArray results = (BValueArray) returns[0];

        int i = -1;
        String msg = "Value is ";
        Assert.assertEquals(results.getString(++i), msg + "'12'");
        Assert.assertEquals(results.getString(++i), msg + "'Hello'");
        Assert.assertEquals(results.getString(++i), msg + "'true'");
        Assert.assertEquals(results.getString(++i), msg + "'15'");
        Assert.assertEquals(results.getString(++i), msg + "'error'");
        Assert.assertEquals(results.getString(++i), msg + "'false'");
        Assert.assertEquals(results.getString(++i), msg + "'Default'");
    }

    @Test(description = "Test basics of static pattern match statement 2")
    public void testNestedMatchPatternsWithFail() {

        BValue[] returns = BRunUtil.invoke(result, "testNestedMatchPatternsWithFail", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);

        BValueArray results = (BValueArray) returns[0];

        int i = -1;
        String msg = "Value is ";
        Assert.assertEquals(results.getString(++i), msg + "'12'");
        Assert.assertEquals(results.getString(++i), msg + "'15 & HelloWorld'");
        Assert.assertEquals(results.getString(++i), msg + "'error'");
        Assert.assertEquals(results.getString(++i), msg + "'Default'");
        Assert.assertEquals(results.getString(++i), msg + "'15 & 34'");
        Assert.assertEquals(results.getString(++i), msg + "'true'");
    }

    @Test(description = "Test basics of static pattern match statement 2")
    public void testNestedMatchPatternsWithCheck() {

        BValue[] returns = BRunUtil.invoke(result, "testNestedMatchPatternsWithCheck", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);

        BValueArray results = (BValueArray) returns[0];

        int i = -1;
        String msg = "Value is ";
        Assert.assertEquals(results.getString(++i), msg + "'12'");
        Assert.assertEquals(results.getString(++i), msg + "'15 & HelloWorld'");
        Assert.assertEquals(results.getString(++i), msg + "'error'");
        Assert.assertEquals(results.getString(++i), msg + "'Default'");
        Assert.assertEquals(results.getString(++i), msg + "'15 & 34'");
        Assert.assertEquals(results.getString(++i), msg + "'true'");
    }

    @Test(description = "Check not incompatible types and reachable statements.")
    public void testNegative1() {
        Assert.assertEquals(resultNegative.getErrorCount(), 5);
        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, "unreachable code", 29, 14);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 59, 7);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 88, 7);
        BAssertUtil.validateError(resultNegative, ++i, "unreachable code", 90, 9);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 124, 7);
    }
}
