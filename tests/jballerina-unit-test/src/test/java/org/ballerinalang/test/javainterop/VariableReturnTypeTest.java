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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for interop functions with variable return types through typedesc refs.
 *
 * @since 2.0.0
 */
public class VariableReturnTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/variable_return_type_test.bal");
    }

    @Test
    public void testNegatives() {
        CompileResult errors = BCompileUtil.compile("test-src/javainterop/variable_return_type_negative.bal");
        int indx = 0;
        validateError(errors, indx++, "incompatible types: expected 'string', found 'int'", 27, 16);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'float'", 29, 13);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'decimal'", 30, 9);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'string'", 31, 9);
        validateError(errors, indx++, "incompatible types: expected 'int', found 'boolean'", 32, 9);
        validateError(errors, indx++, "incompatible types: expected 'boolean', found 'int'", 34, 17);
        validateError(errors, indx++, "incompatible types: expected 'float', found 'int'", 36, 15);
        validateError(errors, indx++, "incompatible types: expected 'typedesc<(int|float|decimal|string|boolean)>', "
                          + "found 'typedesc<json>'", 40, 23);
        validateError(errors, indx++, "unknown type 'aTypeVar'", 43, 60);
        validateError(errors, indx++, "incompatible types: expected 'map<int>', found 'map<other>'", 50, 18);
        validateError(errors, indx++, "incompatible types: expected 'int', found '(int|float)'", 60, 13);
        validateError(errors, indx++, "incompatible types: expected 'float', found '(int|float)'", 61, 15);
        validateError(errors, indx++, "unknown type 'td'", 64, 73);
        validateError(errors, indx++, "unknown type 'td'", 72, 54);
        validateError(errors, indx++, "invalid error detail type 'detail', expected a subtype of " +
                "'map<(anydata|readonly)>'", 81, 83);
        validateError(errors, indx++, "unknown type 'detail'", 81, 83);
        validateError(errors, indx++,
                      "a function with a non-'external' function body cannot be a dependently-typed function", 88, 45);
        validateError(errors, indx++,
                      "a function with a non-'external' function body cannot be a dependently-typed function", 88, 67);
        validateError(errors, indx++, "default value for a 'typedesc' parameter used in the return type" +
                " should be a reference to a type", 92, 29);
        validateError(errors, indx++, "unknown type 'NonExistentParam'", 102, 77);
        validateError(errors, indx++, "invalid parameter reference: expected 'typedesc', found 'string'", 108, 54);
        validateError(errors, indx++,
                      "a function with a non-'external' function body cannot be a dependently-typed function", 114, 45);
        validateError(errors, indx++, "invalid parameter reference: expected 'typedesc', found 'string'", 114, 45);
        validateError(errors, indx++, "incompatible types: expected 'function (typedesc<(string|int)>) returns " +
                "(string)', found 'function (typedesc<(int|string)>) returns (aTypeVar)'", 125, 61);
        validateError(errors, indx++, "unknown type 'td'", 126, 48);
        validateError(errors, indx++, "incompatible types: expected 'function (typedesc<(string|int)>) returns " +
                "(other)', found 'function (typedesc<(int|string)>) returns (aTypeVar)'", 126, 57);
        validateError(errors, indx++, "mismatched function signatures: expected 'public function get" +
                "(typedesc<anydata> td) returns (td|error)', found 'public function get(typedesc<anydata> td) returns" +
                " (other|error)'", 139, 5);
        validateError(errors, indx++, "a function with a non-'external' function body cannot be a dependently-typed " +
                "function", 139, 64);
        validateError(errors, indx++, "mismatched function signatures: expected 'public function get" +
                "(typedesc<anydata> td) returns (td|error)', found 'public function get(typedesc<anydata> td) returns" +
                " (other|error)'", 143, 5);
        validateError(errors, indx++, "a function with a non-'external' function body cannot be a dependently-typed " +
                "function", 143, 64);
        validateError(errors, indx++, "incompatible types: expected 'Bar', found 'Baz'", 175, 15);
        validateError(errors, indx++, "incompatible types: expected 'Quux', found 'Qux'", 179, 17);
        validateError(errors, indx++, "incompatible types: expected 'Qux', found 'Quux'", 180, 15);
        validateError(errors, indx++, "incompatible types: expected 'Baz', found 'Quux'", 181, 16);
        validateError(errors, indx++, "incompatible types: expected 'Quuz', found 'Qux'", 182, 17);
        validateError(errors, indx++, "incompatible types: expected 'Corge', found 'Grault'", 184, 19);
        validateError(errors, indx++, "incompatible types: expected 'Grault', found 'Corge'", 185, 21);

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
//                {"testXML"},
                {"testStream"},
                {"testTable"},
                {"testFunctionPointers"},
                {"testTypedesc"},
                {"testFuture"},
                {"testComplexTypes"},
                {"testObjectExternFunctions"},
                {"testDependentlyTypedMethodsWithObjectTypeInclusion"},
                {"testSubtypingWithDependentlyTypedMethods"}
        };
    }
}
