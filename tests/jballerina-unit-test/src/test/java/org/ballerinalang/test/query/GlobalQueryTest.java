/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.query;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test module level query expressions and query actions.
 *
 * @since Swan Lake
 */
public class GlobalQueryTest {

    private CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/global-queries.bal");
        negativeResult = BCompileUtil.compile("test-src/query/global-queries-negative.bal");
    }

    @Test(description = "Test module level simple join clause with record variable")
    public void testGlobalQuery1() {
        Object values = BRunUtil.invoke(result, "testGlobalQuery1");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test module level simple join clause with function in an equals")
    public void testGlobalQuery2() {
        Object values = BRunUtil.invoke(result, "testGlobalQuery2");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test module level multiple join clauses with inner queries")
    public void testGlobalQuery3() {
        Object values = BRunUtil.invoke(result, "testGlobalQuery3");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test module level let clause")
    public void testGlobalQuery4() {
        Object values = BRunUtil.invoke(result, "testGlobalQuery4");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test nested query-expr as a collection of a query-expr")
    public void testGlobalQuery5() {
        Object values = BRunUtil.invoke(result, "testGlobalQuery5");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test negative scenarios for module level queries")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
        BAssertUtil.validateError(negativeResult, 0, "action invocation as an expression not allowed here", 21, 9);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
