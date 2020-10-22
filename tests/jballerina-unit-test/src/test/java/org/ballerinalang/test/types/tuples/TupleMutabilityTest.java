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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testValidTupleAssignment");
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue(), "Expected value of true but found false");
        Assert.assertEquals(((BInteger) returnValues[1]).intValue(), 100, "Expected value of 100");
    }

    @Test
    public void testWithTryCatch() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testWithTryCatch");
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 5, "Expected value of 5");
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
        BValue[] results = BRunUtil.invoke(compileResult, "testDifferentTypeCovariance");
        Assert.assertEquals(((BInteger) results[0]).intValue(), 12);
    }

    @Test(description = "Test mutation of tuple which include structural and simple values",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.array\\}InherentTypeViolation \\{\"message\":\"incompatible types: " +
                            "expected 'boolean\\|float', found 'Person'.*")
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
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testComplexTupleTypes");
        Assert.assertEquals(((BFloat) returnValues[0]).floatValue(), 12.0);
        Assert.assertTrue(((BBoolean) returnValues[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returnValues[2]).booleanValue());
        Assert.assertEquals(returnValues[3].stringValue(), "json");
        Assert.assertEquals(((BFloat) returnValues[4]).floatValue(), 12.0);
    }
}
