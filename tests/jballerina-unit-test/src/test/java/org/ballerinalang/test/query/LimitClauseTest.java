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

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This contains methods to test limit clause in query expression and query action.
 *
 * @since 1.3.0
 */
public class LimitClauseTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/limit-clause.bal");
    }

    @Test(description = "Test limit clause when limit < number of elements")
    public void testLimitClauseWithQueryExpr() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testLimitClauseWithQueryExpr");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals((person2.get(StringUtils.fromString("age"))), 34L);
    }

    @Test(description = "Test limit clause when limit > number of elements")
    public void testLimitClauseWithQueryExpr2() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testLimitClauseWithQueryExpr2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 3, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals((person3.get(StringUtils.fromString("age"))), 35L);
    }

    @Test(description = "Test limit clause when limit = number of elements")
    public void testLimitClauseWithQueryExpr3() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testLimitClauseWithQueryExpr3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.size(), 3, "Expected events are not received");

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals((person3.get(StringUtils.fromString("age"))), 35L);
    }

    @Test
    public void testLimitClauseReturnStream() {
        Object values = BRunUtil.invoke(result, "testLimitClauseReturnStream", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test query action with limit clause return simple value")
    public void testLimitClauseWithQueryAction() {
        Object returnValues = BRunUtil.invoke(result, "testLimitClauseWithQueryAction");
        Assert.assertNotNull(returnValues);

        long i = (long) returnValues;

        Assert.assertEquals(i, 6L);
    }

    @Test(description = "Test query action with limit clause return list")
    public void testLimitClauseWithQueryAction2() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testLimitClauseWithQueryAction2");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 1, "Expected events are not received");

        BMap<String, Object> fullName = (BMap<String, Object>) returnValues.get(0);

        Assert.assertEquals(fullName.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(fullName.get(StringUtils.fromString("lastName")).toString(), "George");
    }

    @Test(description = "Test limit clause when limit > number of elements")
    public void testLimitClauseWithQueryAction3() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testLimitClauseWithQueryAction3");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");

        BMap<String, Object> fullName1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> fullName2 = (BMap<String, Object>) returnValues.get(1);

        Assert.assertEquals(fullName1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(fullName2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
    }

    @Test(description = "Test limit clause when limit = number of elements")
    public void testLimitClauseWithQueryAction4() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testLimitClauseWithQueryAction4");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.size(), 2, "Expected events are not received");

        BMap<String, Object> fullName1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> fullName2 = (BMap<String, Object>) returnValues.get(1);

        Assert.assertEquals(fullName1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(fullName2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
    }

    @Test(description = "Test limit clause a let expression")
    public void testLetExpressionWithLimitClause() {
        Object values = BRunUtil.invoke(result, "testLetExpressionWithLimitClause");
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test limit clause with incompatible types")
    public void testNegativeScenarios() {
        negativeResult = BCompileUtil.compile("test-src/query/limit-clause-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        int i = 0;
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'boolean'", 38, 19);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'boolean'", 78, 19);
        validateError(negativeResult, i, "more clauses after select clause", 124, 13);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
