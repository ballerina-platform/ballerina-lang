/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.bytetype;

import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This test class will test the behaviour when using a byte in an int context.
 *
 * @since 1.1.0
 */
public class ByteAsIntTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/byte/byte_as_int_test.bal");
    }

    @Test(dataProvider = "byteAsIntTests")
    public void testByteAsInt(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: \\{ballerina/lang.array\\}InherentTypeViolation " +
                    "\\{\"message\":\"incompatible types: expected 'byte', found 'int'.*")
    public void testInherentTypeViolationForArray() {
        BRunUtil.invoke(result, "testInherentTypeViolationForArray");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: \\{ballerina/lang.map\\}InherentTypeViolation " +
                    "\\{\"message\":\"invalid map insertion: expected value of type 'byte', found 'int'.*")
    public void testInherentTypeViolationForMap() {
        BRunUtil.invoke(result, "testInherentTypeViolationForMap");
    }

    @DataProvider(name = "byteAsIntTests")
    public static Object[][] byteAsIntTests() {
        return new Object[][]{
                {"testByteAsInt"},
                {"testByteDowncastFromInt"},
                {"testByteArrayDowncastFromIntArray"},
                {"testBytesInIntArray"},
                {"testBytesInIntMap"},
                {"testByteStructuredTypeAsIntStructuredType"}
        };
    }

    @Test
    public void testByteArrayCastToIntArray() {
        BRunUtil.invoke(result, "testByteArrayCastToIntArray");
        BRunUtil.invoke(result, "testDowncastOfByteArrayCastToIntArray");
        BRunUtil.invoke(result, "testInherentTypeViolationOfByteArrayCastToIntArray");
    }

    @Test
    public void testByteArrayLiteralCastToReadOnlyType() {
        BRunUtil.invoke(result, "testByteArrayLiteralCastToReadOnlyType");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
