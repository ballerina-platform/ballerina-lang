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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This contains methods to test simple query expression with from, where and select clauses.
 *
 * @since 1.2.0
 */
public class SimpleQueryExpressionWithDefinedTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/simple-query-with-defined-type.bal");
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
        Assert.assertEquals(returnValues.size(), 3, "Expected events are not received");

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
        Assert.assertEquals(returnValues.size(), 3, "Expected events are not received");

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
        Assert.assertEquals(returnValues.size(), 3, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
    }

    @Test(description = "Test simple select clause with a where clause")
    public void testSimpleSelectQueryWithWhereClause() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithWhereClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");

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
        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");

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

    @Test(description = "Test from clause with a stream")
    public void testFromClauseWithStream() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testFromClauseWithStream");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person = (BMap<String, Object>) returnValues.get(0);

        Assert.assertEquals(person.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals((person.get(StringUtils.fromString("age"))), 40L);
    }

    @Test(description = "Test let clause with a stream")
    public void testSimpleSelectQueryWithLetClause() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithLetClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");

        BMap<String, Object> employee1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> employee2 = (BMap<String, Object>) returnValues.get(1);

        Assert.assertEquals(employee1.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(employee1.get(StringUtils.fromString("department")).toString(), "HR");
        Assert.assertEquals(employee1.get(StringUtils.fromString("company")).toString(), "WSO2");

        Assert.assertEquals(employee2.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(employee2.get(StringUtils.fromString("department")).toString(), "HR");
        Assert.assertEquals(employee2.get(StringUtils.fromString("company")).toString(), "WSO2");
    }

    @Test(description = "Use function return value in let clause")
    public void testFunctionCallInVarDeclLetClause() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testFunctionCallInVarDeclLetClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");
        BMap<String, Object> employee1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> employee2 = (BMap<String, Object>) returnValues.get(1);

        Assert.assertEquals(employee1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(employee1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(employee1.get(StringUtils.fromString("age")).toString(), "46");

        Assert.assertEquals(employee2.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(employee2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(employee2.get(StringUtils.fromString("age")).toString(), "60");
    }

    @Test(description = "Use value set in let with where clause")
    public void testUseOfLetInWhereClause() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testUseOfLetInWhereClause");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> employee = (BMap<String, Object>) returnValues.get(0);

        Assert.assertEquals(employee.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(employee.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(employee.get(StringUtils.fromString("age")).toString(), "44");
    }

    @Test(description = "Use a stream with query expression")
    public void testQueryWithStream() {
        Object returnValues = BRunUtil.invoke(result, "testQueryWithStream");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Query a stream with error")
    public void testQueryStreamWithError() {
        BRunUtil.invoke(result, "testQueryStreamWithError");
    }

    @Test(description = "Query a stream with different completion types")
    public void testQueryStreamWithDifferentCompletionTypes() {
        BRunUtil.invoke(result, "testQueryStreamWithDifferentCompletionTypes");
    }

    @Test(description = "Test anonymous record type, record type referencing, optional field, " +
            "changed order of the fields")
    public void testOthersAssociatedWithRecordTypes() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testOthersAssociatedWithRecordTypes");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");

        BMap teacher1 = (BMap) returnValues.get(0);
        BMap teacher2 = (BMap) returnValues.get(1);

        Assert.assertTrue(teacher1.get(StringUtils.fromString("classStudents")) instanceof BArray);
        Assert.assertTrue(teacher1.get(StringUtils.fromString("experience")) instanceof BMap);
        Assert.assertEquals(teacher1.get(StringUtils.fromString("classStudents")).toString(),
                "[{\"firstName\":\"Alex\",\"lastName\":\"George\",\"score\":82.5},{\"firstName\":\"Ranjan\"," +
                        "\"lastName\":\"Fonseka\",\"score\":90.6}]");
        Assert.assertEquals(teacher1.get(StringUtils.fromString("experience")).toString(),
                "{\"duration\":10,\"qualitifications\":\"B.Sc.\"}");
        Assert.assertEquals(teacher1.toString(),
                "{\"classStudents\":[{\"firstName\":\"Alex\",\"lastName\":\"George\",\"score\":82.5}," +
                        "{\"firstName\":\"Ranjan\",\"lastName\":\"Fonseka\",\"score\":90.6}]," +
                        "\"experience\":{\"duration\":10,\"qualitifications\":\"B.Sc.\"}," +
                        "\"firstName\":\"Alex\",\"lastName\":\"George\",\"deptAccess\":\"XYZ\"," +
                        "\"address\":{\"city\":\"NY\",\"country\":\"America\"}}");

        Assert.assertTrue(teacher2.get(StringUtils.fromString("classStudents")) instanceof BArray);
        Assert.assertTrue(teacher2.get(StringUtils.fromString("experience")) instanceof BMap);
        Assert.assertEquals(teacher2.get(StringUtils.fromString("classStudents")).toString(),
                "[{\"firstName\":\"Alex\",\"lastName\":\"George\",\"score\":82.5}," +
                        "{\"firstName\":\"Ranjan\",\"lastName\":\"Fonseka\",\"score\":90.6}]");
        Assert.assertEquals(teacher2.get(StringUtils.fromString("experience")).toString(),
                "{\"duration\":10,\"qualitifications\":\"B.Sc.\"}");
        Assert.assertEquals(teacher2.toString(),
                "{\"classStudents\":[{\"firstName\":\"Alex\",\"lastName\":\"George\",\"score\":82.5}," +
                        "{\"firstName\":\"Ranjan\",\"lastName\":\"Fonseka\",\"score\":90.6}]," +
                        "\"experience\":{\"duration\":10,\"qualitifications\":\"B.Sc.\"}," +
                        "\"firstName\":\"Ranjan\",\"lastName\":\"Fonseka\",\"deptAccess\":\"XYZ\"," +
                        "\"address\":{\"city\":\"NY\",\"country\":\"America\"}}");
    }

    @Test(description = "Test query expressions with tuple typed binding in let")
    public void testQueryExprTupleTypedBinding2() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprTupleTypedBinding2");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expression with type conversion in select clause")
    public void testQueryExprWithTypeConversion() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testQueryExprWithTypeConversion");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");

        BMap person1 = (BMap) returnValues.get(0);
        BMap person2 = (BMap) returnValues.get(1);

        Assert.assertEquals(person1.toString(), "{\"firstName\":\"Alex\",\"lastName\":\"George\"," +
                "\"deptAccess\":\"XYZ\",\"address\":{\"city\":\"New York\",\"country\":\"America\"}}");
        Assert.assertEquals(person2.toString(), "{\"firstName\":\"Ranjan\",\"lastName\":\"Fonseka\"," +
                "\"deptAccess\":\"XYZ\",\"address\":{\"city\":\"New York\",\"country\":\"America\"}}");
    }

    @Test(description = "Test streams with map and filter")
    public void testQueryExprWithStreamMapAndFilter() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testQueryExprWithStreamMapAndFilter");
        Assert.assertNotNull(returnValues);

        BMap subscription = (BMap) returnValues.get(0);

        Assert.assertEquals(subscription.toString(),
                "{\"firstName\":\"Ranjan\",\"lastName\":\"Fonseka\",\"score\":90.6,\"" +
                        "degree\":\"Bachelor of Medicine\"}");
    }

    @Test(description = "Test Query expression returning a stream ")
    public void testSimpleSelectQueryReturnStream() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test Query expression With Record Variable within let clause ")
    public void testQueryWithRecordVarInLetClause() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testQueryWithRecordVarInLetClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");
        BMap person1 = (BMap) returnValues.get(0);
        BMap person2 = (BMap) returnValues.get(1);

        Assert.assertEquals(person1.toString(), "{\"firstName\":\"Ranjan\",\"lastName\":\"Fonseka\"," +
                "\"deptAccess\":\"XYZ\",\"address\":{\"city\":\"Colombo\",\"country\":\"SL\"}}");
        Assert.assertEquals(person2.toString(), "{\"firstName\":\"John\",\"lastName\":\"David\"," +
                "\"deptAccess\":\"XYZ\",\"address\":{\"city\":\"Colombo\",\"country\":\"SL\"}}");
    }

    @Test(description = "Test looping a stream with foreach")
    public void testForeachStream() {
        Object returnValues = BRunUtil.invoke(result, "testForeachStream");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test type test in where clause")
    public void testTypeTestInWhereClause() {
        BRunUtil.invoke(result, "testTypeTestInWhereClause");
    }

    @Test
    public void testWildcardBindingPatternInQueryExpr1() {
        BRunUtil.invoke(result, "testWildcardBindingPatternInQueryExpr1");
    }

    @Test
    public void testWildcardBindingPatternInQueryExpr2() {
        BRunUtil.invoke(result, "testWildcardBindingPatternInQueryExpr2");
    }

    @Test
    public void testUsingAnIntersectionTypeInQueryExpr() {
        BRunUtil.invoke(result, "testUsingAnIntersectionTypeInQueryExpr");
    }

    @Test(dataProvider = "dataToTestQueryExprWithRegExp")
    public void testQueryExprWithRegExp(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestQueryExprWithRegExp() {
        return new Object[]{
                "testQueryExprWithRegExp",
                "testQueryExprWithRegExpWithInterpolations",
                "testNestedQueryExprWithRegExp",
                "testJoinedQueryExprWithRegExp"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
