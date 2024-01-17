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
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMultipleLetClausesWithSimpleVariable1");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alexander");
        Assert.assertEquals(person1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person1.get(StringUtils.fromString("deptAccess")).toString(), "WSO2");
    }

    @Test(description = "Test multiple let clauses in multiple lines - simple variable definition statement ")
    public void testMultipleLetClausesWithSimpleVariable() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMultipleLetClausesWithSimpleVariable2");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alexander");
        Assert.assertEquals(person1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person1.get(StringUtils.fromString("deptAccess")).toString(), "WSO2");
    }

    @Test(description = "Test multiple let clauses - record variable definition statement")
    public void testMultipleLetClausesWithRecordVariable() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMultipleLetClausesWithRecordVariable");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        Assert.assertEquals(person1.size(), 3, "Expected events are not received");
        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(person1.get(StringUtils.fromString("deptAccess")).toString(), "WSO2");
    }

    @Test(description = "Reuse variables in let clause")
    public void testMultipleVarDeclReuseLetClause() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testMultipleVarDeclReuseLetClause");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");

        BMap<String, Object> teacher1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> teacher2 = (BMap<String, Object>) returnValues.get(1);

        Assert.assertEquals(teacher1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(teacher1.get(StringUtils.fromString("lastName")).toString(), "George");
        Assert.assertEquals(teacher1.get(StringUtils.fromString("age")).toString(), "30");

        Assert.assertEquals(teacher2.get(StringUtils.fromString("firstName")).toString(), "Ranjan");
        Assert.assertEquals(teacher2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(teacher2.get(StringUtils.fromString("age")).toString(), "30");
    }

    @DataProvider(name = "functionNameProvider")
    public Object[] getFuncNames() {
        return new Object[] {
                "testQueryExpressionWithinLetClause",
                "testwildcardBindingPatternInLetClause",
                "testQueryInLetClauseAsAClosure1",
                "testQueryInLetClauseAsAClosure2",
                "testQueryInLetClauseAsAClosure3"
        };
    }

    @Test(dataProvider = "functionNameProvider")
    public void testLetClauseInQueryExpressions(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
