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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for array mutability.
 */
public class ArrayMutabilityTest {

    private CompileResult compileResult;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-mutability.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/arrays/array-mutability-negative.bal");
    }

    @Test
    public void testValidArrayAssignment() {
        BArray returnValues = (BArray) BRunUtil.invoke(compileResult, "testValidArrayAssignment");
        Assert.assertTrue((Boolean) returnValues.get(0), "Expected value of true but found false");
        Assert.assertEquals(returnValues.get(1), 100L, "Expected value of 100");
    }

    @Test(description = "Check if correct type is saved in covariant array with record type",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error:.*incompatible types: 'Employee' cannot be cast to 'Intern'.*")
    public void testInvalidCast() {
        BRunUtil.invoke(compileResult, "testInvalidCast");
    }

    @Test(description = "Test mutation of record type using covariant array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error: \\{ballerina/lang.array\\}InherentTypeViolation " +
                            "\\{\"message\":\"incompatible types: expected 'Employee', found 'Person'.*")
    public void testAssignmentOfSuperTypeMember() {
        BRunUtil.invoke(compileResult, "testAssignmentOfSuperTypeMember");
    }

    @Test(description = "Test mutation of record type by assigning invalid record type",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error:.*incompatible types: expected 'Employee', found 'Student'.*")
    public void testInvalidAssignment() {
        BRunUtil.invoke(compileResult, "testInvalidAssignment");
    }

    @Test(description = "Test mutation of int array by inserting nil value to int? covariant array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error:.*incompatible types: expected 'int', found '\\(\\)'.*")
    public void testCovarianceIntOrNilArray() {
        BRunUtil.invoke(compileResult, "testCovarianceIntOrNilArray");
    }

    @Test(description = "Test mutation of arrays which include structural and simple values",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation " +
                            "\\{\"message\":\"incompatible types: expected " +
                            "'\\(boolean\\|float\\)\\?', found 'Person'.*?")
    public void testCovarianceBooleanOrFloatOrRecordArray() {
        BRunUtil.invoke(compileResult, "testCovarianceBooleanOrFloatOrRecordArray");
    }

    @Test(description = "Test mutation of sealed arrays",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error:.*array index out of range: index: 3, size: 3.*")
    public void testSealedArrays() {
        BRunUtil.invoke(compileResult, "testSealedArrays");
    }

    @Test(description = "Test mutation of multidimensional sealed arrays",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error:.*array index out of range: index: 3, size: 3.*")
    public void testMultiDimensionalSealedArrays() {
        BRunUtil.invoke(compileResult, "testMultiDimensionalSealedArrays");
    }

    @Test(description = "Test mutation of openly sealed arrays",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error:.*array index out of range: index: 4, size: 4.*")
    public void testOpenSealedArrays() {
        BRunUtil.invoke(compileResult, "testOpenSealedArrays");
    }

    @Test(description = "Test mutation of object type",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error:.*incompatible types: expected 'Dog', found 'Animal'.*")
    public void testObjectTypes() {
        BRunUtil.invoke(compileResult, "testObjectTypes");
    }

    @Test
    public void testUnionOfArrays() {
        Object returnValues = BRunUtil.invoke(compileResult, "testUnionOfArrays");
        Assert.assertEquals((returnValues).toString(), "BOOL");
    }

    @Test(description = "Test mutation of boolean array assigned to a union",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error:.*incompatible types: expected 'boolean', found 'int'.*")
    public void testUnionOfArrays2() {
        BRunUtil.invoke(compileResult, "testUnionOfArrays2");
    }

    @Test(description = "Test mutation of int array assigned to a json array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*\\{ballerina/lang.array\\}InherentTypeViolation " +
                            "\\{\"message\":\"incompatible types: expected 'int', " +
                            "found 'string'.*")
    public void testJsonArrayMutability() {
        BRunUtil.invoke(compileResult, "testJsonArrayMutability");
    }

    @Test(description = "Test mutation of boolean array assigned to a json array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*\\{ballerina/lang.array\\}InherentTypeViolation " +
                            "\\{\"message\":\"incompatible types: expected " +
                            "'boolean', found 'string'.*")
    public void testJsonArrayMutability2() {
        BRunUtil.invoke(compileResult, "testJsonArrayMutability2");
    }

    @Test(description = "Test mutation of arrays through chained assignments",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation " +
                            "\\{\"message\":\"incompatible types: expected " +
                            "\\'\\(int\\|string\\)\\?\\', found \\'boolean\\'.*")
    public void testChainingAssignment() {
        BRunUtil.invoke(compileResult, "testChainingAssignment");
    }

    @Test(description = "Test negative scenarios of assigning tuple literals")
    public void testNegativeTupleLiteralAssignments() {
        int i = 0;
        Assert.assertEquals(resultNegative.getErrorCount(), 8);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Employee[]', found 'Person[]'", 34, 21);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Employee[][]', found 'Person[][]'", 40, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Person[]', found 'Employee[][]'", 43, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Person[][][]', found 'Employee[][]'", 44, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int[3]', found 'int[]'", 51, 18);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int[3][3]', found 'int[3][]'", 54, 21);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Animal[]', found 'Cat[]'", 80, 28);
        BAssertUtil.validateError(resultNegative, i,
                "incompatible types: expected '(int[]|boolean[])', found '(int|boolean)?[]'", 90, 10);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        resultNegative = null;
    }
}
