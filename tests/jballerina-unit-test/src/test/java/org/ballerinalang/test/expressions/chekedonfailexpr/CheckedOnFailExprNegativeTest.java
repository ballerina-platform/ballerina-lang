/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.expressions.chekedonfailexpr;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * Negative tests for checked on fail expression.
 *
 * @since 2201.8.0
 */
public class CheckedOnFailExprNegativeTest {

    @Test
    public void testCheckedOnFailExprNegative() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/expressions/checkedonfailexpr/check_on_fail_expr_negative.bal");
        int i = 0;
        validateError(negativeResult, i++, "variable assignment is required", 24, 5);
        validateWarning(negativeResult, i++, "invalid usage of the 'check' expression operator: " +
                "no expression type is equivalent to error type", 24, 11);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 25, 19);
        validateWarning(negativeResult, i++, "invalid usage of the 'check' expression operator: " +
                "no expression type is equivalent to error type", 25, 25);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found '()'", 26, 16);
        validateError(negativeResult, i++, "operator '+' not defined for 'int' and 'float'", 32, 15);
        validateError(negativeResult, i++, "incompatible types: expected 'error', found 'int'", 36, 41);
        validateError(negativeResult, i++, "incompatible types: expected 'error', found '(int|error)'", 37, 41);
        Assert.assertEquals(negativeResult.getDiagnosticResult().diagnosticCount(), i);
    }

}
