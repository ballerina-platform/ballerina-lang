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

package org.ballerinalang.test.types.decimaltype;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This test class contains negative test cases for decimal value.
 *
 * @since 0.985.0
 */
public class BDecimalValueNegativeTest {
    @Test
    public void testDecimalValue() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/decimal/decimal_value_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 5);

        int index = 0;

        String expectedError = "mismatched input 'decimal'. expecting {'is', ';', '?', '+', '-', '*', '/', '%', " +
                "'==', '!=', '>', '<', '>=', '<=', '&&', '||', '===', '!==', '&', '^', '...', '|', '?:', '->>', " +
                "'..<'}";
        BAssertUtil.validateError(compileResult, index++, expectedError, 23, 5);

        expectedError = "extraneous input 'g'";
        BAssertUtil.validateError(compileResult, index++, expectedError, 23, 21);

        expectedError = "extraneous input '23.04'";
        BAssertUtil.validateError(compileResult, index++, expectedError, 26, 18);

        expectedError = "invalid token 'xX1231'";
        BAssertUtil.validateError(compileResult, index++, expectedError, 29, 19);

        expectedError = "invalid token 'X1231'";
        BAssertUtil.validateError(compileResult, index, expectedError, 32, 20);
    }

    @Test
    void testDecimalValueNegativeLiteral() {
        CompileResult negative = BCompileUtil.compile("test-src/types/decimal/decimal_value_negative_literal.bal");
        Assert.assertEquals(negative.getErrorCount(), 17);
        int i = 0;
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 20, 17);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 21, 17);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'int'", 22, 17);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'int'", 23, 17);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'int'", 24, 17);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'int'", 25, 17);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 26, 22);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 27, 22);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 27, 31);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 28, 23);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 34, 23);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 35, 23);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 36, 24);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 37, 23);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 38, 17);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 39, 17);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected 'decimal', found 'float'", 41, 37);
    }
}
