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

package org.ballerinalang.test.types.constant;

import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant test cases.
 */
public class MapConstantPanicTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/map-literal-constant-panic.bal");
    }

    // boolean ----------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field.*")
    public void updateNestedConstantBooleanMapValueWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantBooleanMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'boolean'.*")
    public void updateNestedConstantBooleanMapValueWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantBooleanMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bm11k' in record of type .*")
    public void updateReturnedConstantBooleanMapWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantBooleanMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'boolean'.*")
    public void updateReturnedConstantBooleanMap2WithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantBooleanMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bm11k' in record of type .*")
    public void updateConstantBooleanMapValueInArrayWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantBooleanMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'boolean'.*")
    public void updateConstantBooleanMapValueInArrayWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantBooleanMapValueInArrayWithNewKey");
    }

    // int ---------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'im11k' in record of type .*")
    public void updateNestedConstantIntMapValueWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantIntMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'int'.*")
    public void updateNestedConstantIntMapValueWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantIntMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'im11k' in record of type .*")
    public void updateReturnedConstantIntMapWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantIntMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'int'.*")
    public void updateReturnedConstantIntMap2WithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantIntMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'im11k' in record of type .*")
    public void updateConstantIntMapValueInArrayWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantIntMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'int'.*")
    public void updateConstantIntMapValueInArrayWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantIntMapValueInArrayWithNewKey");
    }

    // byte --------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bytem11k' in record of type .*")
    public void updateNestedConstantByteMapValueWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantByteMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'byte'.*")
    public void updateNestedConstantByteMapValueWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantByteMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bytem11k' in record of type .*")
    public void updateReturnedConstantByteMapWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantByteMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'byte'.*")
    public void updateReturnedConstantByteMap2WithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantByteMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bytem11k' in record of type .*")
    public void updateConstantByteMapValueInArrayWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantByteMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bytem11k' in record of type .*")
    public void updateConstantByteMapValueInArrayWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantByteMapValueInArrayWithNewKey");
    }

    // float -------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'fm11k' in record of type .*")
    public void updateNestedConstantFloatMapValueWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantFloatMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'float'.*")
    public void updateNestedConstantFloatMapValueWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantFloatMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'fm11k' in record of type .*")
    public void updateReturnedConstantFloatMapWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantFloatMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'float'.*")
    public void updateReturnedConstantFloatMap2WithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantFloatMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'fm11k' in record of type .*")
    public void updateConstantFloatMapValueInArrayWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantFloatMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'float'.*")
    public void updateConstantFloatMapValueInArrayWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantFloatMapValueInArrayWithNewKey");
    }

    // decimal -----------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'dm11k' in record of type .*")
    public void updateNestedConstantDecimalMapValueWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantDecimalMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'decimal'.*")
    public void updateNestedConstantDecimalMapValueWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantDecimalMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'dm10k': expected value of type " +
                    "'never', found 'decimal'.*")
    public void updateReturnedConstantDecimalMapWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantDecimalMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'decimal'.*")
    public void updateReturnedConstantDecimalMap2WithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantDecimalMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'dm11k' in record of type .*")
    public void updateConstantDecimalMapValueInArrayWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantDecimalMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'decimal'.*")
    public void updateConstantDecimalMapValueInArrayWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantDecimalMapValueInArrayWithNewKey");
    }

    // string ------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'sm11k' in record of type .*")
    public void updateNestedConstantStringMapValueWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantStringMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'string'.*")
    public void updateNestedConstantStringMapValueWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantStringMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'sm11k' in record of type .*")
    public void updateReturnedConstantStringMapWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantStringMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'string'.*")
    public void updateReturnedConstantStringMap2WithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantStringMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'sm11k' in record of type .*")
    public void updateConstantStringMapValueInArrayWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantStringMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'string.*")
    public void updateConstantStringMapValueInArrayWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantStringMapValueInArrayWithNewKey");
    }

    // nil ---------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'nm11k' in record of type .*")
    public void updateNestedConstantNilMapValueWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantNilMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found.*")
    public void updateNestedConstantNilMapValueWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateNestedConstantNilMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'nm11k' in record of type .*")
    public void updateReturnedConstantNilMapWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantNilMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found.*")
    public void updateReturnedConstantNilMap2WithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateReturnedConstantNilMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'nm11k' in record of type .*")
    public void updateConstantNilMapValueInArrayWithExistingKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantNilMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found.*")
    public void updateConstantNilMapValueInArrayWithNewKey() {
        JvmRunUtil.invoke(compileResult, "updateConstantNilMapValueInArrayWithNewKey");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
