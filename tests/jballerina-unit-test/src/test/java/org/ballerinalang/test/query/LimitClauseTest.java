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

import org.ballerinalang.jvm.util.exceptions.BLangRuntimeException;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

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
        negativeResult = BCompileUtil.compile("test-src/query/limit-clause-negative.bal");
    }

    @Test(description = "Test limit clause when limit < number of elements")
    public void testLimitClauseWithQueryExpr() {
        BValue[] returnValues = BRunUtil.invoke(result, "testLimitClauseWithQueryExpr");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(((BInteger) person2.get("age")).intValue(), 34);
    }

    @Test(description = "Test limit clause when limit > number of elements")
    public void testLimitClauseWithQueryExpr2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testLimitClauseWithQueryExpr2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(((BInteger) person3.get("age")).intValue(), 35);
    }


    @Test(description = "Test limit clause when limit = number of elements")
    public void testLimitClauseWithQueryExpr3() {
        BValue[] returnValues = BRunUtil.invoke(result, "testLimitClauseWithQueryExpr3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(((BInteger) person3.get("age")).intValue(), 35);
    }

    @Test
    public void testLimitClauseReturnStream() {
        BValue[] values = BRunUtil.invoke(result, "testLimitClauseReturnStream", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test(description = "Test query action with limit clause return simple value")
    public void testLimitClauseWithQueryAction() {
        BValue[] returnValues = BRunUtil.invoke(result, "testLimitClauseWithQueryAction");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BInteger i = (BInteger) returnValues[0];

        Assert.assertEquals(i.intValue(), 6);
    }

    @Test(description = "Test query action with limit clause return list")
    public void testLimitClauseWithQueryAction2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testLimitClauseWithQueryAction2");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");

        BMap<String, BValue> fullName = (BMap<String, BValue>) returnValues[0];

        Assert.assertEquals(fullName.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(fullName.get("lastName").stringValue(), "George");
    }

    @Test(description = "Test limit clause when limit > number of elements")
    public void testLimitClauseWithQueryAction3() {
        BValue[] returnValues = BRunUtil.invoke(result, "testLimitClauseWithQueryAction3");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap<String, BValue> fullName1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> fullName2 = (BMap<String, BValue>) returnValues[1];

        Assert.assertEquals(fullName1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(fullName2.get("lastName").stringValue(), "Fonseka");
    }

    @Test(description = "Test limit clause when limit = number of elements")
    public void testLimitClauseWithQueryAction4() {
        BValue[] returnValues = BRunUtil.invoke(result, "testLimitClauseWithQueryAction4");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 2, "Expected events are not received");

        BMap<String, BValue> fullName1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> fullName2 = (BMap<String, BValue>) returnValues[1];

        Assert.assertEquals(fullName1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(fullName2.get("lastName").stringValue(), "Fonseka");
    }

    @Test(expectedExceptions = BLangRuntimeException.class, description = "Test limit clause with incompatible types")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 2);
        int index = 0;

        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'boolean'",
                27, 19);
        validateError(negativeResult, index, "incompatible types: expected 'int', found 'boolean'",
                66, 19);

        throw new BLangRuntimeException("Unable to assign limit", "limit cannot be < 0.");
    }
}
