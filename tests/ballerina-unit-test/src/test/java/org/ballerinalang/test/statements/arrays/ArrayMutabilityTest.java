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
 * Test cases for ballerina.model.arrays.
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

    @Test(description = "",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*message: 'Employee' cannot be cast to 'Intern'.*")
    public void testInvalidCast() {
        BRunUtil.invoke(compileResult, "testInvalidCast");
    }

    @Test(description = "",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*message: type mismatch: expected type 'Employee', found type 'Person'.*")
    public void testAssignmentOfSuperTypeMember() {
        BRunUtil.invoke(compileResult, "testAssignmentOfSuperTypeMember");
    }

    @Test(description = "",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*message: type mismatch: expected type 'Employee', found type 'Student'.*")
    public void testInvalidAssignment() {
        BRunUtil.invoke(compileResult, "testInvalidAssignment");
    }

    @Test(description = "",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*message: type mismatch: expected type 'int', found type 'null'.*")
    public void testCovarianceIntOrNilArray() {
        BRunUtil.invoke(compileResult, "testCovarianceIntOrNilArray");
    }

    @Test(description = "",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    ".*message: type mismatch: expected type 'boolean\\|float', found type 'Person'.*")
    public void testCovarianceBooleanOrFloatOrRecordArray() {
        BRunUtil.invoke(compileResult, "testCovarianceBooleanOrFloatOrRecordArray");
    }


    @Test(description = "Test negative scenarios of assigning tuple literals")
    public void testNegativeTupleLiteralAssignments() {
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Employee[]', found 'Person[]'", 34, 21);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Employee[][]', found 'Person[][]'", 40, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Person[]', found 'Employee[][]'", 43, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'Person[][][]', found 'Employee[][]'", 44, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int|Person[]', found 'int|Person?[]'", 48, 26);
    }
}
