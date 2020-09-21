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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.string;

import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test StringValue impl of ballerina string.
 */
public class StringValueBasicsTest extends BStringTestCommons {

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/string-value-test.bal");
    }

    @Test
    public void testConcatBMPStrings() {
        BValue[] returns = BRunUtil.invoke(result, "concatBMP");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "red apple");
    }

    @Test
    public void testNonBMPStringLength() {
        testAndAssert("nonBMPLength", 5);
    }

    @Test
    public void testRecordStringValue() {
        testAndAssert("recordStringValue", 5);
        //TODO assert return value has BString
    }

    @Test
    public void testError() {
        testAndAssert("testError", 5);
    }

    @Test
    public void testArrayStore() {
        testAndAssert("testArrayStore", 10);
    }

    @Test
    public void testStringIndexAccess() {
        testAndAssert("testStringIndexAccess", 1);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*string index out of range: index: 6, size: 6.*")
    public void testStringIndexAccessException() {
        BRunUtil.invoke(result, "testStringIndexAccessException");
    }

    @Test
    public void testCastToString() {
        testAndAssert("anyToStringCasting", 6);
        testAndAssert("anydataToStringCast", 6);
    }
}
