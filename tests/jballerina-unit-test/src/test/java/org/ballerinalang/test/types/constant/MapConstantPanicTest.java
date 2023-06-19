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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
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

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field.*")
    public void updateNestedConstantBooleanMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantBooleanMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'boolean'.*")
    public void updateNestedConstantBooleanMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantBooleanMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bm11k' in record of type .*")
    public void updateReturnedConstantBooleanMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantBooleanMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'boolean'.*")
    public void updateReturnedConstantBooleanMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantBooleanMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bm11k' in record of type .*")
    public void updateConstantBooleanMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantBooleanMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'boolean'.*")
    public void updateConstantBooleanMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantBooleanMapValueInArrayWithNewKey");
    }

    // int ---------------------------------------------------

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'im11k' in record of type .*")
    public void updateNestedConstantIntMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantIntMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'int'.*")
    public void updateNestedConstantIntMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantIntMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'im11k' in record of type .*")
    public void updateReturnedConstantIntMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantIntMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'int'.*")
    public void updateReturnedConstantIntMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantIntMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'im11k' in record of type .*")
    public void updateConstantIntMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantIntMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'int'.*")
    public void updateConstantIntMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantIntMapValueInArrayWithNewKey");
    }

    // byte --------------------------------------------------

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bytem11k' in record of type .*")
    public void updateNestedConstantByteMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantByteMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'byte'.*")
    public void updateNestedConstantByteMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantByteMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bytem11k' in record of type .*")
    public void updateReturnedConstantByteMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantByteMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'byte'.*")
    public void updateReturnedConstantByteMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantByteMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bytem11k' in record of type .*")
    public void updateConstantByteMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantByteMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'bytem11k' in record of type .*")
    public void updateConstantByteMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantByteMapValueInArrayWithNewKey");
    }

    // float -------------------------------------------------

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'fm11k' in record of type .*")
    public void updateNestedConstantFloatMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantFloatMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'float'.*")
    public void updateNestedConstantFloatMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantFloatMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'fm11k' in record of type .*")
    public void updateReturnedConstantFloatMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantFloatMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'float'.*")
    public void updateReturnedConstantFloatMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantFloatMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'fm11k' in record of type .*")
    public void updateConstantFloatMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantFloatMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'float'.*")
    public void updateConstantFloatMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantFloatMapValueInArrayWithNewKey");
    }

    // decimal -----------------------------------------------

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'dm11k' in record of type .*")
    public void updateNestedConstantDecimalMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantDecimalMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'decimal'.*")
    public void updateNestedConstantDecimalMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantDecimalMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'dm10k': expected value of type " +
                    "'never', found 'decimal'.*")
    public void updateReturnedConstantDecimalMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantDecimalMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'decimal'.*")
    public void updateReturnedConstantDecimalMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantDecimalMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'dm11k' in record of type .*")
    public void updateConstantDecimalMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantDecimalMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'decimal'.*")
    public void updateConstantDecimalMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantDecimalMapValueInArrayWithNewKey");
    }

    // string ------------------------------------------------

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'sm11k' in record of type .*")
    public void updateNestedConstantStringMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantStringMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'string'.*")
    public void updateNestedConstantStringMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantStringMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'sm11k' in record of type .*")
    public void updateReturnedConstantStringMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantStringMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'string'.*")
    public void updateReturnedConstantStringMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantStringMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'sm11k' in record of type .*")
    public void updateConstantStringMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantStringMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found 'string.*")
    public void updateConstantStringMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantStringMapValueInArrayWithNewKey");
    }

    // nil ---------------------------------------------------

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'nm11k' in record of type .*")
    public void updateNestedConstantNilMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantNilMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found.*")
    public void updateNestedConstantNilMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantNilMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'nm11k' in record of type .*")
    public void updateReturnedConstantNilMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantNilMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found.*")
    public void updateReturnedConstantNilMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantNilMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*cannot update 'readonly' field 'nm11k' in record of type .*")
    public void updateConstantNilMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantNilMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*invalid value for record field 'newKey': expected value of type " +
                    "'never', found.*")
    public void updateConstantNilMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantNilMapValueInArrayWithNewKey");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
