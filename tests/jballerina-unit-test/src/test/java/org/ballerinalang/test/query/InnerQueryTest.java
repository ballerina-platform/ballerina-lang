/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * This contains methods to test nested query expressions.
 *
 * @since Swan Lake
 */
@Test(groups = {"disableOnOldParser"})
public class InnerQueryTest {
    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/inner-queries.bal");
        negativeResult = BCompileUtil.compile("test-src/query/inner-queries-negative.bal");
    }

    @Test(description = "Test multiple join clauses with inner queries")
    public void testMultipleJoinClauses() {
        BValue[] values = BRunUtil.invoke(result, "testMultipleJoinClauses");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test multiple join clauses with inner queries")
    public void testMultipleJoinClausesWithInnerQueries1() {
        BValue[] values = BRunUtil.invoke(result, "testMultipleJoinClausesWithInnerQueries1");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test multiple join clauses with inner queries")
    public void testMultipleJoinClausesWithInnerQueries2() {
        BValue[] values = BRunUtil.invoke(result, "testMultipleJoinClausesWithInnerQueries2");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test multiple nested join clauses with a query action")
    public void testMultipleJoinClausesWithInnerQueries3() {
        BValue[] values = BRunUtil.invoke(result, "testMultipleJoinClausesWithInnerQueries3");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test multiple join clauses with inner queries")
    public void testMultipleJoinClausesWithInnerQueries4() {
        BValue[] values = BRunUtil.invoke(result, "testMultipleJoinClausesWithInnerQueries4");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test multiple clauses with inner queries and xml")
    public void testMultipleJoinClausesWithInnerQueries5() {
        BValue[] values = BRunUtil.invoke(result, "testMultipleJoinClausesWithInnerQueries5");
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test negative scenarios for inner queries")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        int i = 0;
        validateError(negativeResult, i++, "undefined symbol 'deptName'", 71, 29);
        validateError(negativeResult, i++, "undefined symbol 'psn'", 88, 16);
        validateError(negativeResult, i, "undefined symbol 'emp'", 88, 30);
    }

}
