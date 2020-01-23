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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

/**
 * Test class to check the Unicode patterns.
 */
public class UniCodeNegativeTest {

    @Test(description = "Test negative scenarios for unicode patterns.")
    public void testUniCodeNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/string/unicode-negative.bal");

        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{D800}'", 18, 18);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{D8FF}'", 19, 18);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{DFFF}'", 20, 18);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{11FFFF}'", 21, 18);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{12FFFF}'", 22, 18);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{DFFF}'", 23, 18);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{DAFF}'", 23, 26);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{12FFFF}'", 24, 18);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{DFFF}'", 24, 33);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{DAFF}'", 24, 46);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{12FFFF}'", 25, 18);
        BAssertUtil.validateError(compileResult, index++, "unsupported unicode '\\u{DFFFAAA}'", 25, 33);
        BAssertUtil.validateError(compileResult, index, "unsupported unicode '\\u{FFFFFFF}'", 25, 49);
    }
}
