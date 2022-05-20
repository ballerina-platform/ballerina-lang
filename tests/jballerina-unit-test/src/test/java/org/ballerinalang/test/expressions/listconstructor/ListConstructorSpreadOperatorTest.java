/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.listconstructor;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for list constructor spread operator.
 *
 * @since 2201.1.0
 */
public class ListConstructorSpreadOperatorTest {

    private CompileResult result, inferenceResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/listconstructor/list_constructor_spread_op.bal");
        inferenceResult = BCompileUtil
                .compile("test-src/expressions/listconstructor/list_constructor_spread_op_inference.bal");
    }

    @Test
    public void testSpreadOpCompatibilityBetweenLists() {
        BRunUtil.invoke(result, "testArrayArrayCompatibility");
        BRunUtil.invoke(result, "testTupleTupleCompatibility");
        BRunUtil.invoke(result, "testTupleArrayCompatibility");
        BRunUtil.invoke(result, "testArrayTupleCompatibility");
    }

    @Test
    public void testSpreadOpWithFillerValues() {
        BRunUtil.invoke(result, "testFillerValue1");
        BRunUtil.invoke(result, "testFillerValue2");
    }

    @Test
    public void testSpreadOpWithVaryingLengthRef() {
        BRunUtil.invoke(result, "testSpreadOpWithVaryingLengthRef");
    }

    @Test
    public void testSpreadOpWithListConstructorTypeBeingTypeRef() {
        BRunUtil.invoke(result, "testSpreadOpWithListConstructorTypeBeingTypeRef");
    }

    @Test
    public void testSpreadOpWithExprNotBeingAReference() {
        BRunUtil.invoke(result, "testSpreadOpWithExprNotBeingAReference");
    }

    @Test
    public void testSpreadOpTupleWithNeverRestDescriptor() {
        BRunUtil.invoke(result, "testSpreadOpTupleWithNeverRestDescriptor");
    }

    @Test
    public void testSpreadOpAtModuleLevel() {
        BRunUtil.invoke(result, "testSpreadOpAtModuleLevel");
    }

    @Test
    public void testSpreadOpInference() {
        BRunUtil.invoke(inferenceResult, "testSpreadOpInferenceWithVar");
        BRunUtil.invoke(inferenceResult, "testSpreadOpOnVariableLengthListsWithVar");
        BRunUtil.invoke(inferenceResult, "testSpreadOpInferenceWithReadonly");
        BRunUtil.invoke(inferenceResult, "testSpreadOpOnVariableLengthListsWithReadonly");
        BRunUtil.invoke(inferenceResult, "testSpreadOpWithTypedesc");
        BRunUtil.invoke(inferenceResult, "testSpreadOpOnVariableLengthListsWithTypedesc");
        BRunUtil.invoke(inferenceResult, "testInferenceViaSpreadOpWithTypeRef");
        BRunUtil.invoke(inferenceResult, "testSpreadOpInferenceWithNeverRestDescriptor");
    }

    @Test
    public void testSpreadOperatorNegative() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/expressions/listconstructor/list_constructor_spread_op_negative.bal");
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'other'", 18, 19);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a1'", 18, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'other'", 19, 25);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a2'", 19, 25);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'other'", 20, 25);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a3'", 20, 25);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'other'", 21, 26);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a4'", 21, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'other'", 22, 26);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a5'", 22, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'int'", 27, 25);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'string'", 30, 25);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found '(int|string)'", 33, 19);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 38, 25);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 39, 25);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 40, 26);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 41, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found '(int|string)'", 45, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 49, 29);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'int'", 54, 40);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'int'", 55, 43);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 58, 35);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 61, 39);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 62, 41);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '\"s\"', found 'string'", 68, 32);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 69, 40);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(int|boolean)', found 'string'", 70, 45);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(int|string)', found '(int|boolean)'", 71, 44);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found '(string|int)'", 75, 32);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(int|string)', found 'any'", 79, 36);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 84, 35);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found '(int|string)'", 85, 22);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found '(int|string)'", 88, 35);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'string'", 91, 38);
        BAssertUtil.validateError(resultNegative, i++, "tuple and expression size does not match", 92, 26);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 97, 32);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 98, 40);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 99, 45);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed member expected for 'int'", 100, 44);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found '(int|string)'", 104, 35);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed member expected for 'string'", 108, 46);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'string'", 113, 22);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'string'", 114, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(int|string)', found 'any'", 117, 28);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '\"x\"|\"y\"', found '(int|string)'", 120, 30);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '\"x\"|\"y\"', found '(int|string)'", 121, 25);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '\"x\"|\"y\"', found '(int|string)'", 122, 25);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '\"x\"|\"y\"', found '(int|string)'", 123, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '\"x\"|\"y\"', found '(int|string)'", 124, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(string|boolean)', found '(int|string)'", 125, 37);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 128, 32);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '(int|string)', found '(int|boolean)'", 131, 31);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 138, 22);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 139, 27);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 140, 27);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 141, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 142, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed member expected for 'string'", 147, 41);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed member expected for 'any'", 150, 44);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed member expected for '(int|string)'", 153, 36);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed member expected for 'boolean'", 156, 47);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of list constructor: type '(string|int)' does not have a filler value", 161, 43);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed member expected for 'int'", 166, 27);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed member expected for 'string'", 170, 41);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed member expected for 'string'", 173, 38);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'string'", 189, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 192, 24);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 194, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of spread operator: fixed length list expected", 197, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'Qux'", 200, 26);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @Test
    public void testSpreadOpInferenceNegative() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/expressions/listconstructor/list_constructor_spread_op_inference_negative.bal");
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 18, 17);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a'", 18, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'int'", 21, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'int'", 22, 23);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found '[int,int]'", 28, 13);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found '[int,string,int,int,boolean]'", 29, 13);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found '[string,(int|boolean)]'", 35, 13);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found '[int,string,string,(int|boolean),boolean]'", 36, 13);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 40, 23);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a'", 40, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'int'", 43, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'int'", 44, 29);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 47, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'readonly', found 'int[]'", 47, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 48, 29);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'readonly', found 'int[]'", 48, 29);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 51, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'readonly', found '[int,int...]'", 51, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 52, 29);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'readonly', found '[int,int...]'", 52, 29);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 55, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'readonly', found 'int[2]'", 55, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 56, 31);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'readonly', found 'int[2]'", 56, 31);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 59, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'readonly', found '[int,string]'", 59, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 60, 32);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'readonly', found '[int,string]'", 60, 32);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 63, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'readonly', found '[string,(int|boolean)]'", 63, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 64, 32);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'readonly', found '[string,(int|boolean)]'", 64, 32);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'readonly'", 74, 13);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'readonly'", 75, 13);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'readonly'", 81, 13);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'readonly'", 82, 13);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'readonly'", 88, 13);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'readonly'", 89, 13);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'readonly'", 95, 13);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'readonly'", 96, 13);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'readonly'", 101, 13);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found '(readonly|int)'", 106, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found '((int|string)[] & readonly)'", 107, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found '[int]'", 117, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean'," +
                        " found '[int,int,string,(int|string),string,boolean]'", 118, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found '[string,int,int,int]'", 132, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found '[string,int,int,int,boolean]'", 133, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found '[anydata,string,int]'", 139, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found '[anydata,string,int]'", 140, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found '[string,int...]'", 144, 17);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'other'", 148, 22);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a'", 148, 22);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot infer type from spread operator: expected an array or a tuple, found 'int'", 151, 35);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @Test
    public void testSpreadOpSelfReferenceNegative() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/expressions/listconstructor/list_constructor_spread_op_self_ref_negative.bal");
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "self referenced variable 'a1'", 18, 20);
        BAssertUtil.validateError(resultNegative, i++, "self referenced variable 'a2'", 19, 23);
        BAssertUtil.validateError(resultNegative, i++, "self referenced variable 'a3'", 20, 21);
        BAssertUtil.validateError(resultNegative, i++, "tuple and expression size does not match", 21, 24);
        BAssertUtil.validateError(resultNegative, i++, "self referenced variable 'a4'", 21, 31);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 22, 39);
        BAssertUtil.validateError(resultNegative, i++, "self referenced variable 'a5'", 22, 39);
        BAssertUtil.validateError(resultNegative, i++,
                "size mismatch in closed array. expected '3', but found '4'", 23, 17);
        BAssertUtil.validateError(resultNegative, i++, "self referenced variable 'a6'", 23, 32);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        inferenceResult = null;
    }
}
