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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This contains methods to test order by clause in query expression.
 *
 * @since Swan Lake
 */
@Test()
public class OrderByClauseTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/order-by-clause.bal");
        negativeResult = BCompileUtil.compile("test-src/query/order-by-clause-negative.bal");
    }

    @Test(description = "Test query expression with order by")
    public void testQueryExprWithOrderByClause() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClause");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);

    }

    @Test(description = "Test query expression with order by")
    public void testQueryExprWithOrderByClause2() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClause2");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expression with order by and other clauses")
    public void testQueryExprWithOrderByClause3() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testQueryExprWithOrderByClause3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 4, "Expected events are not received");

        BMap<String, Object> customer1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> customer2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> customer3 = (BMap<String, Object>) returnValues.get(2);
        BMap<String, Object> customer4 = (BMap<String, Object>) returnValues.get(3);

        Assert.assertEquals((customer1.get(StringUtils.fromString("id"))), 7L);
        Assert.assertEquals(customer1.get(StringUtils.fromString("name")).toString(), "Johns");
        Assert.assertEquals((customer2.get(StringUtils.fromString("id"))), 5L);
        Assert.assertEquals(customer2.get(StringUtils.fromString("name")).toString(), "Johns");
        Assert.assertEquals((customer3.get(StringUtils.fromString("id"))), 1L);
        Assert.assertEquals(customer3.get(StringUtils.fromString("name")).toString(), "Johns");
        Assert.assertEquals((customer4.get(StringUtils.fromString("id"))), 0L);
        Assert.assertEquals(customer4.get(StringUtils.fromString("name")).toString(), "Johns");
    }

    @Test(description = "Test query expr with order by clause returning a table")
    public void testQueryExprWithOrderByClauseReturnTable() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with order by clause returning a stream")
    public void testQueryExprWithOrderByClauseReturnStream() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expression having join with order by")
    public void testQueryExprWithOrderByClauseAndJoin() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testQueryExprWithOrderByClauseAndJoin");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 4, "Expected events are not received");

        BMap<String, Object> customer1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> customer2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> customer3 = (BMap<String, Object>) returnValues.get(2);
        BMap<String, Object> customer4 = (BMap<String, Object>) returnValues.get(3);

        Assert.assertEquals(customer1.get(StringUtils.fromString("name")).toString(), "Frank");
        Assert.assertEquals((customer1.get(StringUtils.fromString("noOfItems"))), 5L);
        Assert.assertEquals(customer2.get(StringUtils.fromString("name")).toString(), "Amy");
        Assert.assertEquals((customer2.get(StringUtils.fromString("noOfItems"))), 12L);
        Assert.assertEquals(customer3.get(StringUtils.fromString("name")).toString(), "Frank");
        Assert.assertEquals((customer3.get(StringUtils.fromString("noOfItems"))), 25L);
        Assert.assertEquals(customer4.get(StringUtils.fromString("name")).toString(), "Frank");
        Assert.assertEquals((customer4.get(StringUtils.fromString("noOfItems"))), 25L);
    }

    @Test(description = "Test query expr with order by clause with user defined function in order-key")
    public void testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseHavingUser" +
                "DefinedOrderKeyFunction");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with order by clause with user defined function in order-key")
    public void testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction2() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseHavingUser" +
                "DefinedOrderKeyFunction2");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expression with join, order by and user defined order-key function")
    public void testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction3() {
        BArray returnValues = (BArray) BRunUtil.invoke(result,
                "testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 4, "Expected events are not received");

        BMap<String, Object> customer1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> customer2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> customer3 = (BMap<String, Object>) returnValues.get(2);
        BMap<String, Object> customer4 = (BMap<String, Object>) returnValues.get(3);

        Assert.assertEquals(customer1.get(StringUtils.fromString("name")).toString(), "Frank");
        Assert.assertEquals((customer1.get(StringUtils.fromString("noOfItems"))), 25L);
        Assert.assertEquals(customer2.get(StringUtils.fromString("name")).toString(), "Frank");
        Assert.assertEquals((customer2.get(StringUtils.fromString("noOfItems"))), 25L);
        Assert.assertEquals(customer3.get(StringUtils.fromString("name")).toString(), "Amy");
        Assert.assertEquals((customer3.get(StringUtils.fromString("noOfItems"))), 12L);
        Assert.assertEquals(customer4.get(StringUtils.fromString("name")).toString(), "Frank");
        Assert.assertEquals((customer4.get(StringUtils.fromString("noOfItems"))), 5L);
    }

    @Test(description = "Test query expression with order by clause and inner query")
    public void testQueryExprWithOrderByClauseAndInnerQueries() {
        BArray returnValues = (BArray) BRunUtil.invoke(result,
                "testQueryExprWithOrderByClauseAndInnerQueries");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 3, "Expected events are not received");

        BMap<String, Object> customer1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> customer2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> customer3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(customer1.get(StringUtils.fromString("name")).toString(), "Jennifer");
        Assert.assertEquals((customer1.get(StringUtils.fromString("age"))), 23L);
        Assert.assertEquals((customer1.get(StringUtils.fromString("noOfItems"))), 62L);
        Assert.assertEquals(customer2.get(StringUtils.fromString("name")).toString(), "Zeth");
        Assert.assertEquals((customer2.get(StringUtils.fromString("age"))), 50L);
        Assert.assertEquals((customer2.get(StringUtils.fromString("noOfItems"))), 30L);
        Assert.assertEquals(customer3.get(StringUtils.fromString("name")).toString(), "Zeth");
        Assert.assertEquals((customer3.get(StringUtils.fromString("age"))), 50L);
        Assert.assertEquals((customer3.get(StringUtils.fromString("noOfItems"))), 25L);
    }

    @Test(description = "Test query expression with order by clause and inner query")
    public void testQueryExprWithOrderByClauseAndInnerQueries2() {
        BArray returnValues = (BArray) BRunUtil.invoke(result,
                "testQueryExprWithOrderByClauseAndInnerQueries2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");

        BMap<String, Object> customer1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> customer2 = (BMap<String, Object>) returnValues.get(1);

        Assert.assertEquals(customer1.get(StringUtils.fromString("name")).toString(), "Zeth");
        Assert.assertEquals((customer1.get(StringUtils.fromString("age"))), 50L);
        Assert.assertEquals((customer1.get(StringUtils.fromString("noOfItems"))), 30L);
        Assert.assertEquals(customer2.get(StringUtils.fromString("name")).toString(), "Jennifer");
        Assert.assertEquals((customer2.get(StringUtils.fromString("age"))), 23L);
        Assert.assertEquals((customer2.get(StringUtils.fromString("noOfItems"))), 20L);
    }

    @Test(description = "Test query expr with order by clause with NaN and Nil values in order-key")
    public void testQueryExprWithOrderByClauseHavingNaNNilValues() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseHavingNaNNilValues");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expr with order by clause return string")
    public void testQueryExprWithOrderByClauseReturnString() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseReturnString");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "Melina Kodel,Meghan Markle,Amy Melina,");
    }

    @Test(description = "Test query expr with order by clause return XML")
    public void testQueryExprWithOrderByClauseReturnXML() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseReturnXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<author>Dan Brown</author><author>Enid Blyton</author>");
    }

    @Test(description = "Test negative scenarios for query expr with order by clause")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        int index = 0;

        validateError(negativeResult, index++, "order by not supported for complex type fields, " +
                        "order key should belong to a basic type",
                35, 18);
        validateError(negativeResult, index++, "undefined symbol 'address'",
                35, 18);
        validateError(negativeResult, index, "order by not supported for complex type fields, " +
                        "order key should belong to a basic type",
                47, 18);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
