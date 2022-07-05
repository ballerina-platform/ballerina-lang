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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

/**
 * Negative test class to check the Unicode patterns.
 */
public class UnicodeNegativeTest {

    @Test(description = "Test negative scenarios for unicode patterns.")
    public void testUnicodeNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/string/unicode-negative.bal");

        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "unicode code point 'D800' out of allowed range", 18, 21);
        BAssertUtil.validateError(compileResult, index++, "unicode code point 'D8FF' out of allowed range", 19, 21);
        BAssertUtil.validateError(compileResult, index++, "unicode code point 'DFFF' out of allowed range", 20, 21);
        BAssertUtil.validateError(compileResult, index++, "unicode code point '11FFFF' out of allowed range", 21, 21);
        BAssertUtil.validateError(compileResult, index++, "unicode code point '12FFFF' out of allowed range", 22, 21);
        BAssertUtil.validateError(compileResult, index++, "unicode code point 'DFFF' out of allowed range", 23, 21);
        BAssertUtil.validateError(compileResult, index++, "unicode code point 'DAFF' out of allowed range", 23, 29);
        BAssertUtil.validateError(compileResult, index++, "unicode code point '12FFFF' out of allowed range", 24, 21);
        BAssertUtil.validateError(compileResult, index++, "unicode code point 'DFFF' out of allowed range", 24, 36);
        BAssertUtil.validateError(compileResult, index++, "unicode code point 'DAFF' out of allowed range", 24, 49);
        BAssertUtil.validateError(compileResult, index++, "unicode code point '12FFFF' out of allowed range", 25, 21);
        BAssertUtil.validateError(compileResult, index++, "unicode code point 'DFFFAAA' out of allowed range", 25, 36);
        BAssertUtil.validateError(compileResult, index++, "unicode code point 'FFFFFFF' out of allowed range", 25, 52);
        BAssertUtil.validateError(compileResult, index, "invalid string numeric escape sequence", 26, 17);
    }
}
