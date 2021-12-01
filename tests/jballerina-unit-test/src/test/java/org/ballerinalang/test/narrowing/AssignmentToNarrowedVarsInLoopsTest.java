/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.ballerinalang.test.narrowing;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for assignments to narrowed variables in loops.
 *
 * @since 2.0.0
 */
public class AssignmentToNarrowedVarsInLoopsTest {
    private static final String INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR =
            "invalid attempt to assign a value to a variable narrowed outside the loop";

    @Test
    public void testAssignmentToNarrowedVarsInLoops() {
        CompileResult result = BCompileUtil.compile("test-src/narrowing/assignment_to_narrowed_vars_in_loops.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
        Assert.assertEquals(result.getHintCount(), 2);

        Assert.assertEquals(result.getWarnCount(), 69);
        for (Diagnostic diagnostic : result.getDiagnostics()) {
            if (diagnostic.diagnosticInfo().severity() != DiagnosticSeverity.WARNING) {
                continue;
            }

            Assert.assertTrue(diagnostic.message().startsWith("unused variable"));
        }
    }

    @Test
    public void testAssignmentToNarrowedVarsInLoopsNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/narrowing/assignment_to_narrowed_vars_in_loops_negative.bal");
        int index = 0;

        BAssertUtil.validateWarning(result, index++, "unused variable 'j'", 23, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 24, 13);
        BAssertUtil.validateWarning(result, index++, "unused variable 'j'", 32, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 33, 13);
        BAssertUtil.validateWarning(result, index++, "unused variable 'j'", 41, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 42, 13);
        BAssertUtil.validateWarning(result, index++, "unused variable 'j'", 51, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 52, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 67, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 68, 17);
        BAssertUtil.validateWarning(result, index++, "unused variable 'j'", 72, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 80, 17);
        BAssertUtil.validateWarning(result, index++, "unused variable 'j'", 84, 13);
        BAssertUtil.validateWarning(result, index++, "unused variable 'c'", 96, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 97, 17);
        BAssertUtil.validateWarning(result, index++, "unused variable 'c'", 105, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 106, 17);
        BAssertUtil.validateWarning(result, index++, "unused variable 'c'", 120, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 121, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 136, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 150, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 158, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 166, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 175, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 188, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 199, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 215, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 224, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 239, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 254, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 268, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 284, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 302, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 325, 25);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 340, 25);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 360, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 363, 25);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 369, 25);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 380, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 383, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 387, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 399, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 403, 25);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 420, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 424, 21);
        BAssertUtil.validateWarning(result, index++, "unused variable 'f'", 444, 41);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 445, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 446, 13);
        BAssertUtil.validateWarning(result, index++, "unused variable 'e'", 452, 38);
        BAssertUtil.validateWarning(result, index++, "unused variable 'f'", 452, 41);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 459, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 460, 13);
        BAssertUtil.validateWarning(result, index++, "unused variable 'f'", 466, 41);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 468, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 469, 13);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 487, 25);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 503, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 524, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 547, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 568, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 587, 17);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 609, 21);
        BAssertUtil.validateError(result, index++, INVALID_ASSIGNMENT_TO_NARROWED_VAR_ERROR, 628, 13);

        Assert.assertEquals(result.getErrorCount(), index - 13);
        Assert.assertEquals(result.getWarnCount(), 13);
    }
}
