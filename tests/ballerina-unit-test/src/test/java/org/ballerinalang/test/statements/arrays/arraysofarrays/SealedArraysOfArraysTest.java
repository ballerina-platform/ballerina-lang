/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.statements.arrays.arraysofarrays;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.model.arrays.
 */
public class SealedArraysOfArraysTest {

    private CompileResult compileResult;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil
                .compile("test-src/statements/arrays/arraysofarrays/sealed-arrays-of-arrays.bal");
        resultNegative = BCompileUtil
                .compile("test-src/statements/arrays/arraysofarrays/negative-sealed-arrays-of-arrays.bal");
    }

    @Test
    public void testInitializeTwoDArray() {

        BValue[] returnValues = BRunUtil.invoke(compileResult, "initTwoDimensionalSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 3, "Value didn't match");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 4, "Value didn't match");
        Assert.assertEquals(((BInteger) returnValues[2]).intValue(), 2, "Value didn't match");
        Assert.assertEquals(((BInteger) returnValues[3]).intValue(), 3, "Value didn't match");
    }

    @Test
    public void testInitializeThreeDArray() {

        BValue[] returnValues = BRunUtil.invoke(compileResult, "initThreeDimensionalSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 3, "Value didn't match");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 4, "Value didn't match");
        Assert.assertEquals(((BInteger) returnValues[2]).intValue(), 5, "Value didn't match");
        Assert.assertEquals(((BInteger) returnValues[3]).intValue(), 2, "Value didn't match");
        Assert.assertEquals(((BInteger) returnValues[4]).intValue(), 3, "Value didn't match");
        Assert.assertEquals(((BInteger) returnValues[5]).intValue(), 3, "Value didn't match");
    }

    @Test
    public void testIntegerSealedArraysOfArrays() {

        BIntArray arrayValue = new BIntArray(2);
        arrayValue.add(0, 10);
        arrayValue.add(1, 12);
        BValue[] args = {arrayValue};

        BValue[] returnValues = BRunUtil.invoke(compileResult, "twoDArrayIntAssignment", args);
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 3, "Value didn't match");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 10, "Value didn't match");
        Assert.assertEquals(((BInteger) returnValues[2]).intValue(), 3, "Value didn't match");
    }

    @Test
    public void testStringSealedArraysOfArrays() {

        BStringArray arrayValue = new BStringArray(2);
        arrayValue.add(0, "ballerina");
        arrayValue.add(1, "multidimensional");
        BValue[] args = {arrayValue};

        BValue[] returnValues = BRunUtil.invoke(compileResult, "twoDArrayStringAssignment", args);
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals((returnValues[0]).stringValue(), "val1", "Value didn't match");
        Assert.assertEquals((returnValues[1]).stringValue(), "ballerina", "Value didn't match");
        Assert.assertEquals((returnValues[2]).stringValue(), "val1", "Value didn't match");
    }

    @Test
    public void testNegativeSealedArraysOfArrays() {
        BAssertUtil.validateError(
                resultNegative, 0, "size mismatch in sealed array. expected '2', but found '3'", 19, 23);
        BAssertUtil.validateError(
                resultNegative, 1, "size mismatch in sealed array. expected '2', but found '3'", 20, 36);
        BAssertUtil.validateError(
                resultNegative, 2, "size mismatch in sealed array. expected '2', but found '3'", 21, 24);
        BAssertUtil.validateError(resultNegative, 3, "array index out of range: index: '2', size: '2'", 27, 21);
        BAssertUtil.validateError(resultNegative, 4, "array index out of range: index: '3', size: '3'", 37, 8);
        BAssertUtil.validateError(resultNegative, 5, "array index out of range: index: '3', size: '2'", 38, 11);
        BAssertUtil.validateError(resultNegative, 6, "array index out of range: index: '3', size: '3'", 39, 14);
        BAssertUtil.validateError(
                resultNegative, 7, "size mismatch in sealed array. expected '3', but found '4'", 40, 16);
        BAssertUtil.validateError(
                resultNegative, 8, "incompatible types: expected 'int[3]', found 'int'", 41, 14);
        BAssertUtil.validateError(
                resultNegative, 9, "incompatible types: expected 'int[3]', found 'int'", 41, 17);
        BAssertUtil.validateError(
                resultNegative, 10, "size mismatch in sealed array. expected '3', but found '4'", 42, 25);
        BAssertUtil.validateError(
                resultNegative, 11, "incompatible types: expected 'int[3]', found 'int[]'", 47, 21);
        BAssertUtil.validateError(
                resultNegative, 12, "incompatible types: expected 'int[3]', found 'int[]'", 47, 25);
        BAssertUtil.validateError(
                resultNegative, 13, "array index out of range: index: '4', size: '3'", 52, 11);
        BAssertUtil.validateError(
                resultNegative, 14, "incompatible types: expected 'json[2]', found 'json[]'", 60, 12);
        BAssertUtil.validateError(
                resultNegative, 15, "incompatible types: expected 'json[3][3]', found 'boolean'", 63, 13);
        BAssertUtil.validateError(
                resultNegative, 16, "array index out of range: index: '2', size: '2'", 63, 8);
        BAssertUtil.validateError(
                resultNegative, 17, "array index out of range: index: '4', size: '3'", 64, 14);
        BAssertUtil.validateError(
                resultNegative, 18, "array index out of range: index: '4', size: '3'", 68, 14);
        BAssertUtil.validateError(
                resultNegative, 19, "size mismatch in sealed array. expected '3', but found '5'", 73, 49);
        BAssertUtil.validateError(
                resultNegative, 20, "size mismatch in sealed array. expected '3', but found '4'", 73, 69);
        BAssertUtil.validateError(
                resultNegative, 21, "invalid usage of sealed type: can not infer array size", 75, 25);
        BAssertUtil.validateError(
                resultNegative, 22, "invalid usage of sealed type: can not infer array size", 75, 29);
        BAssertUtil.validateError(
                resultNegative, 23, "invalid usage of sealed type: array not initialized", 76, 5);
        BAssertUtil.validateError(
                resultNegative, 24, "array index out of range: index: '4', size: '3'", 83, 11);
    }
}
