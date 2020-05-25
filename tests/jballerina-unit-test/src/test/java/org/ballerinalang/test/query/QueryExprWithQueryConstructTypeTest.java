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

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test query expression with query construct type.
 *
 * @since 1.3.0
 */
public class QueryExprWithQueryConstructTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query-expr-with-query-construct-type.bal");
    }

    @Test(description = "Test query expr returning a stream ")
    public void testSimpleQueryReturnStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with stream in from clause returning a stream ")
    public void testStreamInFromClauseWithReturnStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testStreamInFromClauseWithReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with multiple from, let and where clauses returning a stream ")
    public void testMultipleFromWhereAndLetReturnStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleFromWhereAndLetReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with inner join returning a stream ")
    public void testInnerJoinReturnStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testInnerJoinReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }
}
