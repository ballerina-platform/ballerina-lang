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
package org.ballerinalang.test.types.tuples;

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
 * Test cases for tuple mutability.
 */
public class TupleMutabilityTest {

    private CompileResult compileResult;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/tuples/tuple-mutability.bal");
        resultNegative = BCompileUtil.compile("test-src/types/tuples/tuple-mutability-negative.bal");
    }

    @Test
    public void testValidTupleAssignment() {
        BArray returnValues = (BArray) BRunUtil.invoke(compileResult, "testValidTupleAssignment");
        Assert.assertTrue((Boolean) returnValues.get(0), "Expected value of true but found false");
        Assert.assertEquals(returnValues.get(1), 100L, "Expected value of 100");
    }

    @Test
    public void testWithTryCatch() {
        Object returnValues = BRunUtil.invoke(compileResult, "testWithTryCatch");
        Assert.assertEquals(returnValues, 5L, "Expected value of 5");
    }

    @Test(description = "Check if correct type is saved in covariant tuple with record type ",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible types: 'Employee' cannot be " +
                            "cast to 'Intern'.*")
    public void testInvalidCast() {
        BRunUtil.invoke(compileResult, "testInvalidCast");
    }

    @Test(description = "Test mutation of record type using covariant tuple",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation \\{\"message\":\"incompatible types: " +
                            "expected 'Employee', found 'Person'.*")
    public void testAssignmentOfSuperTypeMember() {
        BRunUtil.invoke(compileResult, "testAssignmentOfSuperTypeMember");
    }

    @Test(description = "Test mutation of record type by assigning invalid record type",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation \\{\"message\":\"incompatible types: " +
                            "expected 'Employee', found 'Student'.*")
    public void testInvalidAssignment() {
        BRunUtil.invoke(compileResult, "testInvalidAssignment");
    }

    @Test(description = "Test mutation of int by inserting nil value to int? covariant tuple",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation \\{\"message\":\"incompatible types: " +
                            "expected 'int', found '\\(\\)'.*")
    public void testCovarianceIntOrNilArray() {
        BRunUtil.invoke(compileResult, "testCovarianceIntOrNilTuple");
    }

    @Test
    public void testDifferentTypeCovariance() {
        Object results = BRunUtil.invoke(compileResult, "testDifferentTypeCovariance");
        Assert.assertEquals(results, 12L);
    }

    @Test(description = "Test mutation of tuple which include structural and simple values",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation \\{\"message\":\"incompatible types: " +
                            "expected '\\(boolean\\|float\\)', found 'Person'.*")
    public void testCovarianceBooleanOrFloatOrRecordArray() {
        BRunUtil.invoke(compileResult, "testCovarianceBooleanOrFloatOrRecordTuple");
    }

    @Test(description = "Test negative scenarios of assigning tuple literals")
    public void testNegativeTupleLiteralAssignments() {
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[Employee,Employee]', found '[Person,Employee]'",
                34, 31);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected '[(boolean|float|Person),int]', found '[(boolean|float),int?]'",
                38, 38);
        BAssertUtil.validateError(resultNegative, i,
                "incompatible types: expected '[[int?,boolean?],Person?]', found '[[(int|string),boolean],Person]'",
                42, 38);
    }

    @Test
    public void testComplexTypes() {
        BArray returnValues = (BArray) BRunUtil.invoke(compileResult, "testComplexTupleTypes");
        Assert.assertEquals(returnValues.get(0), 12.0);
        Assert.assertTrue((Boolean) returnValues.get(1));
        Assert.assertTrue((Boolean) returnValues.get(2));
        Assert.assertEquals(returnValues.get(3).toString(), "json");
        Assert.assertEquals(returnValues.get(4), 12.0);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        resultNegative = null;
    }
}
