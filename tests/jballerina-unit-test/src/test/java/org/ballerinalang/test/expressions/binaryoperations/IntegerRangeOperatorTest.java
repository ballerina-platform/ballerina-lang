/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test the functionality of integer range operators.
 */
@Test(groups = { "brokenOnNewParser" })
public class IntegerRangeOperatorTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/integer_range_operators.bal");
        negativeResult = BCompileUtil.
                compile("test-src/expressions/binaryoperations/integer_range_operators_negative.bal");
    }

    @Test(description = "Test closed integer range when the start value is less than the end value")
    public void testStartLessThanEndClosedIntRange() {
        int startValue = 12;
        int endValue = 15;
        BValue[] args = {new BInteger(startValue), new BInteger(endValue)};
        BValue[] returns = BRunUtil.invoke(result, "testClosedIntRange", args);
        Assert.assertEquals(returns.length, 1);

        BValueArray returnArray = (BValueArray) returns[0];
        int expectedSize = endValue - startValue + 1;
        Assert.assertEquals(returnArray.size(), expectedSize, "Incorrect number of values returned for Closed "
                + "Integer Range " + startValue + " ... " + endValue);
        for (int i = 0; i < expectedSize; i++) {
            Assert.assertEquals(returnArray.getInt(i), startValue + i, "Incorrect value found at index: " + i);
        }
    }

    @Test(description = "Test closed integer range when the start value is equal to the end value")
    public void testStartEqualsEndClosedIntRange() {
        int startValue = 25;
        int endValue = 25;
        BValue[] args = {new BInteger(startValue), new BInteger(endValue)};
        BValue[] returns = BRunUtil.invoke(result, "testClosedIntRange", args);
        Assert.assertEquals(returns.length, 1);

        BValueArray returnArray = (BValueArray) returns[0];
        int expectedSize = 1;
        Assert.assertEquals(returnArray.size(), expectedSize, "Incorrect number of values returned for Closed "
                + "Integer Range " + startValue + " ... " + endValue);
        for (int i = 0; i < expectedSize; i++) {
            Assert.assertEquals(returnArray.getInt(i), startValue + i, "Incorrect value found at index: " + i);
        }
    }

    @Test(description = "Test closed integer range when the start value is greater than the end value")
    public void testStartGreaterThanEndClosedIntRange() {
        int startValue = 40;
        int endValue = 25;
        BValue[] args = {new BInteger(startValue), new BInteger(endValue)};
        BValue[] returns = BRunUtil.invoke(result, "testClosedIntRange", args);
        Assert.assertEquals(returns.length, 1);

        BValueArray returnArray = (BValueArray) returns[0];
        int expectedSize = 0;
        Assert.assertEquals(returnArray.size(), expectedSize, "Incorrect number of values returned for Closed "
                + "Integer Range " + startValue + " ... " + endValue);
    }

    @Test(description = "Test closed integer range as an array")
    public void testClosedIntRangeAsArray() {
        int startValue = 21;
        int endValue = 29;
        BValue[] args = {new BInteger(startValue), new BInteger(endValue)};
        BValue[] returns = BRunUtil.invoke(result, "testClosedIntRangeAsArray", args);
        Assert.assertEquals(returns.length, 1);

        BValueArray returnArray = (BValueArray) returns[0];
        int expectedSize = endValue - startValue + 1;
        Assert.assertEquals(returnArray.size(), expectedSize, "Incorrect number of values returned for Closed "
                + "Integer Range " + startValue + " ... " + endValue + " accessed as an array");
        for (int i = 0; i < expectedSize; i++) {
            Assert.assertEquals(returnArray.getInt(i), startValue + i, "Incorrect value found at index: " + i);
        }
    }

    @Test(description = "Test half open integer range when the start value is less than the end value")
    public void testStartLessThanEndHalfOpenIntRange() {
        int startValue = 12;
        int endValue = 15;
        BValue[] args = {new BInteger(startValue), new BInteger(endValue)};
        BValue[] returns = BRunUtil.invoke(result, "testHalfOpenIntRange", args);
        Assert.assertEquals(returns.length, 1);

        BValueArray returnArray = (BValueArray) returns[0];
        int expectedSize = endValue - startValue;
        Assert.assertEquals(returnArray.size(), expectedSize, "Incorrect number of values returned for Half"
                + " Open Integer Range " + startValue + " ..< " + endValue);
        for (int i = 0; i < expectedSize; i++) {
            Assert.assertEquals(returnArray.getInt(i), startValue + i, "Incorrect value found at index: " + i);
        }
    }

    @Test(description = "Test half open integer range when the start value is equal to the end value")
    public void testStartEqualsEndHalfOpenIntRange() {
        int startValue = 25;
        int endValue = 25;
        BValue[] args = {new BInteger(startValue), new BInteger(endValue)};
        BValue[] returns = BRunUtil.invoke(result, "testHalfOpenIntRange", args);
        Assert.assertEquals(returns.length, 1);

        BValueArray returnArray = (BValueArray) returns[0];
        int expectedSize = 0;
        Assert.assertEquals(returnArray.size(), expectedSize, "Incorrect number of values returned for Half"
                + " Open Integer Range " + startValue + " ..< " + endValue);
    }

    @Test(description = "Test half open integer range when the start value is greater than the end value")
    public void testStartGreaterThanEndHalfOpenIntRange() {
        int startValue = 40;
        int endValue = 25;
        BValue[] args = {new BInteger(startValue), new BInteger(endValue)};
        BValue[] returns = BRunUtil.invoke(result, "testHalfOpenIntRange", args);
        Assert.assertEquals(returns.length, 1);

        BValueArray returnArray = (BValueArray) returns[0];
        int expectedSize = 0;
        Assert.assertEquals(returnArray.size(), expectedSize, "Incorrect number of values returned for Half"
                + " Open Integer Range " + startValue + " ..< " + endValue);
    }

    @Test(description = "Test half open integer range as an array")
    public void testHalfOpenIntRangeAsArray() {
        int startValue = 291;
        int endValue = 310;
        BValue[] args = {new BInteger(startValue), new BInteger(endValue)};
        BValue[] returns = BRunUtil.invoke(result, "testHalfOpenIntRangeAsArray", args);
        Assert.assertEquals(returns.length, 1);

        BValueArray returnArray = (BValueArray) returns[0];
        int expectedSize = endValue - startValue;
        Assert.assertEquals(returnArray.size(), expectedSize, "Incorrect number of values returned for Half "
                + "Open Integer Range " + startValue + " ..< " + endValue + " accessed as an array");
        for (int i = 0; i < expectedSize; i++) {
            Assert.assertEquals(returnArray.getInt(i), startValue + i, "Incorrect value found at index: " + i);
        }
    }


    @Test(description = "Test integer range operators with errors")
    public void testSubtractStmtNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 8);
        BAssertUtil.validateError(negativeResult, 0, "operator '...' not defined for 'string' and 'string'", 2, 25);
        BAssertUtil.validateError(negativeResult, 1, "operator '...' not defined for 'json' and 'json'", 5, 24);
        BAssertUtil.validateError(negativeResult, 2, "operator '...' not defined for 'float' and 'float'", 6, 26);
        BAssertUtil.validateError(negativeResult, 3, "operator '...' not defined for 'int' and 'float'", 7, 25);
        BAssertUtil.validateError(negativeResult, 4, "operator '..<' not defined for 'string' and 'string'", 11, 25);
        BAssertUtil.validateError(negativeResult, 5, "operator '..<' not defined for 'json' and 'json'", 14, 24);
        BAssertUtil.validateError(negativeResult, 6, "operator '..<' not defined for 'float' and 'float'", 15, 26);
        BAssertUtil.validateError(negativeResult, 7, "operator '..<' not defined for 'int' and 'float'", 16, 25);
    }

}
