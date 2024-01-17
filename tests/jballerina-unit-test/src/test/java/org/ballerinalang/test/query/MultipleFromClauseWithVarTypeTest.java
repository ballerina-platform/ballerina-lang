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
public class MultipleFromClauseWithVarTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/multiple-from-clauses-with-var.bal");
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

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
