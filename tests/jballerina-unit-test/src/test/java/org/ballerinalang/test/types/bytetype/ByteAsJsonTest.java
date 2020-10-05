/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This test class will test the behaviour when using a byte in a json context.
 *
 * @since 1.2.0
 */
public class ByteAsJsonTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/byte/byte_as_json_test.bal");
    }

    @Test(dataProvider = "byteAsJsonTests")
    public void testByteAsJson(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}InherentTypeViolation " +
                    "\\{\"message\":\"incompatible" +
                    " types: expected 'byte', found 'int'.*")
    public void testInherentTypeViolationForArray() {
        BRunUtil.invoke(result, "testInherentTypeViolationForArray");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: \\{ballerina/lang.map\\}InherentTypeViolation " +
                    "\\{\"message\":\"invalid map insertion: expected value of type 'byte', found 'int'.*")
    public void testInherentTypeViolationForMap() {
        BRunUtil.invoke(result, "testInherentTypeViolationForMap");
    }

    @DataProvider(name = "byteAsJsonTests")
    public static Object[][] byteAsJsonTests() {
        return new Object[][]{
                {"testByteAsJson"},
                {"testByteDowncastFromJson"},
                {"testByteArrayDowncastFromJsonArray"},
                {"testBytesInJsonArray"},
                {"testBytesInJsonMap"},
                {"testByteStructuredTypeAsJsonStructuredType"}
        };
    }
}
