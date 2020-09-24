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

import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.model.arrays.
 */
public class SealedArrayTest {

    private CompileResult compileResult, resultNegative, semanticsNegative, listExprNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/sealed_array.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/arrays/sealed_array_negative.bal");
        listExprNegative = BCompileUtil.compile("test-src/statements/arrays/sealed_array_listexpr_negative.bal");
        semanticsNegative = BCompileUtil.compile("test-src/statements/arrays/sealed_array_semantics_negative" +
                                                         ".bal");
    }

    @Test
    public void testCreateIntegerSealedArray() {
        BRunUtil.invoke(compileResult, "createIntSealedArray");
        BRunUtil.invoke(compileResult, "createIntAutoFilledSealedArray");
        BRunUtil.invoke(compileResult, "createIntSealedArrayWithLabel");
        BRunUtil.invoke(compileResult, "createIntDefaultSealedArray");
        BRunUtil.invoke(compileResult, "createSealedArraysOfIntSubtypes");
    }

    @Test
    public void testCreateBooleanSealedArray() {
        BRunUtil.invoke(compileResult, "createBoolSealedArray");
        BRunUtil.invoke(compileResult, "createBoolAutoFilledSealedArray");
        BRunUtil.invoke(compileResult, "createBoolSealedArrayWithLabel");
        BRunUtil.invoke(compileResult, "createBoolDefaultSealedArray");
    }

    @Test
    public void testCreateFloatSealedArray() {
        BRunUtil.invoke(compileResult, "createFloatSealedArray");
        BRunUtil.invoke(compileResult, "createFloatAutoFilledSealedArray");
        BRunUtil.invoke(compileResult, "createFloatSealedArrayWithLabel");
        BRunUtil.invoke(compileResult, "createFloatDefaultSealedArray");
    }

    @Test
    public void testCreateStringSealedArray() {
        BRunUtil.invoke(compileResult, "createStringSealedArray");
        BRunUtil.invoke(compileResult, "createStringAutoFilledSealedArray");
        BRunUtil.invoke(compileResult, "createStringSealedArrayWithLabel");
        BRunUtil.invoke(compileResult, "createStringDefaultSealedArray");
    }

    @Test
    public void testCreateJSONSealedArray() {
        BRunUtil.invoke(compileResult, "createJSONSealedArray");
        BRunUtil.invoke(compileResult, "createJSONSealedArrayWithLabel");
        BRunUtil.invoke(compileResult, "createJSONDefaultSealedArray");
        BRunUtil.invoke(compileResult, "createJSONAutoFilledSealedArray");
    }

    @Test
    public void testCreateAnySealedArray() {
        BRunUtil.invoke(compileResult, "createAnySealedArray");
        BRunUtil.invoke(compileResult, "createAnyAutoFilledSealedArray");
        BRunUtil.invoke(compileResult, "createAnySealedArrayWithLabel");
    }

    @Test
    public void testCreateRecordSealedArray() {
        BRunUtil.invoke(compileResult, "createRecordSealedArray");
        BRunUtil.invoke(compileResult, "createRecordAutoFilledSealedArray");
        BRunUtil.invoke(compileResult, "createRecordSealedArrayWithLabel");
        BRunUtil.invoke(compileResult, "createRecordSealedArrayAutoFill");
    }

    @Test
    public void testCreateByteSealedArray() {
        BRunUtil.invoke(compileResult, "createByteSealedArray");
        BRunUtil.invoke(compileResult, "createByteAutoFilledSealedArray");
        BRunUtil.invoke(compileResult, "createByteSealedArrayWithLabel");
        BRunUtil.invoke(compileResult, "createByteDefaultSealedArray");
    }

    @Test
    public void testCreateTupleSealedArray() {
        BRunUtil.invoke(compileResult, "createTupleSealedArray");
        BRunUtil.invoke(compileResult, "createTupleAutoFilledSealedArray");
        BRunUtil.invoke(compileResult, "createTupleSealedArrayWithLabel");
    }

    @Test
    public void testFunctionParametersAndReturnValues() {
        BRunUtil.invoke(compileResult, "functionParametersAndReturns");
        BRunUtil.invoke(compileResult, "functionParametersAndReturnsAutoFilling");
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

    @Test(groups = { "brokenOnNewParser" })
    public void testNegativeAutoFillSealedArray() {
        BAssertUtil.validateError(listExprNegative, 0,
                                  "invalid usage of list constructor: type 'Person[5]' does not have a filler value",
                                  24, 19);
        BAssertUtil.validateError(listExprNegative, 1,
                                  "invalid usage of list constructor: type 'Person[5][1]' does not have a filler value",
                                  31, 22);
        BAssertUtil.validateError(listExprNegative, 2,
                                  "invalid usage of list constructor: type 'Age[5][1]' does not have a filler value",
                                  43, 19);
        BAssertUtil.validateError(listExprNegative, 3,
                                  "invalid usage of list constructor: type '1|2|3|4[3]' does not have a filler value",
                                  63, 18);
        BAssertUtil.validateError(listExprNegative, 4,
                                  "invalid usage of list constructor: type '0|0.0f|[3]' does not have a filler value",
                                  69, 34);
        BAssertUtil.validateError(listExprNegative, 5,
                                  "invalid usage of list constructor: type 'Rec[2]' does not have a filler value",
                                  88, 16);
        BAssertUtil.validateError(listExprNegative, 6,
                                  "invalid usage of list constructor: type 'RecWithManyOptional[2]' does not have a " +
                                          "filler value",
                                  92, 32);
        BAssertUtil.validateError(listExprNegative, 7,
                                  "invalid usage of list constructor: type 'RecWithOptional[2]' does not have a " +
                                          "filler value",
                                  96, 28);
        BAssertUtil.validateError(listExprNegative, 8,
                                  "invalid usage of list constructor: type 'ObjError[2]' does not have a " +
                                          "filler value",
                                  109, 22);
        BAssertUtil.validateError(listExprNegative, 9,
                                  "invalid usage of list constructor: type '(HELLO|2)[2]' does not have a filler " +
                                          "value",
                                  118, 34);
        BAssertUtil.validateError(listExprNegative, 10, "incompatible types: expected '(int|NoFillerObject[2])', " +
                "found '[]'", 122, 31);
        BAssertUtil.validateError(listExprNegative, 11, "incompatible types: expected '" +
                "(NoFillerObject[3]|NoFillerObject[2])', found '[]'", 124, 45);
        BAssertUtil.validateError(listExprNegative, 12,
                                  "invalid usage of list constructor: type '(0|1.0f)[2]' does not have a filler value",
                                  139, 43);
        BAssertUtil.validateError(listExprNegative, 13,
                                  "invalid usage of list constructor: type '(1|2)[2]' does not have a filler value",
                                  146, 31);
        BAssertUtil.validateError(listExprNegative, 14,
                                  "invalid usage of list constructor: type '(map<(foo|bar)>|map<string>)[2]' does not" +
                                          " have a filler value",
                                  155, 38);
        BAssertUtil.validateError(listExprNegative, 15,
                                  "invalid usage of list constructor: type '(int|2)[2]' does not have a filler value",
                                  162, 35);
        Assert.assertEquals(listExprNegative.getErrorCount(), 16);
    }

    @Test(groups = { "brokenOnNewParser" })
    public void testSemanticsNegativeSealedArrays() {
        Assert.assertEquals(semanticsNegative.getErrorCount(), 22);
        int i = 0;
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 19, 30);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 25, 33);
        BAssertUtil.validateError(semanticsNegative, i++, "size mismatch in sealed array. expected '4', but found '5'",
                30, 31);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 36, 18);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 37, 18);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid usage of sealed type: array not initialized",
                38, 5);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int[3]', found 'int[]'",
                45, 17);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'string[2]', found 'string[]'",
                51, 34);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'boolean[4]', found " +
                "'boolean[3]'", 51, 47);
        BAssertUtil.validateError(semanticsNegative, i++, "ambiguous type '(int|int[]|int[4])'", 62, 30);
        BAssertUtil.validateError(semanticsNegative, i++, "ambiguous type '(int|int[]|int[4]|int[5])'", 64, 40);
        BAssertUtil.validateError(semanticsNegative, i++, "size mismatch in sealed array. expected '4', but found '5'",
                78, 18);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '4'", 81, 8);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid usage of sealed type: can not infer array size", 83,
                18);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'json[3]', found 'json[]'", 85,
                18);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int', found 'S1|S2'",
                104, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid list index expression: value space '3|4|5' " +
                "out of range", 105, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid list index expression: value space '3|4|5' " +
                "out of range", 106, 23);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int', found " +
                "'0|1|2|S1'", 107, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int', found " +
                        "'(0|1|2|S1|S3)'", 108, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid list index expression: value space " +
                "'(3|4|5|7)' out of range", 109, 23);
        BAssertUtil.validateError(semanticsNegative, i, "incompatible types: expected 'int[*]', found " +
                        "'int[]'", 114, 22);
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

    @Test(description = "Test accessing invalid index of sealed auto filled array when assigned to unsealed array",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*error:.*array index out of range: index: 4, size: 3.*")
    public void assignedAutoFilledArrayInvalidIndexAccess() {
        BRunUtil.invoke(compileResult, "assignedAutoFilledArrayInvalidIndexAccess");
    }

    @Test(description = "Union array fill with an union which has default fill value as a member value.")
    public void createUnionAutoFillArray() {
        BRunUtil.invoke(compileResult, "createUnionAutoFillArray");
    }

    @Test(description = "Float Union array fill with an union which has default fill value as a member value.")
    public void createUnionFloatAutoFillArray() {
        BRunUtil.invoke(compileResult, "createUnionFloatAutoFillArray");
    }

    @Test(description = "Test union array fill with list-expr with an union with nil")
    public void createNullableUnionAutoFillArray() {
        BRunUtil.invoke(compileResult, "createNullableUnionAutoFillArray");
    }

    @Test(description = "Test non homogeneous union array fill with list-expr with an union with nil")
    public void createNullableNonHomogeneousUnionAutoFillArray() {
        BRunUtil.invoke(compileResult, "createNullableNonHomogeneousUnionAutoFillArray");
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
                    "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                            "\\{\"message\":\"array index out of range: index: 4, size: 3.*")
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
            expectedExceptionsMessageRegExp = ".*\\{\"message\":\"array index out of range: index: 3, size: 3.*")
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

    @Test(groups = { "disableOnOldParser" })
    public void testArrayWithConstantSizeReferenceFill() {
        BRunUtil.invokeFunction(compileResult, "testArrayWithConstantSizeReferenceFill");
    }

    @Test
    public void testCreateXMLSealedArray() {
        BRunUtil.invoke(compileResult, "createXMLAutoFilledSealedArray");
    }

    @Test
    public void createConstLiteralAutoFilledSealedArray() {
        BRunUtil.invoke(compileResult, "createConstLiteralAutoFilledSealedArray");
    }
}
