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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the static/constant value patterns with match statement in Ballerina.
 *
 * @since 0.983.0
 */
public class MatchStatementStaticPatternsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/static-match-patterns.bal");
    }

    @Test(description = "Test basics of static pattern match statement 1")
    public void testMatchStatementBasics1() {

        BValue[] returns = BRunUtil.invoke(result, "testStaticMatchPatternsBasic1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray results = (BStringArray) returns[0];

        int i = -1;
        String msg = "Value is ";
        Assert.assertEquals(results.get(++i), msg + "'12'");
        Assert.assertEquals(results.get(++i), msg + "'Hello'");
        Assert.assertEquals(results.get(++i), msg + "'true'");
        Assert.assertEquals(results.get(++i), msg + "'15'");
        Assert.assertEquals(results.get(++i), msg + "'HelloAgain'");
        Assert.assertEquals(results.get(++i), msg + "'false'");
        Assert.assertEquals(results.get(++i), msg + "'Default'");
    }

    @Test(description = "Test basics of static pattern match statement 2")
    public void testMatchStatementBasics2() {

        BValue[] returns = BRunUtil.invoke(result, "testStaticMatchPatternsBasic2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BStringArray.class);

        BStringArray results = (BStringArray) returns[0];

        int i = -1;
        String msg = "Value is ";
        Assert.assertEquals(results.get(++i), msg + "'12'");
        Assert.assertEquals(results.get(++i), msg + "'15 & HelloWorld'");
        Assert.assertEquals(results.get(++i), msg + "'HelloAgain & 34'");
        Assert.assertEquals(results.get(++i), msg + "'Default'");
        Assert.assertEquals(results.get(++i), msg + "'15 & 34'");
        Assert.assertEquals(results.get(++i), msg + "'true'");
    }


}

