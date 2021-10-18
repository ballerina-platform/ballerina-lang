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

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for dependently-typed interop functions.
 *
 * @since 2.0.0
 */
public class DependentlyTypedFunctionsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/dependently_typed_functions_test.bal");
    }

    @Test
    public void testNegatives() {
        CompileResult errors =
                BCompileUtil.compile("test-src/javainterop/dependently_typed_functions_test_negative.bal");
        int indx = 0;
        validateError(errors, indx++, "incompatible types: expected 'string', found 'int'", 28, 16);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'float'", 30, 13);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'decimal'", 31, 9);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 32, 9);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'boolean'", 33, 9);
        validateError(errors, indx++, "incompatible types: expected 'boolean', found 'int'", 35, 17);
        validateError(errors, indx++, "incompatible types: expected 'float', found 'int'", 37, 15);
        validateError(errors, indx++, "incompatible types: expected 'typedesc<(int|float|decimal|string|boolean)>', "
                          + "found 'typedesc<json>'", 41, 23);
        validateError(errors, indx++, "unknown type 'aTypeVar'", 44, 60);
        validateError(errors, indx++, "incompatible types: expected 'map<int>', found 'map<other>'", 51, 18);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'customType'", 61, 13);
        validateError(errors, indx++, "incompatible types: expected 'float', found 'customType'", 62, 15);
        validateError(errors, indx++, "unknown type 'td'", 65, 73);
        validateError(errors, indx++, "unknown type 'td'", 73, 54);
        validateError(errors, indx++, "invalid error detail type 'detail', expected a subtype of " +
                "'map<ballerina/lang.value:0.0.0:Cloneable>'", 82, 83);
        validateError(errors, indx++, "unknown type 'detail'", 82, 83);
        validateError(errors, indx++,
                      "a function with a non-'external' function body cannot be a dependently-typed function", 89, 45);
        validateError(errors, indx++,
                      "a function with a non-'external' function body cannot be a dependently-typed function", 89, 67);
        validateError(errors, indx++, "default value for a 'typedesc' parameter used in the return type" +
                " should be a reference to a type", 93, 29);
        validateError(errors, indx++, "unknown type 'NonExistentParam'", 103, 77);
        validateError(errors, indx++, "invalid parameter reference: expected 'typedesc', found 'string'", 109, 54);
        validateError(errors, indx++,
                      "a function with a non-'external' function body cannot be a dependently-typed function", 115, 45);
        validateError(errors, indx++, "invalid parameter reference: expected 'typedesc', found 'string'", 115, 45);
        validateError(errors, indx++, "mismatched function signatures: expected 'public function get" +
                "(typedesc<anydata> td) returns (td|error)', found 'public function get(typedesc<anydata> td) returns" +
                " (other|error)'", 140, 5);
        validateError(errors, indx++, "a function with a non-'external' function body cannot be a dependently-typed " +
                "function", 140, 64);
        validateError(errors, indx++, "mismatched function signatures: expected 'public function get" +
                "(typedesc<anydata> td) returns (td|error)', found 'public function get(typedesc<anydata> td) returns" +
                " (other|error)'", 144, 5);
        validateError(errors, indx++, "a function with a non-'external' function body cannot be a dependently-typed " +
                "function", 144, 64);
        validateError(errors, indx++, "incompatible types: expected 'Bar', found 'Baz'", 176, 15);
        validateError(errors, indx++, "incompatible types: expected 'Quux', found 'Qux'", 180, 17);
        validateError(errors, indx++, "incompatible types: expected 'Qux', found 'Quux'", 181, 15);
        validateError(errors, indx++, "incompatible types: expected 'Baz', found 'Quux'", 182, 16);
        validateError(errors, indx++, "incompatible types: expected 'Quuz', found 'Qux'", 183, 17);
        validateError(errors, indx++, "incompatible types: expected 'Corge', found 'Grault'", 185, 19);
        validateError(errors, indx++, "incompatible types: expected 'Grault', found 'Corge'", 186, 21);
        validateError(errors, indx++, "incompatible types: expected 'string', found 'int'", 196, 16);
        validateError(errors, indx++, "incompatible types: expected 'string', found 'int'", 197, 16);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 198, 13);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 199, 13);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 200, 13);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 201, 13);
        validateError(errors, indx++, "incompatible types: expected 'future<int>', found 'future<(IntOrString|error)>'",
                      209, 21);
        validateError(errors, indx++,
                      "incompatible types: expected '(int|string|error)', found 'future<(IntOrString|error)>'",
                      210, 26);
        validateError(errors, indx++,
                      "incompatible types: expected 'future<(int|string)>', found 'future<(IntOrString|error)>'",
                      211, 28);
        validateError(errors, indx++, "incompatible types: expected 'future<int>', found 'future<(int|error)>'",
                      213, 21);
        validateError(errors, indx++, "incompatible types: expected 'string', found 'future<(string|error)>'",
                      214, 16);
        validateError(errors, indx++,
                      "incompatible types: expected 'future<(string|error)>', found 'future<(int|error)>'",
                      215, 30);
        validateError(errors, indx++, "incompatible types: expected '(int|error)', found '(string|error)'", 246, 19);
        validateError(errors, indx++, "incompatible types: expected '(string|error)', found '(int|error)'", 249, 22);
        validateError(errors, indx++, "incompatible types: expected '(string|boolean)', found '(j|boolean)'", 252, 24);
        validateError(errors, indx++, "incompatible types: expected '([int,typedesc<(int|string)>,int...]|record {| " +
                "int i; typedesc<(int|string)> j; |})', found '[int]'", 252, 44);
        validateError(errors, indx++, "missing required parameter 'j' in call to 'getWithRestParam()'", 254, 24);
        validateError(errors, indx++, "incompatible types: expected '(int|boolean)', found '(string|boolean)'", 257,
                      21);
        validateError(errors, indx++, "incompatible types: expected '(string|boolean)', found '(int|boolean)'", 260,
                      24);
        validateError(errors, indx++, "incompatible types: expected '(string|boolean)', found '(j|k|boolean)'", 263,
                      24);
        validateError(errors, indx++, "incompatible types: expected '([typedesc<(int|string)>,typedesc<int>," +
                "typedesc<int>...]|record {| typedesc<(int|string)> j; typedesc<int> k; |})', found " +
                "'[typedesc<string>]'", 263, 55);
        validateError(errors, indx++, "incompatible types: expected '(int|error)', found '(string|error)'", 268, 19);
        validateError(errors, indx++, "incompatible types: expected '(int|error)', found '(y|error)'", 271, 19);
        validateError(errors, indx++, "incompatible types: expected '([typedesc<(int|string)>]|record {| typedesc<" +
                "(int|string)> y; |})', found 'typedesc<int>[]'", 271, 39);
        validateError(errors, indx++, "incompatible types: expected '([(int|string),typedesc<(int|string)>]|record {|" +
                " (int|string) x; typedesc<(int|string)> y; |})', found 'int[1]'", 274, 29);
        validateError(errors, indx++, "incompatible types: expected '(string|boolean)', " +
                "found '(string|int|boolean)'", 277, 24);
        validateError(errors, indx++, "incompatible types: expected '(byte|boolean)', found '(j|k|boolean)'", 280, 22);
        validateError(errors, indx++, "incompatible types: expected '([typedesc<(int|string)>,typedesc<int>," +
                "typedesc<int>...]|record {| typedesc<(int|string)> j; typedesc<int> k; |})', found " +
                "'typedesc<byte>[1]'", 280, 53);
        validateError(errors, indx++, "incompatible types: expected '(string|error)', found '(int|error)'", 301, 22);
        validateError(errors, indx++, "incompatible types: expected '(int|error)', found '(string|error)'", 304, 19);
        validateError(errors, indx++, "incompatible types: expected '(int|boolean)', found '(string|boolean)'", 307,
                      21);
        validateError(errors, indx++, "incompatible types: expected '(string|boolean)', found '(j|boolean)'", 310, 24);
        validateError(errors, indx++, "incompatible types: expected '([typedesc<(int|string)>,int...]|record {| " +
                "typedesc<(int|string)> j; |})', found 'record {| |} & readonly'", 310, 47);
        validateError(errors, indx++, "incompatible types: expected '(string|boolean)', found '(string|int|boolean)'",
                      313, 24);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 338, 14);
        validateError(errors, indx++, "incompatible types: expected 'string', found 'int'", 339, 17);
        validateError(errors, indx++, "incompatible types: expected 'string', found 'int'", 340, 17);
        validateError(errors, indx++, "incompatible types: expected 'string', found 'int'", 341, 17);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 342, 14);
        validateError(errors, indx++, "undefined defaultable parameter 'targetTypes'", 343, 74);
        validateError(errors, indx++, "incompatible types: expected 'int', found '(string|error)'", 345, 14);
        validateError(errors, indx++, "incompatible types: expected 'string', found '(int|error)'", 346, 17);
        validateError(errors, indx++, "incompatible types: expected 'string', found '(int|error)'", 347, 17);
        validateError(errors, indx++, "incompatible types: expected 'string', found '(int|error)'", 348, 18);
        validateError(errors, indx++, "incompatible types: expected 'string', found '(string|error)'", 349, 18);
        validateError(errors, indx++, "missing required parameter 'i' in call to 'calculate()'", 350, 15);
        validateError(errors, indx++, "incompatible types: expected 'string', found '(string|error)'", 351, 18);
        validateError(errors, indx++, "incompatible types: expected 'int', found '(int|error)'", 352, 15);
        validateError(errors, indx++, "incompatible types: expected 'string', found '(string|error)'", 353, 18);
        validateError(errors, indx++, "incompatible types: expected 'string', found '(string|error)'", 354, 18);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 357, 15);
        validateError(errors, indx++, "missing required parameter 'targetType' in call to 'post()'", 358, 18);
        validateError(errors, indx++, "missing required parameter 'targetType' in call to 'post()'", 359, 18);
        validateError(errors, indx++, "incompatible types: expected 'string', found 'int'", 360, 18);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 361, 15);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 362, 15);
        validateError(errors, indx++, "incompatible types: expected 'string', found 'int'", 363, 18);
        Assert.assertEquals(errors.getErrorCount(), indx);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible types:" +
                  " 'map' cannot be cast to 'map<anydata>'.*")
    public void testRuntimeCastError() {
        BRunUtil.invoke(result, "testRuntimeCastError");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible types:" +
                  " 'Person' cannot be cast to 'int'.*")
    public void testCastingForInvalidValues() {
        BRunUtil.invoke(result, "testCastingForInvalidValues");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible types:" +
                  " 'string' cannot be cast to 'int'.*")
    public void testFunctionAssignment() {
        BRunUtil.invoke(result, "testFunctionAssignment");
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
                {"testVarRefUseInMultiplePlaces"},
                {"testSimpleTypes"},
                {"testUnionTypes"},
                {"testArrayTypes"},
                {"testXML"},
                {"testStream"},
                {"testTable"},
                {"testFunctionPointers"},
                {"testTypedesc"},
                {"testFuture"},
                {"testComplexTypes"},
                {"testObjectExternFunctions"},
                {"testDependentlyTypedMethodsWithObjectTypeInclusion"},
                {"testSubtypingWithDependentlyTypedMethods"},
                {"testDependentlyTypedFunctionWithDefaultableParams"},
                {"testStartActionWithDependentlyTypedFunctions"},
                {"testArgsForDependentlyTypedFunctionViaTupleRestArg"},
                {"testArgsForDependentlyTypedFunctionViaArrayRestArg"},
                {"testArgsForDependentlyTypedFunctionViaRecordRestArg"},
                {"testDependentlyTypedFunctionWithIncludedRecordParam"}
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
