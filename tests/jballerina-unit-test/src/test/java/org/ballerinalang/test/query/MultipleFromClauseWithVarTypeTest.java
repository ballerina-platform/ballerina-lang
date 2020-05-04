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
public class MultipleFromClauseWithVarTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/multiple-from-clauses-with-var.bal");
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
}
