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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test simple query action statements.
 *
 * @since 1.2.0
 */
public class QueryActionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query-action.bal");
    }

    @Test(description = "Test simple query action statement")
    public void testSimpleQueryAction() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryAction");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person3.get("lastName").stringValue(), "David");
    }

    @Test(description = "Test simple query action - record variable definition")
    public void testSimpleQueryActionWithRecordVariable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryActionWithRecordVariable");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person3.get("lastName").stringValue(), "David");
    }

    @Test(description = "Test simple query action - record variable definition")
    public void testSimpleSelectQueryWithRecordVariableV2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryWithRecordVariableV2");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person3.get("lastName").stringValue(), "David");
    }

    @Test(description = "Test simple query action statement v2")
    public void testSimpleQueryAction2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryAction2");
        Assert.assertNotNull(returnValues);

        BInteger countValue = (BInteger) returnValues[0];
        Assert.assertEquals(countValue.intValue(), 6);
    }

    @Test(description = "Test simple query action with let clause")
    public void testSimpleSelectQueryWithLetClause() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryWithLetClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        BMap<String, BValue> person = (BMap<String, BValue>) returnValues[0];

        Assert.assertEquals(person.get("firstName").stringValue(), "Alex");
    }

    @Test(description = "Test simple query action with where clause")
    public void testSimpleSelectQueryWithWhereClause() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryWithWhereClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        BMap<String, BValue> person = (BMap<String, BValue>) returnValues[0];

        Assert.assertEquals(person.get("firstName").stringValue(), "Alex");
    }

    @Test(description = "Test simple query action with multiple from clauses")
    public void testSimpleSelectQueryWithMultipleFromClauses() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleSelectQueryWithMultipleFromClauses");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");
        BMap<String, BValue> employee = (BMap<String, BValue>) returnValues[0];

        Assert.assertEquals(employee.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(employee.get("deptAccess").stringValue(), "Human Resource");
    }
}
