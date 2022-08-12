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

/**
 * This contains methods to test simple query expression with from, where and select clauses.
 *
 * @since 1.2.0
 */
public class QueryExpressionWithVarTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/simple-query-with-var-type.bal");
    }

    @Test(description = "Test simple select clause - simple variable definition statement ")
    public void testSimpleSelectQueryWithSimpleVariable() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithSimpleVariable");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 3, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals((person3.get(StringUtils.fromString("age"))), 33L);
    }

    @Test(description = "Test simple select clause - record variable definition statement ")
    public void testSimpleSelectQueryWithRecordVariable() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithRecordVariable");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals((person3.get(StringUtils.fromString("age"))), 33L);
    }

    @Test(description = "Test simple select clause - record variable definition statement v2 ")
    public void testSimpleSelectQueryWithRecordVariableV2() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithRecordVariableV2");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals((person3.get(StringUtils.fromString("age"))), 33L);
    }

    @Test(description = "Test simple select clause - record variable definition statement v3 ")
    public void testSimpleSelectQueryWithRecordVariableV3() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithRecordVariableV3");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals((person3.get(StringUtils.fromString("age"))), 33L);
    }

    @Test(description = "Test simple select clause with a where clause")
    public void testSimpleSelectQueryWithWhereClause() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithWhereClause");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person2.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals((person1.get(StringUtils.fromString("age"))), 30L);
        Assert.assertEquals((person2.get(StringUtils.fromString("age"))), 33L);
    }

    @Test(description = "Test Query expression for primitive types ")
    public void testQueryExpressionForPrimitiveType() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionForPrimitiveType");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test Query expression with select expression ")
    public void testQueryExpressionWithSelectExpression() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionWithSelectExpression");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test filtering null values from query expression")
    public void testFilteringNullElements() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testFilteringNullElements");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals((person1.get(StringUtils.fromString("age"))), 23L);
        Assert.assertEquals(person2.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals((person2.get(StringUtils.fromString("age"))), 30L);
    }

    @Test(description = "Test filtering map with from query expression")
    public void testMapWithArity() {
        Object returnValues = BRunUtil.invoke(result, "testMapWithArity");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test selecting values in a JSON array from query expression")
    public void testJSONArrayWithArity() {
        Object returnValues = BRunUtil.invoke(result, "testJSONArrayWithArity");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test filtering values in a tuple from query expression")
    public void testArrayWithTuple() {
        Object returnValues = BRunUtil.invoke(result, "testArrayWithTuple");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test simple select clause - simple variable definition statement ")
    public void testQueryExpressionWithVarType() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testQueryExpressionWithVarType");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals((person3.get(StringUtils.fromString("age"))), 33L);
        Assert.assertEquals(person3.get(StringUtils.fromString("teacherId")).toString(), "TER1200");
    }

    @Test(description = "Test simple select clause - with spread operator ")
    public void testSimpleSelectQueryWithSpreadOperator() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithSpreadOperator");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 3, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals((person3.get(StringUtils.fromString("age"))), 33L);
    }

    @Test(description = "Test simple select clause - with spread operator v2 ")
    public void testQueryExpressionWithSpreadOperatorV2() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testQueryExpressionWithSpreadOperatorV2");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals((person3.get(StringUtils.fromString("age"))), 33L);
        Assert.assertEquals(person3.get(StringUtils.fromString("teacherId")).toString(), "TER1200");
    }

    @Test(description = "Use a stream with query expression")
    public void testQueryWithStream() {
        Object returnValues = BRunUtil.invoke(result, "testQueryWithStream");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test Query expression returning a stream ")
    public void testSimpleSelectQueryReturnStream() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test variable shadowing with query expressions (with global variables)")
    public void testVariableShadowingWithQueryExpressions1() {
        Object returnValues = BRunUtil.invoke(result, "testVariableShadowingWithQueryExpressions1");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test variable shadowing with query expressions (with local variables)")
    public void testVariableShadowingWithQueryExpressions2() {
        Object returnValues = BRunUtil.invoke(result, "testVariableShadowingWithQueryExpressions2");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test simple select clause - with table")
    public void testSimpleSelectQueryWithTable() {
        BRunUtil.invoke(result, "testSimpleSelectQueryWithTable");
    }

    @Test(description = "Test query constructing table with var")
    public void testQueryConstructingTableWithVar() {
        BRunUtil.invoke(result, "testQueryConstructingTableWithVar");
    }

    @Test(description = "Test method call expressions on query expressions")
    public void testMethodCallExprsOnQueryExprs() {
        BRunUtil.invoke(result, "testMethodCallExprsOnQueryExprs");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
