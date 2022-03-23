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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.model.arrays.
 */
public class SealedArraysOfArraysTest {

    private CompileResult compileResult;
    private CompileResult resultNegative;
    private CompileResult codeAnalysisNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil
                .compile("test-src/statements/arrays/arraysofarrays/sealed-arrays-of-arrays.bal");
        resultNegative = BCompileUtil
                .compile("test-src/statements/arrays/arraysofarrays/negative-sealed-arrays-of-arrays.bal");
        codeAnalysisNegative = BCompileUtil.compile("test-src/statements/arrays/arraysofarrays/" +
                        "code_analysis_negative_sealed_arrays_of_arrays.bal");
    }

    @Test
    public void testInitializeTwoDArray() {

        BArray returnValues = (BArray) BRunUtil.invoke(compileResult, "initTwoDimensionalSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.size() == 0 || returnValues.get(0) == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnValues.get(0), 3L, "Value didn't match");
        Assert.assertEquals(returnValues.get(1), 4L, "Value didn't match");
        Assert.assertEquals(returnValues.get(2), 2L, "Value didn't match");
        Assert.assertEquals(returnValues.get(3), 3L, "Value didn't match");
    }

    @Test
    public void testInitializeThreeDArray() {

        BArray returnValues = (BArray) BRunUtil.invoke(compileResult, "initThreeDimensionalSealedArray");
        Assert.assertFalse(
                returnValues == null || returnValues.size() == 0 || returnValues.get(0) == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnValues.get(0), 3L, "Value didn't match");
        Assert.assertEquals(returnValues.get(1), 4L, "Value didn't match");
        Assert.assertEquals(returnValues.get(2), 5L, "Value didn't match");
        Assert.assertEquals(returnValues.get(3), 2L, "Value didn't match");
        Assert.assertEquals(returnValues.get(4), 3L, "Value didn't match");
        Assert.assertEquals(returnValues.get(5), 3L, "Value didn't match");
    }

    @Test
    public void testIntegerSealedArraysOfArrays() {

        BArray arrayValue = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_INT, 2), 2);
        arrayValue.add(0, 10);
        arrayValue.add(1, 12);
        Object[] args = {arrayValue};

        BArray returnValues = (BArray) BRunUtil.invoke(compileResult, "twoDArrayIntAssignment", args);
        Assert.assertFalse(returnValues == null || returnValues.size() == 0 || returnValues.get(0) == null, "Invalid " +
                "Return Values.");
        Assert.assertEquals(returnValues.get(0), 3L, "Value didn't match");
        Assert.assertEquals(returnValues.get(1), 10L, "Value didn't match");
        Assert.assertEquals(returnValues.get(2), 3L, "Value didn't match");
    }

    @Test
    public void testStringSealedArraysOfArrays() {

        BArray arrayValue =
                ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING, 2), 2);
        arrayValue.add(0, "ballerina");
        arrayValue.add(1, "multidimensional");
        Object[] args = {arrayValue};

        BArray returnValues = (BArray) BRunUtil.invoke(compileResult, "twoDArrayStringAssignment", args);
        Assert.assertFalse(
                returnValues == null || returnValues.size() == 0 || returnValues.get(0) == null,
                "Invalid Return Values.");
        Assert.assertEquals((returnValues.get(0)).toString(), "val1", "Value didn't match");
        Assert.assertEquals((returnValues.get(1)).toString(), "ballerina", "Value didn't match");
        Assert.assertEquals((returnValues.get(2)).toString(), "val1", "Value didn't match");
    }

    @Test()
    public void testNegativeSealedArraysOfArrays() {
        Assert.assertEquals(resultNegative.getErrorCount(), 34);
        int i = 0;
        BAssertUtil.validateError(
                resultNegative, i++, "size mismatch in closed array. expected '2', but found '3'", 19, 23);
        BAssertUtil.validateError(
                resultNegative, i++, "size mismatch in closed array. expected '2', but found '3'", 20, 36);
        BAssertUtil.validateError(
                resultNegative, i++, "size mismatch in closed array. expected '2', but found '3'", 21, 24);
        BAssertUtil.validateError(resultNegative, i++, "list index out of range: index: '2'", 27, 21);
        BAssertUtil.validateError(resultNegative, i++, "list index out of range: index: '3'", 37, 8);
        BAssertUtil.validateError(resultNegative, i++, "list index out of range: index: '3'", 38, 11);
        BAssertUtil.validateError(resultNegative, i++, "list index out of range: index: '3'", 39, 14);
        BAssertUtil.validateError(
                resultNegative, i++, "size mismatch in closed array. expected '3', but found '4'", 40, 16);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected 'int[3]', found 'int'", 41, 14);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected 'int[3]', found 'int'", 41, 17);
        BAssertUtil.validateError(
                resultNegative, i++, "size mismatch in closed array. expected '3', but found '4'", 42, 25);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected 'int[3]', found 'int[]'", 47, 21);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected 'int[3]', found 'int[]'", 47, 25);
        BAssertUtil.validateError(
                resultNegative, i++, "list index out of range: index: '4'", 52, 11);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected 'json[2]', found 'json[]'", 60, 12);
        BAssertUtil.validateError(
                resultNegative, i++, "list index out of range: index: '2'", 63, 8);
        BAssertUtil.validateError(
                resultNegative, i++, "list index out of range: index: '4'", 64, 14);
        BAssertUtil.validateError(
                resultNegative, i++, "list index out of range: index: '4'", 68, 14);
        BAssertUtil.validateError(
                resultNegative, i++, "size mismatch in closed array. expected '3', but found '5'", 72, 46);
        BAssertUtil.validateError(
                resultNegative, i++, "size mismatch in closed array. expected '3', but found '4'", 72, 66);
        BAssertUtil.validateError(
                resultNegative, i++, "invalid usage of closed type: array not initialized", 73, 5);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected '((float[*][] & readonly)|string)', " +
                        "found '(float[2][2] & readonly)'", 76, 40);
        BAssertUtil.validateError(
                resultNegative, i++, "list index out of range: index: '4'", 83, 11);
        BAssertUtil.validateError(
                resultNegative, i++, "invalid usage of closed type: can not infer array size", 88, 22);
        BAssertUtil.validateError(
                resultNegative, i++, "invalid usage of closed type: can not infer array size", 91, 33);
        BAssertUtil.validateError(
                resultNegative, i++, "invalid usage of closed type: can not infer array size", 94, 33);
        BAssertUtil.validateError(
                resultNegative, i++, "invalid usage of closed type: can not infer array size", 98, 24);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected 'map<int[*][]>', found 'map<(float|int[1][1])>'",
                102, 23);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected '[(int[*][] & readonly),float]', " +
                        "found '[(string|int[1][]),float]'", 105, 38);
        BAssertUtil.validateError(
                resultNegative, i++, "invalid usage of closed type: can not infer array size", 109, 18);
        BAssertUtil.validateError(
                resultNegative, i++, "invalid usage of closed type: can not infer array size", 112, 29);
        BAssertUtil.validateError(
                resultNegative, i++, "invalid usage of closed type: can not infer array size", 115, 29);
        BAssertUtil.validateError(
                resultNegative, i++, "incompatible types: expected 'map<int[*][]>', found 'map<(float|int[1][1])>'",
                118, 19);
        BAssertUtil.validateError(
                resultNegative, i, "incompatible types: expected '[(int[*][] & readonly),float]', " +
                        "found '[(string|int[1][]),float]'", 121, 34);
    }

    @Test
    public void testCodeAnalysisNegativeSealedArrays() {
        Assert.assertEquals(codeAnalysisNegative.getErrorCount(), 34);
        int i = 0;
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 18, 5);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 19, 5);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 20, 5);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 23, 5);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 24, 5);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 26, 5);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 30, 6);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 32, 6);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 36, 15);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 37, 15);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                37, 36);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                38, 43);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                39, 40);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 47, 16);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                47, 45);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 51, 24);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 55, 24);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                59, 41);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 59, 53);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                63, 14);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                66, 2);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                67, 9);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                68, 2);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                70, 5);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 71, 11);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                72, 5);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                73, 11);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                73, 52);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                73, 80);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 74, 11);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                74, 32);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "inferred array size is only allowed in the first " +
                "dimension of an array type descriptor", 74, 54);
        BAssertUtil.validateError(codeAnalysisNegative, i++, "invalid usage of closed type: can not infer array size",
                74, 83);
        BAssertUtil.validateError(codeAnalysisNegative, i, "invalid usage of closed type: can not infer array size",
                75, 36);
    }
}
