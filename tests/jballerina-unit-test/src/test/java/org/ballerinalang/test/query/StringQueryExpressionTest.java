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

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Alex Ranjan John ");
    }

    @Test(description = "Test query expression with where giving string result")
    public void testQueryExprWithWhereForStringResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithWhereForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Ranjan John ");
    }

    @Test(description = "Test simple query expression with limit clause giving string result")
    public void testQueryExprWithInnerJointForStringResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithInnerJointForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Alex 1 Ranjan 2 ");
    }

    @Test(description = "Test simple query expression with multiple from clauses giving string result")
    public void testQueryExprWithMultipleFromForStringResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleFromForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Alex 1 Alex 2 Ranjan 1 Ranjan 2 ");
    }

    @Test(description = "Test simple query expression with stream giving string result")
    public void testQueryExprWithStreamForStringResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithStreamForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Ferdinand 2 ");
    }

    @Test(description = "Test simple query expression with string or error result")
    public void testSimpleQueryExprForStringOrNilResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForStringOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Alex Ranjan John ");
    }

    @Test(description = "Test query expression with where giving string or error result")
    public void testQueryExprWithWhereForStringOrNilResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithWhereForStringOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Ranjan John ");
    }

    @Test(description = "Test simple query expression with limit clause giving string or error result")
    public void testQueryExprWithInnerJointForStringOrNilResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithInnerJointForStringOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Alex 1 Ranjan 2 ");
    }

    @Test(description = "Test simple query expression with multiple from clauses giving string or error result")
    public void testQueryExprWithMultipleFromForStringOrNilResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleFromForStringOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Alex 1 Alex 2 Ranjan 1 Ranjan 2 ");
    }

    @Test(description = "Test simple query expression with stream giving string or error result")
    public void testQueryExprWithStreamForStringOrNilResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithStreamForStringOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Ferdinand 2 ");
    }

    @Test(description = "Test simple query expression with var for string result")
    public void testQueryExprWithVarForStringResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithVarForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Ranjan ");
    }

    @Test(description = "Test simple query expression with var for string result")
    public void testQueryExprWithListForStringResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithListForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Ranjan ");
        Assert.assertEquals(returnValues[1].stringValue(), "John ");
    }

    @Test(description = "Test simple query expression with var for string result")
    public void testQueryExprWithUnionTypeForStringResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithUnionTypeForStringResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Ranjan John ");
    }

    @Test(description = "Test simple query expression with var for string result")
    public void testQueryExprWithUnionTypeForStringResult2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithUnionTypeForStringResult2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Ranjan ");
        Assert.assertEquals(returnValues[1].stringValue(), "John ");
    }
}
