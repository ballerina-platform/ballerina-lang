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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This contains methods to test nested query expressions.
 *
 * @since Swan Lake
 */
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
        Object values = BRunUtil.invoke(result, "testMultipleJoinClauses");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test multiple join clauses with inner queries")
    public void testMultipleJoinClausesWithInnerQueries1() {
        Object values = BRunUtil.invoke(result, "testMultipleJoinClausesWithInnerQueries1");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test multiple join clauses with inner queries")
    public void testMultipleJoinClausesWithInnerQueries2() {
        Object values = BRunUtil.invoke(result, "testMultipleJoinClausesWithInnerQueries2");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test multiple nested join clauses with a query action")
    public void testMultipleJoinClausesWithInnerQueries3() {
        Object values = BRunUtil.invoke(result, "testMultipleJoinClausesWithInnerQueries3");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test multiple join clauses with inner queries")
    public void testMultipleJoinClausesWithInnerQueries4() {
        Object values = BRunUtil.invoke(result, "testMultipleJoinClausesWithInnerQueries4");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test multiple clauses with inner queries and xml")
    public void testMultipleJoinClausesWithInnerQueries5() {
        Object values = BRunUtil.invoke(result, "testMultipleJoinClausesWithInnerQueries5");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test negative scenarios for inner queries")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 6);
        int i = 0;
        validateError(negativeResult, i++, "undefined symbol 'deptName'", 71, 29);
        validateError(negativeResult, i++, "undefined symbol 'psn'", 88, 16);
        validateError(negativeResult, i++, "undefined symbol 'emp'", 88, 30);
        validateError(negativeResult, i++, "redeclared symbol 'item'", 105, 20);
        validateError(negativeResult, i++, "redeclared symbol 'item'", 117, 27);
        validateError(negativeResult, i, "redeclared symbol 'item'", 130, 26);
    }

    @Test(description = "Test type test in where clause")
    public void testTypeTestInWhereClause() {
        BRunUtil.invoke(result, "testTypeTestInWhereClause");
    }

    @Test(description = "Test query expression within select clause")
    public void testQueryExpWithinSelectClause() {
        BRunUtil.invoke(result, "testQueryExpWithinSelectClause1");
        BRunUtil.invoke(result, "testQueryExpWithinSelectClause2");
    }

    @Test(description = "Test query expression within query action")
    public void testQueryExpWithinQueryAction() {
        BRunUtil.invoke(result, "testQueryExpWithinQueryAction");
    }

    @Test
    public void testDestructuringRecordingBindingPatternWithAnIntersectionTypeInQueryAction() {
        BRunUtil.invoke(result, "testDestructuringRecordingBindingPatternWithAnIntersectionTypeInQueryAction");
    }

    @Test
    public void testQueryConstructingTableHavingInnerQueriesWithOnConflictClause() {
        BRunUtil.invoke(result, "testQueryConstructingTableHavingInnerQueriesWithOnConflictClause");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
