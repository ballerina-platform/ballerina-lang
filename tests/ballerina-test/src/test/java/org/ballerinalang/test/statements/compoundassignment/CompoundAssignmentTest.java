/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.compoundassignment;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Class to test compound assignment statements.
 */
public class CompoundAssignmentTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(this, "test-src", "statements/compoundassignment/compoundassignment.bal");
    }

    @Test
    public void testCompoundAssignmentAddition() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAddition");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 15);
    }

    @Test
    public void testCompoundAssignmentSubtraction() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentSubtraction");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -5);
    }

    @Test
    public void testCompoundAssignmentMultiplication() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentMultiplication");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
    }

    @Test
    public void testCompoundAssignmentDivision() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentDivision");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test
    public void testIncrementOperator() {
        BValue[] returns = BRunUtil.invoke(result, "testIncrementOperator");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 101);
    }

    @Test
    public void testDecrementOperator() {
        BValue[] returns = BRunUtil.invoke(result, "testDecrementOperator");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 99);
    }

    @Test
    public void testCompoundAssignmentAdditionArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 110);
    }

    @Test
    public void testCompoundAssignmentSubtractionArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentSubtractionArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 90);
    }

    @Test
    public void testCompoundAssignmentMultiplicationArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentMultiplicationArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1000);
    }

    @Test
    public void testCompoundAssignmentDivisionArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentDivisionArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test
    public void testCompoundAssignmentAdditionStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentAdditionStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 110);
    }

    @Test
    public void testCompoundAssignmentSubtractionStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentSubtractionStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 90);
    }

    @Test
    public void testCompoundAssignmentMultiplicationStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentMultiplicationStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1000);
    }

    @Test
    public void testCompoundAssignmentDivisionStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testCompoundAssignmentDivisionStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test
    public void testIncrementOperatorArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testIncrementOperatorArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 101);
    }

    @Test
    public void testDecrementOperatorArrayElement() {
        BValue[] returns = BRunUtil.invoke(result, "testDecrementOperatorArrayElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 99);
    }

    @Test
    public void testIncrementOperatorStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testIncrementOperatorStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 889);
    }

    @Test
    public void testDecrementOperatorStructElement() {
        BValue[] returns = BRunUtil.invoke(result, "testDecrementOperatorStructElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 887);
    }
}
