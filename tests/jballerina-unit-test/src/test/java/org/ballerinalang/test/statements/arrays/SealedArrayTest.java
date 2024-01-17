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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.model.arrays.
 */
public class SealedArrayTest {

    private CompileResult compileResult, resultNegative, semanticsNegative, listExprNegative, codeAnalysisNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/sealed_array.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/arrays/sealed_array_negative.bal");
        listExprNegative = BCompileUtil.compile("test-src/statements/arrays/sealed_array_listexpr_negative.bal");
        semanticsNegative = BCompileUtil.compile(
                "test-src/statements/arrays/sealed_array_semantics_negative.bal");
        codeAnalysisNegative = BCompileUtil.compile(
                "test-src/statements/arrays/sealed_array_code_analysis_negative.bal");
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
        BArray bFloatArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_FLOAT));
        bFloatArray.add(0, 01.0);
        bFloatArray.add(0, 12.2);
        bFloatArray.add(0, 23.3);
        bFloatArray.add(0, 34.4);
        Object[] args = {bFloatArray};
        Object returnValues = BRunUtil.invoke(compileResult, "unionAndMatchStatementSealedArray", args);
        Assert.assertFalse(returnValues == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues.toString(), "matched float array", "Couldn't match sealed array type");

        bFloatArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_FLOAT, 5));
        bFloatArray.add(0, 01.0);
        bFloatArray.add(0, 12.2);
        bFloatArray.add(0, 23.3);
        bFloatArray.add(0, 34.4);
        bFloatArray.add(0, 45.5);
        Object[] args2 = {bFloatArray};
        returnValues = BRunUtil.invoke(compileResult, "unionAndMatchStatementSealedArray", args2);
        Assert.assertFalse(returnValues == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues.toString(),
                "matched float array", "Couldn't match sealed array type");

        bFloatArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_FLOAT));
        bFloatArray.add(0, 01.0);
        bFloatArray.add(0, 12.2);
        bFloatArray.add(0, 23.3);
        bFloatArray.add(0, 34.4);
        Object[] args3 = {bFloatArray};
        returnValues = BRunUtil.invoke(compileResult, "unionAndMatchStatementUnsealedArray", args3);
        Assert.assertFalse(returnValues == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues.toString(),
                "matched float array", "Couldn't match unsealed array type");
    }

    @Test
    public void testUnionAndMatchNoSealedArrayStatement() {
        BArray bFloatArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_FLOAT, 4));
        bFloatArray.add(0, 01.0);
        bFloatArray.add(0, 12.2);
        bFloatArray.add(0, 23.3);
        bFloatArray.add(0, 34.4);
        Object[] args = {bFloatArray};
        Object returnValues = BRunUtil.invoke(compileResult, "unionAndMatchStatementUnsealedArray", args);
        Assert.assertFalse(returnValues == null, "Invalid Return Values.");
        Assert.assertEquals(returnValues.toString(),
                "matched float array", "Couldn't match sealed array type");

        bFloatArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_FLOAT));
        bFloatArray.add(0, 01.0);
        bFloatArray.add(0, 12.2);
        bFloatArray.add(0, 23.3);
        bFloatArray.add(0, 34.4);
        Object[] args2 = {bFloatArray};
        returnValues = BRunUtil.invoke(compileResult, "unionAndMatchStatementUnsealedArray", args2);
        Assert.assertFalse(returnValues == null , "Invalid Return Values.");
        Assert.assertEquals(returnValues.toString(),
                "matched float array", "Couldn't match unsealed array type");
    }

    @Test()
    public void testNegativeSealedArrays() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "variable 'sealedArray1' is not initialized", 19, 5);
    }

    @Test
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
                                  "invalid usage of list constructor: type 'myVar[3]' does not have a filler value",
                                  63, 18);
        BAssertUtil.validateError(listExprNegative, 4,
                                  "invalid usage of list constructor: type 'myNonHomogeneousUnion[3]' " +
                                          "does not have a filler value", 69, 34);
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
                                  "invalid usage of list constructor: type 'unionWithConst[2]' does not have a filler" +
                                          " value", 118, 34);
        BAssertUtil.validateError(listExprNegative, 10, "incompatible types: expected '(int|NoFillerObject[2])', " +
                "found '[]'", 122, 31);
        BAssertUtil.validateError(listExprNegative, 11, "incompatible types: expected '" +
                "(NoFillerObject[3]|NoFillerObject[2])', found '[]'", 124, 45);
        BAssertUtil.validateError(listExprNegative, 12,
                                  "invalid usage of list constructor: type 'unionWithIntFloatConsts[2]' does not have" +
                                          " a filler value", 139, 43);
        BAssertUtil.validateError(listExprNegative, 13,
                                  "invalid usage of list constructor: type 'INT_ONE_TWO[2]' does not have a filler " +
                                          "value", 146, 31);
        BAssertUtil.validateError(listExprNegative, 14,
                                  "invalid usage of list constructor: type '(map<FooBar>|map<string>)[2]' does not" +
                                          " have a filler value", 155, 38);
        Assert.assertEquals(listExprNegative.getErrorCount(), 15);
    }

    @Test
    public void testSemanticsNegativeSealedArrays() {
        int i = 0;
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 19, 30);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 25, 33);
        BAssertUtil.validateError(semanticsNegative, i++, "size mismatch in closed array. expected '4', but found '5'",
                30, 31);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 36, 18);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '5'", 37, 18);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid usage of closed type: array not initialized",
                38, 5);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int[3]', found 'int[]'",
                45, 17);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'string[2]', found 'string[]'",
                51, 34);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'boolean[4]', found " +
                "'boolean[3]'", 51, 47);
        BAssertUtil.validateError(semanticsNegative, i++, "ambiguous type '(int|int[]|int[4])'", 62, 30);
        BAssertUtil.validateError(semanticsNegative, i++, "ambiguous type '(int|int[]|int[4]|int[5])'", 64, 40);
        BAssertUtil.validateError(semanticsNegative, i++, "size mismatch in closed array. expected '4', but found '5'",
                78, 18);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '4'", 81, 8);
        BAssertUtil.validateError(semanticsNegative, i++, "length of the array cannot be inferred from the context", 83,
                18);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'json[3]', found 'json[]'", 85,
                18);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int', found 'FiniteOne'",
                104, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid list member access expression: " +
                "value space 'FiniteTwo' out of range", 105, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid list member access expression: " +
                "value space 'FiniteTwo' out of range", 106, 23);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int', found " +
                "'FiniteThree'", 107, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int', found " +
                        "'FiniteFour'", 108, 20);
        BAssertUtil.validateError(semanticsNegative, i++, "invalid list member access expression: value space " +
                "'FiniteFive' out of range", 109, 23);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int[*]', found " +
                        "'int[]'", 114, 22);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '3'", 123, 15);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '3'", 124, 15);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '3'", 125, 7);
        BAssertUtil.validateError(semanticsNegative, i++, "list index out of range: index: '3'", 126, 7);
        BAssertUtil.validateError(semanticsNegative, i++, "incompatible types: expected 'int[*]', found 'int[1]'",
                133, 13);
        Assert.assertEquals(semanticsNegative.getErrorCount(), i);
    }

    @Test
    public void testCodeAnalysisNegativeSealedArrays() {
        int i = 0;
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                18, 4);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                21, 16);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                26, 15);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                27, 43);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                28, 40);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                36, 24);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                40, 24);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                44, 41);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                44, 51);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                48, 14);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                51, 2);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                52, 9);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                53, 2);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                55, 5);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                56, 11);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                57, 5);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                58, 11);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                58, 54);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                58, 78);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                59, 39);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "length of the array cannot be inferred from the context",
                60, 36);
        Assert.assertEquals(codeAnalysisNegative.getErrorCount(), i);
    }

    @Test(description = "Test accessing invalid index of sealed array",
            expectedExceptions = {BLangTestException.class},
            expectedExceptionsMessageRegExp = ".*error:.*array index out of range: index: 5, size: 3.*")
    public void invalidIndexAccess() {
        Object[] args = {5};
        BRunUtil.invoke(compileResult, "invalidIndexAccess", args);
    }

    @Test(description = "Test accessing invalid index of sealed array when assigned to unsealed array",
            expectedExceptions = {BLangTestException.class},
            expectedExceptionsMessageRegExp = ".*error:.*array index out of range: index: 4, size: 3.*")
    public void assignedArrayInvalidIndexAccess() {
        BRunUtil.invoke(compileResult, "assignedArrayInvalidIndexAccess");
    }

    @Test(description = "Test accessing invalid index of sealed auto filled array when assigned to unsealed array",
          expectedExceptions = {BLangTestException.class},
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
            expectedExceptions = {BLangTestException.class},
            expectedExceptionsMessageRegExp = ".*error:.*array index out of range: index: 5, size: 3.*")
    public void accessInvalidIndexOfMatchedSealedArray() {
        BArray bIntArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_INT, 3));
        bIntArray.add(0, 1);
        bIntArray.add(0, 3);
        bIntArray.add(0, 5);
        Object[] args = {bIntArray, (5)};
        BRunUtil.invoke(compileResult, "accessIndexOfMatchedSealedArray", args);
    }

    @Test(description = "Test accessing invalid index of sealed array matched union type",
            expectedExceptions = {BLangTestException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                            "\\{\"message\":\"array index out of range: index: 4, size: 3.*")
    public void accessInvalidIndexJSONArray() {
        long bInteger = (1);
        long bInteger2 = (4);
        Object[] args = {bInteger};
        Object[] args2 = {bInteger2};
        BRunUtil.invoke(compileResult, "invalidIndexJSONArray", args);
        BRunUtil.invoke(compileResult, "invalidIndexJSONArray", args2);
    }

    @Test(description = "Test accessing invalid index of sealed array matched union type",
            expectedExceptions = {BLangTestException.class},
            expectedExceptionsMessageRegExp = ".*\\{\"message\":\"array index out of range: index: 3, size: 3.*")
    public void invalidIndexReferenceJSONArray() {
        BRunUtil.invoke(compileResult, "invalidIndexReferenceJSONArray");
    }

    @Test
    public void accessValidIndexOfMatchedUnsealedArray() {
        BArray bIntArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_INT));
        bIntArray.add(0, 1);
        bIntArray.add(0, 3);
        bIntArray.add(0, 5);
        Object[] args = {bIntArray, (5)};
        Object returnValues = BRunUtil.invoke(compileResult, "accessIndexOfMatchedSealedArray", args);
        Assert.assertEquals(returnValues, 10L, "Invalid match for sealed array");
    }

    @Test
    public void testSealedArrayConstrainedMap() {
        BArray bIntArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_INT, 3));
        bIntArray.add(0, 1);
        bIntArray.add(1, 3);
        bIntArray.add(2, 5);
        BArray bIntArray2 = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_INT));
        bIntArray2.add(0, 1);
        bIntArray2.add(1, 3);
        bIntArray2.add(2, 5);
        Object[] args = {bIntArray, bIntArray2};
        Object returnValues = BRunUtil.invoke(compileResult, "testSealedArrayConstrainedMap", args);
        Assert.assertEquals(returnValues, 5L, "Invalid match for sealed array");
    }

    @Test(description = "Test accessing invalid index of sealed array of constrained map",
            expectedExceptions = {BLangTestException.class},
            expectedExceptionsMessageRegExp = ".*error:.*array index out of range: index: 3, size: 3.*")
    public void testSealedArrayConstrainedMapInvalidIndex() {
        BArray bIntArray = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_INT, 3));
        bIntArray.add(0, 1);
        bIntArray.add(1, 3);
        bIntArray.add(2, 5);
        Object[] args = {bIntArray, (3)};
        BRunUtil.invoke(compileResult, "testSealedArrayConstrainedMapInvalidIndex", args);
    }

    @Test()
    public void testArrayWithConstantSizeReferenceFill() {
        BRunUtil.invoke(compileResult, "testArrayWithConstantSizeReferenceFill");
    }

    @Test
    public void testCreateXMLSealedArray() {
        BRunUtil.invoke(compileResult, "createXMLAutoFilledSealedArray");
    }

    @Test
    public void createConstLiteralAutoFilledSealedArray() {
        BRunUtil.invoke(compileResult, "createConstLiteralAutoFilledSealedArray");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        resultNegative = null;
        semanticsNegative = null;
        listExprNegative = null;
    }
}
