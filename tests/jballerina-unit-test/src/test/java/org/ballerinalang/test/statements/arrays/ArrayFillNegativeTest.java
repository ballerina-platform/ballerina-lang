/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */

package org.ballerinalang.test.statements.arrays;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for filling the elements of the array with its type's implicit initial value.
 *
 * @since 2201.9.0
 */
public class ArrayFillNegativeTest {

    private CompileResult negativeCompileResult;

    @BeforeClass
    public void setup() {
        negativeCompileResult =
                BCompileUtil.compile("test-src/statements/arrays/array-fill-test-negative.bal");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testArrayFillWithObjWithInitParam() {
        BRunUtil.invoke(negativeCompileResult, "testArrayFillWithObjWithInitParam");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testArrayFillWithIntFiniteTypes() {
        BRunUtil.invoke(negativeCompileResult, "testArrayFillWithIntFiniteTypes");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testArrayFillWithFloatFiniteTypes() {
        BRunUtil.invoke(negativeCompileResult, "testArrayFillWithFloatFiniteTypes");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testArrayFillWithStringFiniteTypes() {
        BRunUtil.invoke(negativeCompileResult, "testArrayFillWithStringFiniteTypes");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testArrayFillWithTypedesc() {
        BRunUtil.invoke(negativeCompileResult, "testArrayFillWithTypedesc");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testNonSequentialArrayInsertion() {
        BRunUtil.invoke(negativeCompileResult, "testNonSequentialArrayInsertion");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testIllegalArrayInsertion() {
        BRunUtil.invoke(negativeCompileResult, "testIllegalArrayInsertion");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testIllegalTwoDimensionalArrayInsertion() {
        BRunUtil.invoke(negativeCompileResult, "testIllegalTwoDimensionalArrayInsertion");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testRecordTypeWithRequiredFieldsArrayFill() {
        BRunUtil.invoke(negativeCompileResult, "testRecordTypeWithRequiredFieldsArrayFill");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testFiniteTypeArrayFillNegative() {
        BRunUtil.invoke(negativeCompileResult, "testFiniteTypeArrayFill");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testFiniteTypeArrayFillNegative2() {
        BRunUtil.invoke(negativeCompileResult, "testFiniteTypeArrayFill2");
    }

    @AfterClass
    public void tearDown() {
        negativeCompileResult = null;
    }
}
