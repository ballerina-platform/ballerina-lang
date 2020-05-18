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
 * This contains methods to test on conflict clause in query expression.
 *
 * @since 1.3.0
 */
public class OnConflictClauseLimitClauseTest {
    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/onConflictClause-limitClause.bal");
        negativeResult = BCompileUtil.compile("test-src/query/onConflictClause-limitClause-negative.bal");
    }

    @Test(description = "Test on conflict clause")
    public void testOnConflictClause() {
        BValue[] returnValues = BRunUtil.invoke(result, "testOnConflictClause");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(((BInteger) person3.get("age")).intValue(), 33);
    }

    @Test(description = "Test limit clause")
    public void testLimitClause() {
        BValue[] returnValues = BRunUtil.invoke(result, "testLimitClause");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(((BInteger) person3.get("age")).intValue(), 33);
    }

    @Test(description = "Test simple query action statement")
    public void testLimitClauseWithQueryAction() {
        BValue[] returnValues = BRunUtil.invoke(result, "testLimitClauseWithQueryAction");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) returnValues[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) returnValues[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) returnValues[2];

        Assert.assertEquals(person1.get("firstName").stringValue(), "Alex");
        Assert.assertEquals(person2.get("lastName").stringValue(), "Fonseka");
        Assert.assertEquals(person3.get("lastName").stringValue(), "David");
    }

    @Test(description = "Test on conflict clause and limit clause with incompatible types")
    public void testNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 4);
        int index = 0;
        validateError(negativeResult, index++, "incompatible types: expected 'error', found 'boolean'",
                29, 25);
        validateError(negativeResult, index++, "type 'error' not allowed here; expected an 'error' " +
                        "or a subtype of 'error'.",
                29, 25);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'boolean'",
                49, 19);
        validateError(negativeResult, index, "incompatible types: expected 'int', found 'boolean'",
                68, 19);
    }


}
