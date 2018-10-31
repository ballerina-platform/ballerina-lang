/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.integer;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for negative integer tests.
 */
public class BIntegerValueNegativeTest {

    @Test
    public void testIntegerValue() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/integer/integer-value-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 10);

        int index = 0;
        String expectedError = "mismatched input 'int'. expecting {'but', 'is', ';', '?', '+', '-', '*', '/', '%', " +
                "'==', '!=', '>', '<', '>=', '<=', '&&', '||', '===', '!==', '&', '^', '...', '|', '?:', '..<'}";
        BAssertUtil.validateError(compileResult, index++, expectedError, 5, 5);

        expectedError = "Hexadecimal '0xFFFFFFFFFFFFFFFF' too large";
        BAssertUtil.validateError(compileResult, index++, expectedError, 5, 13);

        expectedError = "Integer '9999999999999999999' too large";
        BAssertUtil.validateError(compileResult, index++, expectedError, 6, 13);

        expectedError = "Binary '0b1111111111111111111111111111111111111111111111111111111111111111' too large";
        BAssertUtil.validateError(compileResult, index++, expectedError, 7, 13);

        expectedError = "Hexadecimal '-0xFFFFFFFFFFFFFFFF' too small";
        BAssertUtil.validateError(compileResult, index++, expectedError, 9, 13);

        expectedError = "Integer '-9999999999999999999' too small";
        BAssertUtil.validateError(compileResult, index++, expectedError, 10, 13);

        expectedError = "Binary '-0b1111111111111111111111111111111111111111111111111111111111111111' too small";
        BAssertUtil.validateError(compileResult, index++, expectedError, 11, 13);

        expectedError = "extraneous input '672'";
        BAssertUtil.validateError(compileResult, index++, expectedError, 13, 14);

        expectedError = "extraneous input '912'";
        BAssertUtil.validateError(compileResult, index++, expectedError, 14, 14);

        expectedError = "mismatched input '}'. expecting {'but', 'is', ';', '?', '+', '-', '*', '/', '%', '==', " +
                "'!=', '>', '<', '>=', '<=', '&&', '||', '===', '!==', '&', '^', '...', '|', '?:', '..<'}";
        BAssertUtil.validateError(compileResult, index, expectedError, 18, 1);
    }
}
