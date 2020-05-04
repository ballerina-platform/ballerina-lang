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
 * This contains methods to test query expression with multiple let clauses.
 *
 * @since 1.2.0
 */
public class MultipleLetClauseTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/multiple-let-clauses.bal");
    }

    @Test(description = "Test multiple let clauses in single line - simple variable definition statement ")
    public void testLetClauseWithSimpleVariable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleLetClausesWithSimpleVariable1");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alexander");
        Assert.assertEquals(person1.get("lastName").stringValue(), "George");
        Assert.assertEquals(person1.get("deptAccess").stringValue(), "WSO2");
    }

    @Test(description = "Test multiple let clauses in multiple lines - simple variable definition statement ")
    public void testMultipleLetClausesWithSimpleVariable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleLetClausesWithSimpleVariable2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alexander");
        Assert.assertEquals(person1.get("lastName").stringValue(), "George");
        Assert.assertEquals(person1.get("deptAccess").stringValue(), "WSO2");
    }

    @Test(description = "Test multiple let clauses - record variable definition statement")
    public void testMultipleLetClausesWithRecordVariable() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleLetClausesWithRecordVariable");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person1.get("lastName").stringValue(), "George");
        Assert.assertEquals(person1.get("deptAccess").stringValue(), "WSO2");
    }


    @Test(description = "Reuse variables in let clause")
    public void testMultipleVarDeclReuseLetClause() {
        BValue[] returnValues = BRunUtil.invoke(result, "testMultipleVarDeclReuseLetClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap<String, BValue> teacher1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> teacher2 = (BMap<String, BValue>) returnValues[1];

        Assert.assertEquals(teacher1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(teacher1.get("lastName").stringValue(), "George");
        Assert.assertEquals(teacher1.get("age").stringValue(), "30");

        Assert.assertEquals(teacher2.get("firstName").stringValue(), "Ranjan");
        Assert.assertEquals(teacher2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(teacher2.get("age").stringValue(), "30");
    }
}
