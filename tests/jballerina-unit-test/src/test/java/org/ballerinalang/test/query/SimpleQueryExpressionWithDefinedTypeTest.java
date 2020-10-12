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
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryWithSimpleVariable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(((BInteger) person3.get("age")).intValue(), 33);
    }

    @Test(description = "Test simple select clause - record variable definition statement ")
    public void testSimpleSelectQueryWithRecordVariable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryWithRecordVariable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(((BInteger) person3.get("age")).intValue(), 33);
    }

    @Test(description = "Test simple select clause - record variable definition statement v2 ")
    public void testSimpleSelectQueryWithRecordVariableV2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryWithRecordVariableV2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(((BInteger) person3.get("age")).intValue(), 33);
    }

    @Test(description = "Test simple select clause - record variable definition statement v3 ")
    public void testSimpleSelectQueryWithRecordVariableV3() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryWithRecordVariableV3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
    }

    @Test(description = "Test simple select clause with a where clause")
    public void testSimpleSelectQueryWithWhereClause() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryWithWhereClause");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person2.get("firstName").stringValue(), "John");
        Assert.assertEquals(((BInteger) person1.get("age")).intValue(), 30);
        Assert.assertEquals(((BInteger) person2.get("age")).intValue(), 33);
    }

    @Test(description = "Test Query expression for primitive types ")
    public void testQueryExpressionForPrimitiveType() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExpressionForPrimitiveType");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test Query expression with select expression ")
    public void testQueryExpressionWithSelectExpression() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExpressionWithSelectExpression");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test filtering null values from query expression")
    public void testFilteringNullElements() {
        BValue[] returnValues = BRunUtil.invoke(result, "testFilteringNullElements");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(((BInteger) person1.get("age")).intValue(), 23);
        Assert.assertEquals(person2.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(((BInteger) person2.get("age")).intValue(), 30);
    }

    @Test(description = "Test filtering map with from query expression")
    public void testMapWithArity() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMapWithArity");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test selecting values in a JSON array from query expression")
    public void testJSONArrayWithArity() {
        BValue[] returnValues = BRunUtil.invoke(result, "testJSONArrayWithArity");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test filtering values in a tuple from query expression")
    public void testArrayWithTuple() {
        BValue[] returnValues = BRunUtil.invoke(result, "testArrayWithTuple");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test from clause with a stream")
    public void testFromClauseWithStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testFromClauseWithStream");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BMap<String, BValue> person = (BMap<String, BValue>) returnValues[0];

        Assert.assertEquals(person.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(((BInteger) person.get("age")).intValue(), 40);
    }

    @Test(description = "Test let clause with a stream")
    public void testSimpleSelectQueryWithLetClause() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryWithLetClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap<String, BValue> employee1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) returnValues[1];

        Assert.assertEquals(employee1.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(employee1.get("department").stringValue(), "HR");
        Assert.assertEquals(employee1.get("company").stringValue(), "WSO2");

        Assert.assertEquals(employee2.get("firstName").stringValue(), "John");
        Assert.assertEquals(employee2.get("department").stringValue(), "HR");
        Assert.assertEquals(employee2.get("company").stringValue(), "WSO2");
    }

    @Test(description = "Use function return value in let clause")
    public void testFunctionCallInVarDeclLetClause() {
        BValue[] returnValues = BRunUtil.invoke(result, "testFunctionCallInVarDeclLetClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap<String, BValue> employee1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) returnValues[1];

        Assert.assertEquals(employee1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(employee1.get("lastName").stringValue(), "George");
        Assert.assertEquals(employee1.get("age").stringValue(), "46");

        Assert.assertEquals(employee2.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(employee2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(employee2.get("age").stringValue(), "60");
    }

    @Test(description = "Use value set in let with where clause")
    public void testUseOfLetInWhereClause() {
        BValue[] returnValues = BRunUtil.invoke(result, "testUseOfLetInWhereClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BMap<String, BValue> employee = (BMap<String, BValue>) returnValues[0];

        Assert.assertEquals(employee.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(employee.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(employee.get("age").stringValue(), "44");
    }

    @Test(description = "Use a stream with query expression")
    public void testQueryWithStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryWithStream");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Query a stream with error")
    public void testQueryStreamWithError() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryStreamWithError");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(returnValues[0] instanceof BError, "Expected BErrorType type value");
    }

    @Test(description = "Test anonymous record type, record type referencing, optional field, " +
            "changed order of the fields")
    public void testOthersAssociatedWithRecordTypes() {
        BValue[] returnValues = BRunUtil.invoke(result, "testOthersAssociatedWithRecordTypes");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap teacher1 = (BMap) returnValues[0];
        BMap teacher2 = (BMap) returnValues[1];

        Assert.assertTrue(teacher1.get("classStudents") instanceof BValueArray);
        Assert.assertTrue(teacher1.get("experience") instanceof BMap);
        Assert.assertEquals(teacher1.get("classStudents").stringValue(),
                "[{firstName:\"Alex\", lastName:\"George\", score:82.5}, " +
                        "{firstName:\"Ranjan\", lastName:\"Fonseka\", score:90.6}]");
        Assert.assertEquals(teacher1.get("experience").stringValue(), "{duration:10, qualitifications:\"B.Sc.\"}");
        Assert.assertEquals(teacher1.stringValue(),
                "{classStudents:[{firstName:\"Alex\", lastName:\"George\", score:82.5}, " +
                        "{firstName:\"Ranjan\", lastName:\"Fonseka\", score:90.6}], " +
                        "experience:{duration:10, qualitifications:\"B.Sc.\"}, " +
                        "firstName:\"Alex\", lastName:\"George\", deptAccess:\"XYZ\", " +
                        "address:{city:\"NY\", country:\"America\"}}");

        Assert.assertTrue(teacher2.get("classStudents") instanceof BValueArray);
        Assert.assertTrue(teacher2.get("experience") instanceof BMap);
        Assert.assertEquals(teacher2.get("classStudents").stringValue(),
                "[{firstName:\"Alex\", lastName:\"George\", score:82.5}, " +
                        "{firstName:\"Ranjan\", lastName:\"Fonseka\", score:90.6}]");
        Assert.assertEquals(teacher2.get("experience").stringValue(),
                "{duration:10, qualitifications:\"B.Sc.\"}");
        Assert.assertEquals(teacher2.stringValue(),
                "{classStudents:[{firstName:\"Alex\", lastName:\"George\", score:82.5}, " +
                        "{firstName:\"Ranjan\", lastName:\"Fonseka\", score:90.6}], " +
                        "experience:{duration:10, qualitifications:\"B.Sc.\"}, " +
                        "firstName:\"Ranjan\", lastName:\"Fonseka\", deptAccess:\"XYZ\", " +
                        "address:{city:\"NY\", country:\"America\"}}");
    }

    @Test(description = "Test query expressions with tuple typed binding in let")
    public void testQueryExprTupleTypedBinding2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprTupleTypedBinding2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expression with type conversion in select clause")
    public void testQueryExprWithTypeConversion() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithTypeConversion");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap person1 = (BMap) returnValues[0];
        BMap person2 = (BMap) returnValues[1];

        Assert.assertEquals(person1.stringValue(), "{firstName:\"Alex\", lastName:\"George\", " +
                "deptAccess:\"XYZ\", " +
                "address:{city:\"New York\", country:\"America\"}}");
        Assert.assertEquals(person2.stringValue(), "{firstName:\"Ranjan\", lastName:\"Fonseka\", " +
                "deptAccess:\"XYZ\", " +
                "address:{city:\"New York\", country:\"America\"}}");
    }

    @Test(description = "Test streams with map and filter")
    public void testQueryExprWithStreamMapAndFilter() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithStreamMapAndFilter");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BMap subscription = (BMap) returnValues[0];

        Assert.assertEquals(subscription.stringValue(), "{firstName:\"Ranjan\", lastName:\"Fonseka\", score:90.6, " +
                "degree:\"Bachelor of Medicine\"}");
    }

    @Test(description = "Test Query expression returning a stream ")
    public void testSimpleSelectQueryReturnStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryReturnStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test Query expression With Record Variable within let clause ")
    public void testQueryWithRecordVarInLetClause() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryWithRecordVarInLetClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap person1 = (BMap) returnValues[0];
        BMap person2 = (BMap) returnValues[1];

        Assert.assertEquals(person1.stringValue(), "{firstName:\"Ranjan\", lastName:\"Fonseka\", " +
                "deptAccess:\"XYZ\", address:{city:\"Colombo\", country:\"SL\"}}");
        Assert.assertEquals(person2.stringValue(), "{firstName:\"John\", lastName:\"David\", " +
                "deptAccess:\"XYZ\", address:{city:\"Colombo\", country:\"SL\"}}");
    }
}
