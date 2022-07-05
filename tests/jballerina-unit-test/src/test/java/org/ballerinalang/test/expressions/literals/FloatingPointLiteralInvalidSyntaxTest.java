/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.expressions.literals;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This class tests invalid syntax of floating point literal.
 *
 * @since 2201.1.0
 */
public class FloatingPointLiteralInvalidSyntaxTest {
    @Test
    public void testMissingDigitAfterExponentIndicator() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/expressions/literals/floating_point_literal_syntax_negative.bal");
        int expectedErrorCount = 79;
        Assert.assertEquals(negativeResult.getErrorCount(), expectedErrorCount);
        for (int i = 0; i < expectedErrorCount - 7; i++) {
            validateError(negativeResult, i, "missing digit after exponent indicator", i + 18, 9);
        }

        for (int i = 72; i < expectedErrorCount - 3; i++) {
            validateError(negativeResult, i, "missing hex number after hex indicator", i + 18, 9);
        }

        validateError(negativeResult, 76, "missing digit after exponent indicator", 95, 13);
        validateError(negativeResult, 77, "missing digit after exponent indicator", 96, 13);
        validateError(negativeResult, 78, "missing hex number after hex indicator", 97, 13);
    }
}
