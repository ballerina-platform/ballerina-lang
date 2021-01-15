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
        validateError(errors, indx++, "incompatible types: expected 'int', found '(int|float)'", 61, 13);
        validateError(errors, indx++, "incompatible types: expected 'float', found '(int|float)'", 62, 15);
        validateError(errors, indx++, "unknown type 'td'", 65, 73);
        validateError(errors, indx++, "unknown type 'td'", 73, 54);
        validateError(errors, indx++, "invalid error detail type 'detail', expected a subtype of " +
                "'map<Cloneable>'", 82, 83);
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
        validateError(errors, indx++, "unknown type 'td'", 127, 48);
        validateError(errors, indx++, "incompatible types: expected 'function (typedesc<(string|int)>) returns " +
                "(other)', found 'function (typedesc<(int|string)>) returns (aTypeVar)'", 127, 57);
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

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
