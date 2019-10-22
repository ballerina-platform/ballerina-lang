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

import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.model.arrays.
 */
public class SealedArrayTest {

    private CompileResult compileResult, resultNegative, semanticsNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/sealed-array.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/arrays/sealed-array-negative.bal");
        semanticsNegative = BCompileUtil.compile("test-src/statements/arrays/sealed-array-semantics-negative" +
                ".bal");
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
    public void testCreateJSONSealedArray() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "createJSONSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createJSONSealedArrayWithLabel");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createJSONDefaultSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(), "[null, null, null, null, null]", "Length didn't match");
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
    public void testCreateByteSealedArray() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "createByteSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createByteSealedArrayWithLabel");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Length didn't match");

        returnValues = BRunUtil.invoke(compileResult, "createByteDefaultSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(), "[0, 0, 0, 0, 0]", "Length didn't match");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 5, "Length didn't match");
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
    public void testUnionAndMatchSealedArrayStatement() {
        BValueArray bFloatArray = new BValueArray(BTypes.typeFloat, 4);
        bFloatArray.add(0, 01.0);
        bFloatArray.add(0, 12.2);
        bFloatArray.add(0, 23.3);
        bFloatArray.add(0, 34.4);
        BValue[] args = {bFloatArray};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "unionAndMatchStatementSealedArray", args);
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(),
                "matched sealed float array size 4", "Couldn't match sealed array type");

        bFloatArray = new BValueArray(BTypes.typeFloat, 5);
        bFloatArray.add(0, 01.0);
        bFloatArray.add(0, 12.2);
        bFloatArray.add(0, 23.3);
        bFloatArray.add(0, 34.4);
        bFloatArray.add(0, 45.5);
        BValue[] args2 = {bFloatArray};
        returnValues = BRunUtil.invoke(compileResult, "unionAndMatchStatementSealedArray", args2);
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(),
                "matched float array", "Couldn't match sealed array type");

        bFloatArray = new BValueArray(BTypes.typeFloat);
        bFloatArray.add(0, 01.0);
        bFloatArray.add(0, 12.2);
        bFloatArray.add(0, 23.3);
        bFloatArray.add(0, 34.4);
        BValue[] args3 = {bFloatArray};
        returnValues = BRunUtil.invoke(compileResult, "unionAndMatchStatementUnsealedArray", args3);
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(),
                "matched float array", "Couldn't match unsealed array type");
    }

    @Test
    public void testUnionAndMatchNoSealedArrayStatement() {
        BValueArray bFloatArray = new BValueArray(BTypes.typeFloat, 4);
        bFloatArray.add(0, 01.0);
        bFloatArray.add(0, 12.2);
        bFloatArray.add(0, 23.3);
        bFloatArray.add(0, 34.4);
        BValue[] args = {bFloatArray};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "unionAndMatchStatementUnsealedArray", args);
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(),
                "matched float array", "Couldn't match sealed array type");

        bFloatArray = new BValueArray(BTypes.typeFloat);
        bFloatArray.add(0, 01.0);
        bFloatArray.add(0, 12.2);
        bFloatArray.add(0, 23.3);
        bFloatArray.add(0, 34.4);
        BValue[] args2 = {bFloatArray};
        returnValues = BRunUtil.invoke(compileResult, "unionAndMatchStatementUnsealedArray", args2);
        Assert.assertFalse(
                returnValues == null || returnValues.length == 0 || returnValues[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(),
                "matched float array", "Couldn't match unsealed array type");
    }

    @Test()
    public void testNegativeSealedArrays() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "variable 'sealedArray1' is not initialized", 19, 5);
    }

    @Test()
    public void testSemanticsNegativeSealedArrays() {
        Assert.assertEquals(semanticsNegative.getErrorCount(), 23);
        int i = 0;
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 19, 30);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 25, 33);
        BAssertUtil.validateError(semanticsNegative, i++, "size mismatch in sealed array. expected '4', but found '3'",
                30, 31);
        BAssertUtil.validateError(semanticsNegative, i++, "size mismatch in sealed array. expected '4', but found '5'",
                31, 31);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 37, 18);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 38, 18);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid usage of sealed type: array not initialized",
                39, 5);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int[3]', found 'int[]'",
                46, 17);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'string[2]', found 'string[]'",
                52, 34);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'boolean[4]', found " +
                "'boolean[3]'", 52, 47);
        BAssertUtil.validateError(semanticsNegative, i++, "ambiguous type '(int|int[]|int[4])'", 63, 30);
        BAssertUtil.validateError(semanticsNegative, i++, "ambiguous type '(int|int[]|int[4]|int[5])'", 65, 40);
        BAssertUtil.validateError(semanticsNegative, i++, "size mismatch in sealed array. expected '4', but found '2'",
                79, 18);
        BAssertUtil.validateError(semanticsNegative, i++, "size mismatch in sealed array. expected '4', but found '5'",
                80, 18);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '4'", 83, 8);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid usage of sealed type: can not infer array size", 85,
                18);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'json[3]', found 'json[]'", 87,
                18);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int', found 'S1|S2'",
                106, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid list index expression: value space '3|4|5' " +
                "out of range", 107, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid list index expression: value space '3|4|5' " +
                "out of range", 108, 23);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int', found " +
                "'0|1|2|S1'", 109, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int', found " +
                        "'(0|1|2|S1|S3)'", 110, 20);
        BAssertUtil.validateError(semanticsNegative, i, "invalid list index expression: value space " +
                "'(3|4|5|7)' out of range", 111, 23);
    }

    @Test(description = "Test accessing invalid index of sealed array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*array index out of range: index: 5, size: 3.*")
    public void invalidIndexAccess() {
        BValue[] args = {new BInteger(5)};
        BRunUtil.invoke(compileResult, "invalidIndexAccess", args);
    }

    @Test(description = "Test accessing invalid index of sealed array when assigned to unsealed array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*array index out of range: index: 4, size: 3.*")
    public void assignedArrayInvalidIndexAccess() {
        BRunUtil.invoke(compileResult, "assignedArrayInvalidIndexAccess");
    }

    @Test(description = "Test accessing invalid index of sealed array matched union type",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*array index out of range: index: 5, size: 3.*")
    public void accessInvalidIndexOfMatchedSealedArray() {
        BValueArray bIntArray = new BValueArray(BTypes.typeInt, 3);
        bIntArray.add(0, 1);
        bIntArray.add(0, 3);
        bIntArray.add(0, 5);
        BValue[] args = {bIntArray, new BInteger(5)};
        BRunUtil.invoke(compileResult, "accessIndexOfMatchedSealedArray", args);
    }

    @Test(description = "Test accessing invalid index of sealed array matched union type",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}IndexOutOfRange message=failed to set element to json: " +
                            "message=array index out of range: index: 4, size: 3.*")
    public void accessInvalidIndexJSONArray() {
        BInteger bInteger = new BInteger(1);
        BInteger bInteger2 = new BInteger(4);
        BValue[] args = {bInteger};
        BValue[] args2 = {bInteger2};
        BRunUtil.invoke(compileResult, "invalidIndexJSONArray", args);
        BRunUtil.invoke(compileResult, "invalidIndexJSONArray", args2);
    }

    @Test(description = "Test accessing invalid index of sealed array matched union type",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*message=failed to set element to json: message=array index out of range: index: 3, size: 3.*")
    public void invalidIndexReferenceJSONArray() {
        BRunUtil.invoke(compileResult, "invalidIndexReferenceJSONArray");
    }

    @Test
    public void accessValidIndexOfMatchedUnsealedArray() {
        BValueArray bIntArray = new BValueArray(BTypes.typeInt);
        bIntArray.add(0, 1);
        bIntArray.add(0, 3);
        bIntArray.add(0, 5);
        BValue[] args = {bIntArray, new BInteger(5)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "accessIndexOfMatchedSealedArray", args);
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 10, "Invalid match for sealed array");
    }

    @Test
    public void testSealedArrayConstrainedMap() {
        BValueArray bIntArray = new BValueArray(BTypes.typeInt, 3);
        bIntArray.add(0, 1);
        bIntArray.add(1, 3);
        bIntArray.add(2, 5);
        BValueArray bIntArray2 = new BValueArray(BTypes.typeInt);
        bIntArray2.add(0, 1);
        bIntArray2.add(1, 3);
        bIntArray2.add(2, 5);
        BValue[] args = {bIntArray, bIntArray2};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSealedArrayConstrainedMap", args);
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Invalid match for sealed array");
    }

    @Test(description = "Test accessing invalid index of sealed array of constrained map",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*array index out of range: index: 3, size: 3.*")
    public void testSealedArrayConstrainedMapInvalidIndex() {
        BValueArray bIntArray = new BValueArray(BTypes.typeInt, 3);
        bIntArray.add(0, 1);
        bIntArray.add(1, 3);
        bIntArray.add(2, 5);
        BValue[] args = {bIntArray, new BInteger(3)};
        BRunUtil.invoke(compileResult, "testSealedArrayConstrainedMapInvalidIndex", args);
    }
}
