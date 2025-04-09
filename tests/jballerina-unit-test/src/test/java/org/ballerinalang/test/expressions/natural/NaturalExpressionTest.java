/*
 * Copyright (c) 2025, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.test.expressions.natural;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative semantic test cases for natural expressions.
 */
public class NaturalExpressionTest {

    @Test
    public void testSemanticErrors() {
        CompileResult negativeRes = BCompileUtil.compile(
                "test-src/expressions/naturalexpr/natural_expr_negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeRes, i++,
                "the expected type for a 'natural' expression must contain 'error'", 17, 9);
        BAssertUtil.validateError(negativeRes, i++,
                "the expected type for a 'natural' expression must be a subtype of 'anydata|error'", 26, 57);
        BAssertUtil.validateError(negativeRes, i++, "undefined symbol 'today'", 37, 12);
        BAssertUtil.validateError(negativeRes, i++,
                "the expected type for a 'natural' expression must contain both a non-'error' type and 'error'",
                40, 32);
        Assert.assertEquals(negativeRes.getErrorCount(), i);
    }
}
