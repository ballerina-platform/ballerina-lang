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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
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
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testValidArrayAssignment");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue(), "Expected value of true but found false");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 100, "Expected value of 100");
    }

    @Test(description = "Check if correct type is saved in covariant array with record type",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error:.*assertion error: expected 'Intern', found 'Employee'.*")
    public void testInvalidCast() {
        BRunUtil.invoke(compileResult, "testInvalidCast");
    }

    @Test(description = "Test mutation of record type using covariant array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina\\}InherentTypeViolation \\{\"message\":\"incompatible types: expected " +
                            "'Employee', found 'Person'\"\\}.*")
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
                    ".*error:.*incompatible types: expected 'int', found 'null'.*")
    public void testCovarianceIntOrNilArray() {
        BRunUtil.invoke(compileResult, "testCovarianceIntOrNilArray");
    }

    @Test(description = "Test mutation of arrays which include structural and simple values",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina\\}InherentTypeViolation \\{\"message\":\"incompatible types: expected " +
                            "'boolean\\|float\\|null', found 'Person'.*?")
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
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testUnionOfArrays");
        Assert.assertEquals((returnValues[0]).stringValue(), "BOOL");
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
                    ".*error: incompatible types: expected 'int', found 'string'.*")
    public void testJsonArrayMutability() {
        BRunUtil.invoke(compileResult, "testJsonArrayMutability");
    }

    @Test(description = "Test mutation of boolean array assigned to a json array",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*error: incompatible types: expected 'boolean', found 'string'.*")
    public void testJsonArrayMutability2() {
        BRunUtil.invoke(compileResult, "testJsonArrayMutability2");
    }

    @Test(description = "Test mutation of arrays through chained assignments",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina\\}InherentTypeViolation .*?\"message\":\"incompatible types: expected " +
                            "\\'int\\|string\\|null\\', found \\'boolean\\'.*")
    public void testChainingAssignment() {
        BRunUtil.invoke(compileResult, "testChainingAssignment");
    }

    @Test(description = "Test negative scenarios of assigning tuple literals")
    public void testNegativeTupleLiteralAssignments() {
        int i = 0;
        Assert.assertEquals(resultNegative.getErrorCount(), 8);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Employee[]', found 'Person[]'", 35, 21);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Employee[][]', found 'Person[][]'", 41, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Person[]', found 'Employee[][]'", 44, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Person[][][]', found 'Employee[][]'", 45, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int[3]', found 'int[]'", 52, 18);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int[3][3]', found 'int[3][]'", 55, 21);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Animal[]', found 'Cat[]'", 81, 28);
        BAssertUtil.validateError(resultNegative, i,
                "incompatible types: expected 'int[]|boolean[]', found 'int|boolean?[]'", 91, 10);
    }
}
