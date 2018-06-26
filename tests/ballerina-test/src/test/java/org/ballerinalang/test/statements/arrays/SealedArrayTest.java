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
package org.ballerinalang.test.statements.arrays;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.model.arrays.
 */
public class SealedArrayTest {

    private CompileResult compileResult;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/sealed-array.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/arrays/negative-sealed-array.bal");
    }

    @Test
    public void testCreateIntegerSealedArray() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "createIntSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createIntSealedArrayWithLabel");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createIntDefaultSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(), "[0, 0, 0, 0, 0]", "Length didn't match");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 5, "Length didn't match");
    }

    @Test
    public void testCreateBooleanSealedArray() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "createBoolSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createBoolSealedArrayWithLabel");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createBoolDefaultSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(),
                "[false, false, false, false, false]", "Length didn't match");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 5, "Length didn't match");
    }

    @Test
    public void testCreateFloatSealedArray() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "createFloatSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createFloatSealedArrayWithLabel");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createFloatDefaultSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(), "[0.0, 0.0, 0.0, 0.0, 0.0]", "Length didn't match");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 5, "Length didn't match");
    }

    @Test
    public void testCreateStringSealedArray() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "createStringSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createStringSealedArrayWithLabel");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createStringDefaultSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(), "[\"\", \"\", \"\", \"\", \"\"]", "Length didn't match");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 5, "Length didn't match");
    }

    @Test
    public void testCreateAnySealedArray() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "createAnySealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createAnySealedArrayWithLabel");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");
    }

    @Test
    public void testCreateRecordSealedArray() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "createRecordSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createRecordSealedArrayWithLabel");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");
    }

    @Test
    public void testCreateBlobSealedArray() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "createBlobSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createBlobSealedArrayWithLabel");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");
    }

    @Test
    public void testCreateTupleSealedArray() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "createTupleSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 3, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createTupleSealedArrayWithLabel");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testFunctionParametersAndReturnValues() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "functionParametersAndReturns");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 3, "Length didn't match");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 2, "Length didn't match");
    }

    @Test
    public void testNegativeSealedArrays() {
        BAssertUtil.validateError(resultNegative, 0, "array index out of range: index: '5', size: '5'", 19, 30);
        BAssertUtil.validateError(resultNegative, 1, "array index out of range: index: '5', size: '5'", 25, 33);
        BAssertUtil.validateError(
                resultNegative, 2, "size mismatch in sealed array. expected '4', but found '3'", 30, 31);
        BAssertUtil.validateError(
                resultNegative, 3, "size mismatch in sealed array. expected '4', but found '5'", 31, 31);
        BAssertUtil.validateError(
                resultNegative, 4, "array index out of range: index: '5', size: '5'", 37, 18);
        BAssertUtil.validateError(
                resultNegative, 5, "array index out of range: index: '5', size: '5'", 38, 18);
        BAssertUtil.validateError(
                resultNegative, 6, "sealed keyword is not allowed for type ", 39, 5);
        BAssertUtil.validateError(
                resultNegative, 7, "incompatible types: expected 'int[3]', found 'int[]'", 46, 17);
        BAssertUtil.validateError(
                resultNegative, 8, "incompatible types: expected 'boolean[4]', found 'boolean[3]'", 52, 47);
        BAssertUtil.validateError(
                resultNegative, 9, "incompatible types: expected 'string[2]', found 'string[]'", 52, 34);
    }

}
