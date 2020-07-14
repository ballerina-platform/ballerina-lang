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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
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
 * @since 2.0.0
 */
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

        Assert.assertEquals(returnValues.length, 11, "Expected events are not received");

        BMap<String, BValue> student1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> student2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> student3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> student4 = (BMap<String, BValue>) returnValues[3];
        BMap<String, BValue> student5 = (BMap<String, BValue>) returnValues[4];
        BMap<String, BValue> student6 = (BMap<String, BValue>) returnValues[5];
        BMap<String, BValue> student7 = (BMap<String, BValue>) returnValues[6];
        BMap<String, BValue> student8 = (BMap<String, BValue>) returnValues[7];
        BMap<String, BValue> student9 = (BMap<String, BValue>) returnValues[8];
        BMap<String, BValue> student10 = (BMap<String, BValue>) returnValues[9];
        BMap<String, BValue> student11 = (BMap<String, BValue>) returnValues[10];

        Assert.assertEquals(student1.get("firstname").stringValue(), "Ranjan");
        Assert.assertEquals(student2.get("lastname").stringValue(), "{lname:\"Herman\"}");
        Assert.assertEquals(student3.get("addressList").stringValue(), "[{addr:\"C\"}, {addr:\"A\"}]");
        Assert.assertEquals(student4.get("addressList").stringValue(), "[{addr:\"C\"}, {addr:\"B\"}]");
        Assert.assertEquals(student4.get("userTokens").stringValue(), "{\"one\":2.0, \"two\":3.0, \"three\":3.0}");
        Assert.assertEquals(student5.get("addressList").stringValue(), "[{addr:\"C\"}, {addr:\"B\"}]");
        Assert.assertEquals(student5.get("userTokens").stringValue(), "{\"one\":2.0, \"two\":3.0, \"three\":3.0}");
        Assert.assertEquals(student6.get("userTokens").stringValue(), "{\"one\":2.0, \"two\":3.0, \"three\":2.0}");
        Assert.assertEquals(student7.get("userTokens").stringValue(), "{\"one\":2.0, \"two\":2.0, \"three\":5.0}");
        Assert.assertEquals(student8.get("firstname").stringValue(), "Nina");
        Assert.assertEquals(student8.get("isUndergrad").stringValue(), "false");
        Assert.assertEquals(student9.get("firstname").stringValue(), "Nina");
        Assert.assertEquals(student9.get("isUndergrad").stringValue(), "true");
        Assert.assertEquals(student10.get("firstname").stringValue(), "Zander");
        Assert.assertEquals(student11.get("firstname").stringValue(), "Xyla");
    }

    @Test(description = "Test query expression with order by and other clauses")
    public void testQueryExprWithLetWhereAndOrderByClauses() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithLetWhereAndOrderByClauses");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> student1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> student2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> student3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(student1.get("lastname").stringValue(), "{lname:\"George\"}");
        Assert.assertEquals(student2.get("lastname").stringValue(), "{lname:\"Herman\"}");
        Assert.assertEquals(student2.get("isUndergrad").stringValue(), "false");
        Assert.assertEquals(student3.get("lastname").stringValue(), "{lname:\"Frost\"}");
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

    @Test(description = "Test negative scenarios for query expr with order by clause")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        int index = 0;

        validateError(negativeResult, index++, "undefined field 'lastname' in record 'Person'",
                18, 18);
        validateError(negativeResult, index++, "undefined field 'lastname' in record 'Person'",
                35, 18);
        validateError(negativeResult, index, "undefined field 'lastname' in record 'Person'",
                52, 18);
    }
}
