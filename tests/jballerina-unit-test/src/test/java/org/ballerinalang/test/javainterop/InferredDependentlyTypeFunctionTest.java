/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.test.javainterop;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for dependently typed function signatures derived from contextual type.
 *
 * @since 2.0.0
 */
public class InferredDependentlyTypeFunctionTest {

    private CompileResult result;
    private static final String INVALID_RETURN_TYPE_ERROR = "invalid return type: members of a dependently-typed " +
            "union type with an inferred typedesc parameter should have disjoint basic types";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/inferred_dependently_typed_func_signature.bal");
    }

    @Test(dataProvider = "FunctionNames")
    public void testVariableTypeAsReturnType(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "FunctionNames")
    public Object[][] getFuncNames() {
        return new Object[][]{
                {"testRecordVarRef"},
                {"testVarRefInMapConstraint"},
                {"testTupleTypes"},
                {"testSimpleTypes"},
                {"testArrayTypes"},
                {"testStream"},
                {"testTable"},
                {"testFunctionPointers"},
                {"testTypedesc"},
                {"testFuture"},
                {"testComplexTypes"},
                {"testRuntimeCastError"},
                {"testFunctionAssignment"},
                {"testCastingForInvalidValues"},
                {"testUnionTypes"},
                {"testXml"},
                {"testArgCombinations"},
                {"testBuiltInRefType"},
                {"testParameterizedTypeInUnionWithNonParameterizedTypes"},
                {"testUsageWithVarWithUserSpecifiedArg"},
                {"testFunctionWithAnyFunctionParamType"},
                {"testUsageWithCasts"}
        };
    }

    @Test
    public void testDependentlyTypedFunctionWithInferredTypedescValueNegative() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/javainterop/inferred_dependently_typed_func_signature_negative.bal");
        int index = 0;
        validateError(negativeResult, index++, "incompatible type for parameter 'rowType' with inferred " +
                "typedesc value: expected 'typedesc<record {| int...; |}>', found 'typedesc<OpenRecord>'", 20, 38);
        validateError(negativeResult, index++, "incompatible types: expected 'typedesc<record {| int...; |}>', found " +
                "'typedesc<OpenRecord>'", 21, 49);
        validateError(negativeResult, index++, "cannot have more than one defaultable parameter with an inferred " +
                "typedesc default", 28, 1);
        validateError(negativeResult, index++, "cannot have more than one defaultable parameter with an inferred " +
                "typedesc default", 32, 5);
        validateError(negativeResult, index++, "incompatible types: expected 'stream<record {| int x; |},error?>', " +
                "found 'stream<OpenRecord,error?>'", 40, 44);
        validateError(negativeResult, index++, INVALID_RETURN_TYPE_ERROR, 42, 51);
        validateError(negativeResult, index++, INVALID_RETURN_TYPE_ERROR, 44, 46);
        validateError(negativeResult, index++, INVALID_RETURN_TYPE_ERROR, 46, 52);
        validateError(negativeResult, index++, INVALID_RETURN_TYPE_ERROR, 48, 65);
        validateError(negativeResult, index++, INVALID_RETURN_TYPE_ERROR, 50, 64);
        validateError(negativeResult, index++, INVALID_RETURN_TYPE_ERROR, 52, 53);
        validateError(negativeResult, index++, INVALID_RETURN_TYPE_ERROR, 54, 66);
        validateError(negativeResult, index++, INVALID_RETURN_TYPE_ERROR, 56, 49);
        validateError(negativeResult, index++,
                "incompatible types: expected 'xml:Comment', found 'xml<xml:Comment>'", 65, 21);
        validateError(negativeResult, index++,
                "incompatible types: expected 'xml<xml:Element>', found 'xml<xml:Comment>'", 66, 26);
        validateError(negativeResult, index++,
                "incompatible types: expected 'xml<xml:Text>', found 'xml<xml:Element>'", 67, 23);
        validateError(negativeResult, index++,
                "incompatible types: expected 'xml<xml:Comment>', found 'xml<xml:Element>'", 68, 26);
        validateError(negativeResult, index++,
                "incompatible types: expected 'typedesc<(xml:Element|xml:Comment)>', found 'typedesc<xml:Text>'",
                69, 38);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td'", 78, 18);
        validateError(negativeResult, index++, "incompatible type for parameter 'td' with inferred typedesc value: " +
                "expected 'typedesc<(string|int)>', found 'typedesc<float>'", 79, 26);
        validateError(negativeResult, index++, "incompatible type for parameter 'td' with inferred typedesc value: " +
                "expected 'typedesc<(int|[int,string])>', found 'typedesc<float>'", 80, 31);
        validateError(negativeResult, index++, "incompatible type for parameter 'td' with inferred typedesc value: " +
                "expected 'typedesc<(int|[int,string])>', found 'typedesc<[int,boolean]>'", 81, 50);
        validateError(negativeResult, index++, "incompatible types: expected 'boolean?', found '(int|boolean)?'", 82,
                18);
        validateError(negativeResult, index++, "incompatible types: expected 'typedesc<(string|int)>', found " +
                "'typedesc<float>'", 83, 46);
        validateError(negativeResult, index++, "incompatible types: expected '(float|boolean)?', found '" +
                "(string|boolean)?'", 84, 24);
        validateError(negativeResult, index++, "incompatible types: expected '(string|boolean)', found '" +
                "(string|boolean)?'", 85, 24);
        validateError(negativeResult, index++, "incompatible types: expected '(int[]|map<float[]>)?', found '" +
                "(int[]|map<int[]>)?'", 86, 29);
        validateError(negativeResult, index++, "incompatible type for parameter 'td' with inferred typedesc value: " +
                "expected 'typedesc<(int|[int,string])>', found 'typedesc<[int,int]>'", 87, 43);
        validateError(negativeResult, index++, "incompatible types: expected 'string[]', found 'int[]'", 95, 18);
        validateError(negativeResult, index++, "incompatible types: expected 'int[]', found 'string[]'", 96, 15);
        validateError(negativeResult, index++, "incompatible types: expected 'string[]', found 'int[]'", 99, 18);
        validateError(negativeResult, index++, "incompatible types: expected 'string[]', found 'int[]'", 102, 18);
        validateError(negativeResult, index++, "incompatible types: expected 'int[]', found 'string[]'", 105, 15);
        validateError(negativeResult, index++, "incompatible types: expected 'int[]', found 'string[]'", 108, 15);
        validateError(negativeResult, index++, "incompatible type for parameter 'td' with inferred typedesc value: " +
                "expected 'typedesc<(int|string)>', found 'typedesc<boolean>'", 110, 19);
        validateError(negativeResult, index++, "incompatible type for parameter 'td' with inferred typedesc value: " +
                "expected 'typedesc<(int|string)>', found 'typedesc<float>'", 112, 17);
        validateError(negativeResult, index++, "incompatible type for parameter 'td' with inferred typedesc value: " +
                "expected 'typedesc<(int|string)>', found 'typedesc<json>'", 114, 16);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td'", 120, 15);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td'", 121, 32);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td'", 122, 18);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td2'", 124, 15);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td2'", 125, 32);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td2'", 126, 18);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td2'", 127, 18);
        validateError(negativeResult, index++, "incompatible types: expected '[string]', found '[int,string...]'",
                128, 18);
        validateError(negativeResult, index++, "incompatible type for parameter 'td' with inferred typedesc value: " +
                "expected 'typedesc<stream<int>>', found 'typedesc<stream<string>>'", 143, 40);
        validateError(negativeResult, index++, "incompatible types: expected 'stream<byte>', found '" +
                "(readonly|stream<byte>|handle)'", 144, 22);
        validateError(negativeResult, index++, "incompatible types: expected 'stream<int>?', found 'typedesc<int>'",
                145, 67);
        validateError(negativeResult, index++, "incompatible types: expected '(stream<boolean>|readonly)', found '" +
                "(readonly|stream<int>|handle)'", 146, 34);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td'", 147, 25);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td'", 151, 13);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td'", 152, 13);
        validateError(negativeResult, index++, "incompatible types: expected 'int', found 'typedesc<(any|error)>'",
                155, 55);
        validateError(negativeResult, index++, "incompatible types: expected '(int|typedesc<int>)', found 'typedesc<" +
                "(any|error)>'", 158, 69);
        validateError(negativeResult, index++, "incompatible type for parameter 'td' with inferred typedesc value: " +
                "expected 'typedesc<int>', found 'typedesc<boolean>'", 168, 37);
        validateError(negativeResult, index++, "cannot use an inferred typedesc default with a parameter on which the" +
                " return type does not depend on", 171, 38);
        validateError(negativeResult, index++, "cannot use an inferred typedesc default with a parameter on which the" +
                " return type does not depend on", 172, 37);
        validateError(negativeResult, index++, "cannot use an inferred typedesc default with a parameter on which the" +
                " return type does not depend on", 173, 49);
        validateError(negativeResult, index++, "cannot use an inferred typedesc default with a parameter on which the" +
                " return type does not depend on", 174, 49);
        validateError(negativeResult, index++, "cannot use an inferred typedesc default with a parameter on which the" +
                " return type does not depend on", 176, 34);
        validateError(negativeResult, index++, "cannot use an inferred typedesc default with a parameter on which the" +
                " return type does not depend on", 177, 34);
        validateError(negativeResult, index++, "cannot use an inferred typedesc default with a parameter on which the" +
                " return type does not depend on", 178, 46);
        validateError(negativeResult, index++, "cannot have more than one defaultable parameter with an inferred " +
                "typedesc default", 180, 1);
        validateError(negativeResult, index++, "incompatible types: expected 'function (typedesc<(any|error)>," +
                "typedesc<boolean>) returns ((t|td))', found 'function (typedesc<(any|error)>,typedesc<boolean>) " +
                "returns ((int|string))'", 180, 74);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td'", 185, 44);
        validateError(negativeResult, index++, "cannot infer type for parameter 'td'", 186, 52);
        Assert.assertEquals(index, negativeResult.getErrorCount());
    }
}
