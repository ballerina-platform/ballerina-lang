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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test query expression/query action with multiple order by clauses.
 *
 * @since 2.0.0
 */
public class MultipleOrderByClauseTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/multiple-order-by-clauses.bal");
    }

    @Test(description = "Test query expression with multiple order by clauses")
    public void testQueryExprWithMultipleOrderByClauses() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleOrderByClauses");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expression with multiple from and order by clauses")
    public void testQueryExprWithMultipleFromAndMultipleOrderByClauses() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleFromAndMultipleOrderByClauses");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expression with join and multiple order by clauses")
    public void testQueryExprWithJoinAndMultipleOrderByClauses() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithJoinAndMultipleOrderByClauses");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expression with inner queries and multiple order by clauses")
    public void testQueryExprWithInnerQueriesAndMultipleOrderByClauses() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithInnerQueriesAndMultipleOrderByClauses");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expression with multiple limit and multiple order by clauses")
    public void testQueryExprWithMultipleOrderByAndMultipleLimitClauses() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleOrderByAndMultipleLimitClauses");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query action with multiple order by clauses")
    public void testQueryActionWithMultipleOrderByClauses() {
        Object returnValues = BRunUtil.invoke(result, "testQueryActionWithMultipleOrderByClauses");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expression with multiple order by clauses returning table")
    public void testQueryExprWithMultipleOrderByClausesReturnTable() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleOrderByClausesReturnTable");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expression with multiple order by clauses returning stream")
    public void testQueryExprWithMultipleOrderByClausesReturnStream() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleOrderByClausesReturnStream");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }
}
