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
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * This contains methods to test order by clause in query expression.
 *
 * @since Swan Lake
 */
@Test(groups = {"disableOnOldParser"})
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
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClause");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());

    }

    @Test(description = "Test query expression with order by")
    public void testQueryExprWithOrderByClause2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClause2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expression with order by and other clauses")
    public void testQueryExprWithOrderByClause3() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClause3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 4, "Expected events are not received");

        BMap<String, BValue> customer1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> customer2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> customer3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> customer4 = (BMap<String, BValue>) returnValues[3];

        Assert.assertEquals(((BInteger) customer1.get("id")).intValue(), 7);
        Assert.assertEquals(customer1.get("name").stringValue(), "Johns");
        Assert.assertEquals(((BInteger) customer2.get("id")).intValue(), 5);
        Assert.assertEquals(customer2.get("name").stringValue(), "Johns");
        Assert.assertEquals(((BInteger) customer3.get("id")).intValue(), 1);
        Assert.assertEquals(customer3.get("name").stringValue(), "Johns");
        Assert.assertEquals(((BInteger) customer4.get("id")).intValue(), 0);
        Assert.assertEquals(customer4.get("name").stringValue(), "Johns");
    }

    @Test(description = "Test query expr with order by clause returning a table")
    public void testQueryExprWithOrderByClauseReturnTable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseReturnTable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with order by clause returning a stream")
    public void testQueryExprWithOrderByClauseReturnStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expression having join with order by")
    public void testQueryExprWithOrderByClauseAndJoin() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseAndJoin");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 4, "Expected events are not received");

        BMap<String, BValue> customer1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> customer2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> customer3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> customer4 = (BMap<String, BValue>) returnValues[3];

        Assert.assertEquals(customer1.get("name").stringValue(), "Frank");
        Assert.assertEquals(((BInteger) customer1.get("noOfItems")).intValue(), 5);
        Assert.assertEquals(customer2.get("name").stringValue(), "Amy");
        Assert.assertEquals(((BInteger) customer2.get("noOfItems")).intValue(), 12);
        Assert.assertEquals(customer3.get("name").stringValue(), "Frank");
        Assert.assertEquals(((BInteger) customer3.get("noOfItems")).intValue(), 25);
        Assert.assertEquals(customer4.get("name").stringValue(), "Frank");
        Assert.assertEquals(((BInteger) customer4.get("noOfItems")).intValue(), 25);
    }

    @Test(description = "Test query expr with order by clause with user defined function in order-key")
    public void testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseHavingUser" +
                "DefinedOrderKeyFunction");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with order by clause with user defined function in order-key")
    public void testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseHavingUser" +
                "DefinedOrderKeyFunction2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expression with join, order by and user defined order-key function")
    public void testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction3() {
        BValue[] returnValues = BRunUtil.invoke(result,
                "testQueryExprWithOrderByClauseHavingUserDefinedOrderKeyFunction3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 4, "Expected events are not received");

        BMap<String, BValue> customer1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> customer2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> customer3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> customer4 = (BMap<String, BValue>) returnValues[3];

        Assert.assertEquals(customer1.get("name").stringValue(), "Frank");
        Assert.assertEquals(((BInteger) customer1.get("noOfItems")).intValue(), 25);
        Assert.assertEquals(customer2.get("name").stringValue(), "Frank");
        Assert.assertEquals(((BInteger) customer2.get("noOfItems")).intValue(), 25);
        Assert.assertEquals(customer3.get("name").stringValue(), "Amy");
        Assert.assertEquals(((BInteger) customer3.get("noOfItems")).intValue(), 12);
        Assert.assertEquals(customer4.get("name").stringValue(), "Frank");
        Assert.assertEquals(((BInteger) customer4.get("noOfItems")).intValue(), 5);
    }

    @Test(description = "Test query expression with order by clause and inner query")
    public void testQueryExprWithOrderByClauseAndInnerQueries() {
        BValue[] returnValues = BRunUtil.invoke(result,
                "testQueryExprWithOrderByClauseAndInnerQueries");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> customer1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> customer2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> customer3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(customer1.get("name").stringValue(), "Jennifer");
        Assert.assertEquals(((BInteger) customer1.get("age")).intValue(), 23);
        Assert.assertEquals(((BInteger) customer1.get("noOfItems")).intValue(), 62);
        Assert.assertEquals(customer2.get("name").stringValue(), "Zeth");
        Assert.assertEquals(((BInteger) customer2.get("age")).intValue(), 50);
        Assert.assertEquals(((BInteger) customer2.get("noOfItems")).intValue(), 30);
        Assert.assertEquals(customer3.get("name").stringValue(), "Zeth");
        Assert.assertEquals(((BInteger) customer3.get("age")).intValue(), 50);
        Assert.assertEquals(((BInteger) customer3.get("noOfItems")).intValue(), 25);
    }

    @Test(description = "Test query expression with order by clause and inner query")
    public void testQueryExprWithOrderByClauseAndInnerQueries2() {
        BValue[] returnValues = BRunUtil.invoke(result,
                "testQueryExprWithOrderByClauseAndInnerQueries2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap<String, BValue> customer1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> customer2 = (BMap<String, BValue>) returnValues[1];

        Assert.assertEquals(customer1.get("name").stringValue(), "Zeth");
        Assert.assertEquals(((BInteger) customer1.get("age")).intValue(), 50);
        Assert.assertEquals(((BInteger) customer1.get("noOfItems")).intValue(), 30);
        Assert.assertEquals(customer2.get("name").stringValue(), "Jennifer");
        Assert.assertEquals(((BInteger) customer2.get("age")).intValue(), 23);
        Assert.assertEquals(((BInteger) customer2.get("noOfItems")).intValue(), 20);
    }

    @Test(description = "Test query expr with order by clause with NaN and Nil values in order-key")
    public void testQueryExprWithOrderByClauseHavingNaNNilValues() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseHavingNaNNilValues");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expr with order by clause return string")
    public void testQueryExprWithOrderByClauseReturnString() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseReturnString");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "Melina Kodel,Meghan Markle,Amy Melina,");
    }

    @Test(description = "Test query expr with order by clause return XML")
    public void testQueryExprWithOrderByClauseReturnXML() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOrderByClauseReturnXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<author>Dan Brown</author><author>Enid Blyton</author>");
    }

    @Test(description = "Test negative scenarios for query expr with order by clause")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        int index = 0;

        validateError(negativeResult, index++, "order by not supported for complex type fields, " +
                        "order key should belong to a basic type",
                19, 18);
        validateError(negativeResult, index++, "undefined symbol 'address'",
                19, 18);
        validateError(negativeResult, index, "order by not supported for complex type fields, " +
                        "order key should belong to a basic type",
                31, 18);
    }
}
