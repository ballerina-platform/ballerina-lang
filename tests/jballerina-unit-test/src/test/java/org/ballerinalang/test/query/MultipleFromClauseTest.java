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
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test simple query expression with from and select clauses.
 *
 * @since 1.2.0
 */
public class MultipleFromClauseTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/multiple-from-clauses.bal");
    }

    @Test(description = "Test multiple select clauses - simple variable definition statement ")
    public void testMultipleSelectClausesWithSimpleVariable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleSelectClausesWithSimpleVariable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 6, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> person4 = (BMap<String, BValue>) returnValues[3];
        BMap<String, BValue> person5 = (BMap<String, BValue>) returnValues[4];
        BMap<String, BValue> person6 = (BMap<String, BValue>) returnValues[5];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person1.get("lastName").stringValue(), "George");
        Assert.assertEquals(person1.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person2.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "George");
        Assert.assertEquals(person2.get("deptAccess").stringValue(), "Operations");

        Assert.assertEquals(person3.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person3.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person3.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person4.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person4.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person4.get("deptAccess").stringValue(), "Operations");

        Assert.assertEquals(person5.get("firstName").stringValue(), "John");
        Assert.assertEquals(person5.get("lastName").stringValue(), "David");
        Assert.assertEquals(person5.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person6.get("firstName").stringValue(), "John");
        Assert.assertEquals(person6.get("lastName").stringValue(), "David");
        Assert.assertEquals(person6.get("deptAccess").stringValue(), "Operations");
    }

    @Test(description = "Test multiple select clauses - record variable definition statement ")
    public void testMultipleSelectClausesWithRecordVariable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleSelectClausesWithRecordVariable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 6, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> person4 = (BMap<String, BValue>) returnValues[3];
        BMap<String, BValue> person5 = (BMap<String, BValue>) returnValues[4];
        BMap<String, BValue> person6 = (BMap<String, BValue>) returnValues[5];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person1.get("lastName").stringValue(), "George");
        Assert.assertEquals(person1.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person2.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "George");
        Assert.assertEquals(person2.get("deptAccess").stringValue(), "Operations");

        Assert.assertEquals(person3.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person3.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person3.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person4.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person4.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person4.get("deptAccess").stringValue(), "Operations");

        Assert.assertEquals(person5.get("firstName").stringValue(), "John");
        Assert.assertEquals(person5.get("lastName").stringValue(), "David");
        Assert.assertEquals(person5.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person6.get("firstName").stringValue(), "John");
        Assert.assertEquals(person6.get("lastName").stringValue(), "David");
        Assert.assertEquals(person6.get("deptAccess").stringValue(), "Operations");
    }

    @Test(description = "Test multiple select clauses - record variable definition statement v2")
    public void testMultipleSelectClausesWithRecordVariableV2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleSelectClausesWithRecordVariableV2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 6, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> person4 = (BMap<String, BValue>) returnValues[3];
        BMap<String, BValue> person5 = (BMap<String, BValue>) returnValues[4];
        BMap<String, BValue> person6 = (BMap<String, BValue>) returnValues[5];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person1.get("lastName").stringValue(), "George");
        Assert.assertEquals(person1.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person2.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "George");
        Assert.assertEquals(person2.get("deptAccess").stringValue(), "Operations");

        Assert.assertEquals(person3.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person3.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person3.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person4.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person4.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person4.get("deptAccess").stringValue(), "Operations");

        Assert.assertEquals(person5.get("firstName").stringValue(), "John");
        Assert.assertEquals(person5.get("lastName").stringValue(), "David");
        Assert.assertEquals(person5.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person6.get("firstName").stringValue(), "John");
        Assert.assertEquals(person6.get("lastName").stringValue(), "David");
        Assert.assertEquals(person6.get("deptAccess").stringValue(), "Operations");
    }

    @Test(description = "Test multiple from clauses with type stream")
    public void testMultipleFromClausesWithStream() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleFromClausesWithStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 6, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> person4 = (BMap<String, BValue>) returnValues[3];
        BMap<String, BValue> person5 = (BMap<String, BValue>) returnValues[4];
        BMap<String, BValue> person6 = (BMap<String, BValue>) returnValues[5];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person1.get("lastName").stringValue(), "George");
        Assert.assertEquals(person1.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person2.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "George");
        Assert.assertEquals(person2.get("deptAccess").stringValue(), "Operations");

        Assert.assertEquals(person3.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person3.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person3.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person4.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person4.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person4.get("deptAccess").stringValue(), "Operations");

        Assert.assertEquals(person5.get("firstName").stringValue(), "John");
        Assert.assertEquals(person5.get("lastName").stringValue(), "David");
        Assert.assertEquals(person5.get("deptAccess").stringValue(), "HR");

        Assert.assertEquals(person6.get("firstName").stringValue(), "John");
        Assert.assertEquals(person6.get("lastName").stringValue(), "David");
        Assert.assertEquals(person6.get("deptAccess").stringValue(), "Operations");
    }

    @Test(description = "Test multiple from clauses with let clause")
    public void testMultipleFromWithLetClauses() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleFromWithLetClauses");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 6, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> person4 = (BMap<String, BValue>) returnValues[3];
        BMap<String, BValue> person5 = (BMap<String, BValue>) returnValues[4];
        BMap<String, BValue> person6 = (BMap<String, BValue>) returnValues[5];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person1.get("lastName").stringValue(), "George");
        Assert.assertEquals(person1.get("deptAccess").stringValue(), "WSO2");

        Assert.assertEquals(person2.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "George");
        Assert.assertEquals(person2.get("deptAccess").stringValue(), "WSO2");

        Assert.assertEquals(person3.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person3.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person3.get("deptAccess").stringValue(), "WSO2");

        Assert.assertEquals(person4.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person4.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person4.get("deptAccess").stringValue(), "WSO2");

        Assert.assertEquals(person5.get("firstName").stringValue(), "John");
        Assert.assertEquals(person5.get("lastName").stringValue(), "David");
        Assert.assertEquals(person5.get("deptAccess").stringValue(), "WSO2");

        Assert.assertEquals(person6.get("firstName").stringValue(), "John");
        Assert.assertEquals(person6.get("lastName").stringValue(), "David");
        Assert.assertEquals(person6.get("deptAccess").stringValue(), "WSO2");
    }
    @Test(description = "Test more than two from clauses")
    public void testMultipleFromAndSelectClausesWithRecordVariable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleFromAndSelectClausesWithRecordVariable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 8, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];
        BMap<String, BValue> person4 = (BMap<String, BValue>) returnValues[3];
        BMap<String, BValue> person5 = (BMap<String, BValue>) returnValues[4];
        BMap<String, BValue> person6 = (BMap<String, BValue>) returnValues[5];
        BMap<String, BValue> person7 = (BMap<String, BValue>) returnValues[6];
        BMap<String, BValue> person8 = (BMap<String, BValue>) returnValues[7];

        BMap<String, BValue> person1Address = (BMap<String, BValue>) person1.get("address");
        BMap<String, BValue> person2Address = (BMap<String, BValue>) person2.get("address");
        BMap<String, BValue> person3Address = (BMap<String, BValue>) person3.get("address");
        BMap<String, BValue> person4Address = (BMap<String, BValue>) person4.get("address");
        BMap<String, BValue> person5Address = (BMap<String, BValue>) person5.get("address");
        BMap<String, BValue> person6Address = (BMap<String, BValue>) person6.get("address");
        BMap<String, BValue> person7Address = (BMap<String, BValue>) person7.get("address");
        BMap<String, BValue> person8Address = (BMap<String, BValue>) person8.get("address");

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person1.get("lastName").stringValue(), "George");
        Assert.assertEquals(person1.get("deptAccess").stringValue(), "HR");
        Assert.assertEquals(person1Address.get("city").stringValue(), "New York");
        Assert.assertEquals(person1Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person2.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "George");
        Assert.assertEquals(person2.get("deptAccess").stringValue(), "HR");
        Assert.assertEquals(person2Address.get("city").stringValue(), "Springfield");
        Assert.assertEquals(person2Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person3.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person3.get("lastName").stringValue(), "George");
        Assert.assertEquals(person3.get("deptAccess").stringValue(), "Operations");
        Assert.assertEquals(person3Address.get("city").stringValue(), "New York");
        Assert.assertEquals(person3Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person4.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person4.get("lastName").stringValue(), "George");
        Assert.assertEquals(person4.get("deptAccess").stringValue(), "Operations");
        Assert.assertEquals(person4Address.get("city").stringValue(), "Springfield");
        Assert.assertEquals(person4Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person5.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person5.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person5.get("deptAccess").stringValue(), "HR");
        Assert.assertEquals(person5Address.get("city").stringValue(), "New York");
        Assert.assertEquals(person5Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person6.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person6.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person6.get("deptAccess").stringValue(), "HR");
        Assert.assertEquals(person6Address.get("city").stringValue(), "Springfield");
        Assert.assertEquals(person6Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person7.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person7.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person7.get("deptAccess").stringValue(), "Operations");
        Assert.assertEquals(person7Address.get("city").stringValue(), "New York");
        Assert.assertEquals(person7Address.get("country").stringValue(), "USA");

        Assert.assertEquals(person8.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(person8.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person8.get("deptAccess").stringValue(), "Operations");
        Assert.assertEquals(person8Address.get("city").stringValue(), "Springfield");
        Assert.assertEquals(person8Address.get("country").stringValue(), "USA");
    }

    @Test(description = "Test query expressions with tuple typed binding")
    public void testQueryExprTupleTypedBinding1() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprTupleTypedBinding1");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test query expressions with open records")
    public void testQueryExprWithOpenRecords() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithOpenRecords");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 4, "Expected events are not received");

        BMap section1 = (BMap) returnValues[0];
        BMap section2 = (BMap) returnValues[1];
        BMap section3 = (BMap) returnValues[2];
        BMap section4 = (BMap) returnValues[3];

        Assert.assertTrue(section1.get("grades") instanceof BMap);
        Assert.assertEquals(section1.get("grades").stringValue(), "{physics:30, chemistry:50, maths:60}");
        Assert.assertEquals(section1.stringValue(), "{name:\"Maths\", grades:{physics:30, chemistry:50, maths:60}," +
                " noOfStudents:100}");

        Assert.assertTrue(section2.get("grades") instanceof BMap);
        Assert.assertEquals(section2.get("grades").stringValue(), "{physics:50, chemistry:60, bio:70}");
        Assert.assertEquals(section2.stringValue(), "{name:\"Maths\", grades:{physics:50, chemistry:60, bio:70}, " +
                "noOfStudents:100}");

        Assert.assertTrue(section3.get("grades") instanceof BMap);
        Assert.assertEquals(section3.get("grades").stringValue(), "{physics:30, chemistry:50, maths:60}");
        Assert.assertEquals(section3.stringValue(), "{name:\"Bio\", grades:{physics:30, chemistry:50, maths:60}, " +
                "noOfStudents:100}");

        Assert.assertTrue(section4.get("grades") instanceof BMap);
        Assert.assertEquals(section4.get("grades").stringValue(), "{physics:50, chemistry:60, bio:70}");
        Assert.assertEquals(section4.stringValue(), "{name:\"Bio\", grades:{physics:50, chemistry:60, bio:70}, " +
                "noOfStudents:100}");
    }

    @Test(description = "Test query expression with record typed binding")
    public void testQueryExprRecordTypedBinding() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprRecordTypedBinding");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 4, "Expected events are not received");

        BMap person1 = (BMap) returnValues[0];
        BMap person2 = (BMap) returnValues[1];
        BMap person3 = (BMap) returnValues[2];
        BMap person4 = (BMap) returnValues[3];

        Assert.assertTrue(person1.get("address") instanceof BMap);
        Assert.assertEquals(person1.stringValue(), "{firstName:\"Alex\", lastName:\"George\", " +
                "deptAccess:\"HR\", address:{city:\"NY\", country:\"America\"}}");
        Assert.assertEquals(person2.stringValue(), "{firstName:\"Alex\", lastName:\"George\", " +
                "deptAccess:\"Operations\", address:{city:\"NY\", country:\"America\"}}");
        Assert.assertEquals(person3.stringValue(), "{firstName:\"Ranjan\", lastName:\"Fonseka\", " +
                "deptAccess:\"HR\", address:{city:\"NY\", country:\"America\"}}");
        Assert.assertEquals(person4.stringValue(), "{firstName:\"Ranjan\", lastName:\"Fonseka\", " +
                "deptAccess:\"Operations\", address:{city:\"NY\", country:\"America\"}}");
    }
}
