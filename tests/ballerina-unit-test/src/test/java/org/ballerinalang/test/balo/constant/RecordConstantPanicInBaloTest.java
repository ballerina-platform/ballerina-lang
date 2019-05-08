/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.balo.constant;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for reading constants.
 */
public class RecordConstantPanicInBaloTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
        compileResult = BCompileUtil.compile("test-src/balo/test_balo/constant/record-constant-panic.bal");
    }

    // boolean ----------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantBooleanRecordValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantBooleanRecordValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantBooleanRecordValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantBooleanRecordValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantBooleanRecordWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantBooleanRecordWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantBooleanRecord2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantBooleanRecord2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantBooleanRecordValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantBooleanRecordValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantBooleanRecordValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantBooleanRecordValueInArrayWithNewKey");
    }

    // int ---------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantIntRecordValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantIntRecordValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantIntRecordValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantIntRecordValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantIntRecordWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantIntRecordWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantIntRecord2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantIntRecord2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantIntRecordValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantIntRecordValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantIntRecordValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantIntRecordValueInArrayWithNewKey");
    }

    // byte --------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantByteRecordValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantByteRecordValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantByteRecordValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantByteRecordValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantByteRecordWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantByteRecordWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantByteRecord2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantByteRecord2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantByteRecordValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantByteRecordValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantByteRecordValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantByteRecordValueInArrayWithNewKey");
    }

    // float -------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantFloatRecordValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantFloatRecordValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantFloatRecordValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantFloatRecordValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantFloatRecordWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantFloatRecordWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantFloatRecord2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantFloatRecord2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantFloatRecordValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantFloatRecordValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantFloatRecordValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantFloatRecordValueInArrayWithNewKey");
    }

    // decimal -----------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantDecimalRecordValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantDecimalRecordValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantDecimalRecordValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantDecimalRecordValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantDecimalRecordWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantDecimalRecordWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantDecimalRecord2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantDecimalRecord2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantDecimalRecordValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantDecimalRecordValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantDecimalRecordValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantDecimalRecordValueInArrayWithNewKey");
    }

    // string ------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantStringRecordValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantStringRecordValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantStringRecordValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantStringRecordValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantStringRecordWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantStringRecordWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantStringRecord2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantStringRecord2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantStringRecordValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantStringRecordValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantStringRecordValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantStringRecordValueInArrayWithNewKey");
    }

    // nil ---------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantNilRecordValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantNilRecordValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantNilRecordValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantNilRecordValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantNilRecordWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantNilRecordWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantNilRecord2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantNilRecord2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantNilRecordValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantNilRecordValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantNilRecordValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantNilRecordValueInArrayWithNewKey");
    }
}
