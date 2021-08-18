/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests functionalities of the {@link StringUtils} class.
 */
public class StringUtilsTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/string-utils.bal");
    }

    @Test
    public void testStringUtils() {
        BRunUtil.invoke(result, "testStringUtils");
    }

    public static BString getStringVal(Object... values) {
        StringBuilder content = new StringBuilder();
        for (Object value : values) {
            if (value != null) {
                content.append(StringUtils.getStringValue(value, null));
            }
        }
        return StringUtils.fromString(content.toString());
    }

    @Test
    public void testStringValue() {
        BRunUtil.invoke(result, "testStringValue");
    }

    public static BString invokeStringValue(BValue value) {
        return StringUtils.fromString(value.stringValue(null));
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
