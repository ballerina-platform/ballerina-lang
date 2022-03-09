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
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMultipleSelectClausesWithSimpleVariable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 6, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);
        BMap<String, Object> person4 = (BMap<String, Object>) returnValues.get(3);
        BMap<String, Object> person5 = (BMap<String, Object>) returnValues.get(4);
        BMap<String, Object> person6 = (BMap<String, Object>) returnValues.get(5);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person1.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person2.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person2.get(StringUtils.fromString("deptAccess")).toString(), "Operations");

        Assert.assertEquals(person3.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person3.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person3.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person4.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person4.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person4.get(StringUtils.fromString("deptAccess")).toString(), "Operations");

        Assert.assertEquals(person5.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(person5.get(StringUtils.fromString("lastName")).toString(), "David");
        Assert.assertEquals(person5.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person6.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(person6.get(StringUtils.fromString("lastName")).toString(), "David");
        Assert.assertEquals(person6.get(StringUtils.fromString("deptAccess")).toString(), "Operations");
    }

    @Test(description = "Test multiple select clauses - record variable definition statement ")
    public void testMultipleSelectClausesWithRecordVariable() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMultipleSelectClausesWithRecordVariable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 6, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);
        BMap<String, Object> person4 = (BMap<String, Object>) returnValues.get(3);
        BMap<String, Object> person5 = (BMap<String, Object>) returnValues.get(4);
        BMap<String, Object> person6 = (BMap<String, Object>) returnValues.get(5);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person1.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person2.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person2.get(StringUtils.fromString("deptAccess")).toString(), "Operations");

        Assert.assertEquals(person3.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person3.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person3.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person4.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person4.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person4.get(StringUtils.fromString("deptAccess")).toString(), "Operations");

        Assert.assertEquals(person5.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(person5.get(StringUtils.fromString("lastName")).toString(), "David");
        Assert.assertEquals(person5.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person6.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(person6.get(StringUtils.fromString("lastName")).toString(), "David");
        Assert.assertEquals(person6.get(StringUtils.fromString("deptAccess")).toString(), "Operations");
    }

    @Test(description = "Test multiple select clauses - record variable definition statement v2")
    public void testMultipleSelectClausesWithRecordVariableV2() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMultipleSelectClausesWithRecordVariableV2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 6, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);
        BMap<String, Object> person4 = (BMap<String, Object>) returnValues.get(3);
        BMap<String, Object> person5 = (BMap<String, Object>) returnValues.get(4);
        BMap<String, Object> person6 = (BMap<String, Object>) returnValues.get(5);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person1.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person2.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person2.get(StringUtils.fromString("deptAccess")).toString(), "Operations");

        Assert.assertEquals(person3.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person3.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person3.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person4.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person4.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person4.get(StringUtils.fromString("deptAccess")).toString(), "Operations");

        Assert.assertEquals(person5.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(person5.get(StringUtils.fromString("lastName")).toString(), "David");
        Assert.assertEquals(person5.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person6.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(person6.get(StringUtils.fromString("lastName")).toString(), "David");
        Assert.assertEquals(person6.get(StringUtils.fromString("deptAccess")).toString(), "Operations");
    }

    @Test(description = "Test multiple from clauses with type stream")
    public void testMultipleFromClausesWithStream() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMultipleFromClausesWithStream");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 6, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);
        BMap<String, Object> person4 = (BMap<String, Object>) returnValues.get(3);
        BMap<String, Object> person5 = (BMap<String, Object>) returnValues.get(4);
        BMap<String, Object> person6 = (BMap<String, Object>) returnValues.get(5);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person1.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person2.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person2.get(StringUtils.fromString("deptAccess")).toString(), "Operations");

        Assert.assertEquals(person3.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person3.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person3.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person4.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person4.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person4.get(StringUtils.fromString("deptAccess")).toString(), "Operations");

        Assert.assertEquals(person5.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(person5.get(StringUtils.fromString("lastName")).toString(), "David");
        Assert.assertEquals(person5.get(StringUtils.fromString("deptAccess")).toString(), "HR");

        Assert.assertEquals(person6.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(person6.get(StringUtils.fromString("lastName")).toString(), "David");
        Assert.assertEquals(person6.get(StringUtils.fromString("deptAccess")).toString(), "Operations");
    }

    @Test(description = "Test multiple from clauses with let clause")
    public void testMultipleFromWithLetClauses() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMultipleFromWithLetClauses");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 6, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);
        BMap<String, Object> person4 = (BMap<String, Object>) returnValues.get(3);
        BMap<String, Object> person5 = (BMap<String, Object>) returnValues.get(4);
        BMap<String, Object> person6 = (BMap<String, Object>) returnValues.get(5);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person1.get(StringUtils.fromString("deptAccess")).toString(), "WSO2");

        Assert.assertEquals(person2.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person2.get(StringUtils.fromString("deptAccess")).toString(), "WSO2");

        Assert.assertEquals(person3.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person3.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person3.get(StringUtils.fromString("deptAccess")).toString(), "WSO2");

        Assert.assertEquals(person4.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person4.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person4.get(StringUtils.fromString("deptAccess")).toString(), "WSO2");

        Assert.assertEquals(person5.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(person5.get(StringUtils.fromString("lastName")).toString(), "David");
        Assert.assertEquals(person5.get(StringUtils.fromString("deptAccess")).toString(), "WSO2");

        Assert.assertEquals(person6.get(StringUtils.fromString("firstName")).toString(), "John");
        Assert.assertEquals(person6.get(StringUtils.fromString("lastName")).toString(), "David");
        Assert.assertEquals(person6.get(StringUtils.fromString("deptAccess")).toString(), "WSO2");
    }

    @Test(description = "Test more than two from clauses")
    public void testMultipleFromAndSelectClausesWithRecordVariable() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMultipleFromAndSelectClausesWithRecordVariable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 8, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);
        BMap<String, Object> person4 = (BMap<String, Object>) returnValues.get(3);
        BMap<String, Object> person5 = (BMap<String, Object>) returnValues.get(4);
        BMap<String, Object> person6 = (BMap<String, Object>) returnValues.get(5);
        BMap<String, Object> person7 = (BMap<String, Object>) returnValues.get(6);
        BMap<String, Object> person8 = (BMap<String, Object>) returnValues.get(7);

        BMap<String, Object> person1Address = (BMap<String, Object>) person1.get(StringUtils.fromString("address"));
        BMap<String, Object> person2Address = (BMap<String, Object>) person2.get(StringUtils.fromString("address"));
        BMap<String, Object> person3Address = (BMap<String, Object>) person3.get(StringUtils.fromString("address"));
        BMap<String, Object> person4Address = (BMap<String, Object>) person4.get(StringUtils.fromString("address"));
        BMap<String, Object> person5Address = (BMap<String, Object>) person5.get(StringUtils.fromString("address"));
        BMap<String, Object> person6Address = (BMap<String, Object>) person6.get(StringUtils.fromString("address"));
        BMap<String, Object> person7Address = (BMap<String, Object>) person7.get(StringUtils.fromString("address"));
        BMap<String, Object> person8Address = (BMap<String, Object>) person8.get(StringUtils.fromString("address"));

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person1.get(StringUtils.fromString("deptAccess")).toString(), "HR");
        Assert.assertEquals(person1Address.get(StringUtils.fromString("city")).toString(), "New York");
        Assert.assertEquals(person1Address.get(StringUtils.fromString("country")).toString(), "USA");

        Assert.assertEquals(person2.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person2.get(StringUtils.fromString("deptAccess")).toString(), "HR");
        Assert.assertEquals(person2Address.get(StringUtils.fromString("city")).toString(), "Springfield");
        Assert.assertEquals(person2Address.get(StringUtils.fromString("country")).toString(), "USA");

        Assert.assertEquals(person3.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person3.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person3.get(StringUtils.fromString("deptAccess")).toString(), "Operations");
        Assert.assertEquals(person3Address.get(StringUtils.fromString("city")).toString(), "New York");
        Assert.assertEquals(person3Address.get(StringUtils.fromString("country")).toString(), "USA");

        Assert.assertEquals(person4.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person4.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person4.get(StringUtils.fromString("deptAccess")).toString(), "Operations");
        Assert.assertEquals(person4Address.get(StringUtils.fromString("city")).toString(), "Springfield");
        Assert.assertEquals(person4Address.get(StringUtils.fromString("country")).toString(), "USA");

        Assert.assertEquals(person5.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person5.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person5.get(StringUtils.fromString("deptAccess")).toString(), "HR");
        Assert.assertEquals(person5Address.get(StringUtils.fromString("city")).toString(), "New York");
        Assert.assertEquals(person5Address.get(StringUtils.fromString("country")).toString(), "USA");

        Assert.assertEquals(person6.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person6.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person6.get(StringUtils.fromString("deptAccess")).toString(), "HR");
        Assert.assertEquals(person6Address.get(StringUtils.fromString("city")).toString(), "Springfield");
        Assert.assertEquals(person6Address.get(StringUtils.fromString("country")).toString(), "USA");

        Assert.assertEquals(person7.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person7.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person7.get(StringUtils.fromString("deptAccess")).toString(), "Operations");
        Assert.assertEquals(person7Address.get(StringUtils.fromString("city")).toString(), "New York");
        Assert.assertEquals(person7Address.get(StringUtils.fromString("country")).toString(), "USA");

        Assert.assertEquals(person8.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(person8.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person8.get(StringUtils.fromString("deptAccess")).toString(), "Operations");
        Assert.assertEquals(person8Address.get(StringUtils.fromString("city")).toString(), "Springfield");
        Assert.assertEquals(person8Address.get(StringUtils.fromString("country")).toString(), "USA");
    }

    @Test(description = "Test query expressions with tuple typed binding")
    public void testQueryExprTupleTypedBinding1() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprTupleTypedBinding1");
        Assert.assertNotNull(returnValues);
        Assert.assertTrue((Boolean) returnValues);
    }

    @Test(description = "Test query expressions with open records")
    public void testQueryExprWithOpenRecords() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testQueryExprWithOpenRecords");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 4, "Expected events are not received");

        BMap section1 = (BMap) returnValues.get(0);
        BMap section2 = (BMap) returnValues.get(1);
        BMap section3 = (BMap) returnValues.get(2);
        BMap section4 = (BMap) returnValues.get(3);

        Assert.assertTrue(section1.get(StringUtils.fromString("grades")) instanceof BMap);
        Assert.assertEquals(section1.get(StringUtils.fromString("grades")).toString(),
                "{\"physics\":30,\"chemistry\":50,\"maths\":60}");
        Assert.assertEquals(section1.toString(), "{\"name\":\"Maths\",\"grades\":{\"physics\":30,\"chemistry\":50," +
                "\"maths\":60},\"noOfStudents\":100}");

        Assert.assertTrue(section2.get(StringUtils.fromString("grades")) instanceof BMap);
        Assert.assertEquals(section2.get(StringUtils.fromString("grades")).toString(),
                "{\"physics\":50,\"chemistry\":60,\"bio\":70}");
        Assert.assertEquals(section2.toString(),
                "{\"name\":\"Maths\",\"grades\":{\"physics\":50,\"chemistry\":60,\"bio\":70}," +
                        "\"noOfStudents\":100}");

        Assert.assertTrue(section3.get(StringUtils.fromString("grades")) instanceof BMap);
        Assert.assertEquals(section3.get(StringUtils.fromString("grades")).toString(),
                "{\"physics\":30,\"chemistry\":50,\"maths\":60}");
        Assert.assertEquals(section3.toString(),
                "{\"name\":\"Bio\",\"grades\":{\"physics\":30,\"chemistry\":50,\"maths\":60}," +
                        "\"noOfStudents\":100}");

        Assert.assertTrue(section4.get(StringUtils.fromString("grades")) instanceof BMap);
        Assert.assertEquals(section4.get(StringUtils.fromString("grades")).toString(),
                "{\"physics\":50,\"chemistry\":60,\"bio\":70}");
        Assert.assertEquals(section4.toString(),
                "{\"name\":\"Bio\",\"grades\":{\"physics\":50,\"chemistry\":60,\"bio\":70}," +
                        "\"noOfStudents\":100}");
    }

    @Test(description = "Test query expression with record typed binding")
    public void testQueryExprRecordTypedBinding() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testQueryExprRecordTypedBinding");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 4, "Expected events are not received");

        BMap person1 = (BMap) returnValues.get(0);
        BMap person2 = (BMap) returnValues.get(1);
        BMap person3 = (BMap) returnValues.get(2);
        BMap person4 = (BMap) returnValues.get(3);

        Assert.assertTrue(person1.get(StringUtils.fromString("address")) instanceof BMap);
        Assert.assertEquals(person1.toString(), "{\"firstName\":\"Alex\",\"lastName\":\"George\"," +
                "\"deptAccess\":\"HR\",\"address\":{\"city\":\"NY\",\"country\":\"America\"}}");
        Assert.assertEquals(person2.toString(), "{\"firstName\":\"Alex\",\"lastName\":\"George\"," +
                "\"deptAccess\":\"Operations\",\"address\":{\"city\":\"NY\",\"country\":\"America\"}}");
        Assert.assertEquals(person3.toString(), "{\"firstName\":\"Ranjan\",\"lastName\":\"Fonseka\"," +
                "\"deptAccess\":\"HR\",\"address\":{\"city\":\"NY\",\"country\":\"America\"}}");
        Assert.assertEquals(person4.toString(), "{\"firstName\":\"Ranjan\",\"lastName\":\"Fonseka\"," +
                "\"deptAccess\":\"Operations\",\"address\":{\"city\":\"NY\",\"country\":\"America\"}}");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
