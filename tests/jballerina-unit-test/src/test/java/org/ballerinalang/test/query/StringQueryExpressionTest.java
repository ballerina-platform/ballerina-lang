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

/**
 * This contains methods to test query expression with string result.
 *
 * @since 1.3.0
 */
public class StringQueryExpressionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/string-query-expression.bal");
    }

    @Test(description = "Test simple query expression with string result")
    public void testSimpleQueryExprForStringResult() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Alex Ranjan John ");
    }

    @Test(description = "Test query expression with where giving string result")
    public void testQueryExprWithWhereForStringResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithWhereForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Ranjan John ");
    }

    @Test(description = "Test simple query expression with limit clause giving string result")
    public void testQueryExprWithInnerJointForStringResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithInnerJointForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Alex 1 Ranjan 2 ");
    }

    @Test(description = "Test simple query expression with multiple from clauses giving string result")
    public void testQueryExprWithMultipleFromForStringResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleFromForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Alex 1 Alex 2 Ranjan 1 Ranjan 2 ");
    }

    @Test(description = "Test simple query expression with stream giving string result")
    public void testQueryExprWithStreamForStringResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithStreamForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Ferdinand 2 ");
    }

    @Test(description = "Test simple query expression with string or error result")
    public void testSimpleQueryExprForStringOrNilResult() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForStringOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Alex Ranjan John ");
    }

    @Test(description = "Test query expression with where giving string or error result")
    public void testQueryExprWithWhereForStringOrNilResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithWhereForStringOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Ranjan John ");
    }

    @Test(description = "Test simple query expression with limit clause giving string or error result")
    public void testQueryExprWithInnerJointForStringOrNilResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithInnerJointForStringOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Alex 1 Ranjan 2 ");
    }

    @Test(description = "Test simple query expression with multiple from clauses giving string or error result")
    public void testQueryExprWithMultipleFromForStringOrNilResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleFromForStringOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Alex 1 Alex 2 Ranjan 1 Ranjan 2 ");
    }

    @Test(description = "Test simple query expression with stream giving string or error result")
    public void testQueryExprWithStreamForStringOrNilResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithStreamForStringOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Ferdinand 2 ");
    }

    @Test(description = "Test simple query expression with var for string result")
    public void testQueryExprWithVarForStringResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithVarForStringResult");
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test simple query expression with var for string result")
    public void testQueryExprWithListForStringResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithListForStringResult");
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test simple query expression with var for string result")
    public void testQueryExprWithUnionTypeForStringResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithUnionTypeForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Ranjan John ");
    }

    @Test(description = "Test simple query expression with var for string result")
    public void testQueryExprWithUnionTypeForStringResult2() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithUnionTypeForStringResult2");
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test method access with casting in select clause")
    public void testMethodAccessWithCasting() {
        CompileResult resultV2 = BCompileUtil.compile("test-src/query/string-query-expression-v2.bal");
        Object returnValues = BRunUtil.invoke(resultV2, "testMethodAccessWithCasting");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "Everyday Italian|Harry Potter|XQuery Kick Start|Learning XML|");
    }

    @Test(description = "Test query expression with limit clause")
    public void testQueryExprWithLimitForStringResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithLimitForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Ranjan ");
    }

    @Test(description = "Test query expression with limit clause-v2")
    public void testQueryExprWithLimitForStringResultV2() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithLimitForStringResultV2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Ranjan John ");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
