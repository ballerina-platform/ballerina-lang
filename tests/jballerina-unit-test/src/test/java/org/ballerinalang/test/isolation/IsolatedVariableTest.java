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
package org.ballerinalang.test.isolation;

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * Test cases related to isolated variables.
 *
 * @since 2.0.0
 */
public class IsolatedVariableTest {

    private static final String ERROR_EXPECTED_AN_ISOLATED_EXPRESSION =
            "invalid initial value expression: expected an isolated expression";
    private static final String ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK =
            "invalid access of an 'isolated' variable outside a 'lock' statement";
    private static final String ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS =
            "cannot access more than one variable for which usage is restricted in a single 'lock' statement";

    private static final String ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE =
            "invalid attempt to transfer a value into a 'lock' statement with restricted variable usage";
    private static final String ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE =
            "invalid attempt to transfer out a value from a 'lock' statement with restricted variable usage: expected" +
                    " an isolated expression";
    private static final String ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE =
            "invalid invocation of a non-isolated function in a 'lock' statement with restricted variable usage";

    @Test
    public void testIsolatedVariablesSemanticNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolated-variables/isolated_variables_semantic_negative.bal");
        int i = 0;
        validateError(result, i++, "incompatible types: expected 'IsolatedObject', found 'NonIsolatedObject'", 32, 30);
        validateError(result, i++, "incompatible types: expected 'IsolatedObject', found 'IsolatedNonMatchingClass'",
                      34, 30);
        validateError(result, i++, "an uninitialized module variable declaration cannot be marked as 'isolated'", 36,
                      1);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testIsolatedVariablesIsolationNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolated-variables/isolated_variables_isolation_negative.bal");
        int i = 0;
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 19, 23);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 19, 26);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 25, 8);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 26, 33);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 32, 7);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 41, 69);
        validateError(result, i++, ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK, 67, 19);
        validateError(result, i++, ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK, 74, 13);
        validateWarning(result, i++, "unused variable 'arr2'", 81, 9);
        validateError(result, i++, ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK, 81, 24);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 90, 21);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 91, 13);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 97, 21);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 98, 13);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 99, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 111, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 112, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 113, 14);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 113, 14);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 113, 19);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 122, 32);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 123, 19);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 126, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 127, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 128, 14);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 128, 14);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 128, 19);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 129, 15);
        validateWarning(result, i++, "unused variable 'bm1'", 134, 5);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 138, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 139, 16);
        validateWarning(result, i++, "unused variable 'bm1'", 144, 5);
        validateWarning(result, i++, "unused variable 'bm2'", 146, 9);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 149, 19);
        validateError(result, i++, ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 150, 17);
        validateError(result, i++, "invalid invocation of a non-isolated function in an 'isolated' function", 150, 17);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 152, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 152, 13);
        validateError(result, i++, ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 159, 13);
        validateWarning(result, i++, "unused variable 'i'", 168, 13);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 168, 21);
        validateWarning(result, i++, "unused variable 'j'", 171, 9);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 172, 9);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 187, 21);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 195, 29);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 201, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 207, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 213, 32);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 225, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 231, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 244, 28);
        validateError(result, i++, "unreachable code", 246, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 246, 20);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 253, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 264, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 271, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 280, 22);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 281, 22);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 287, 22);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 288, 20);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 288, 39);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 305, 9);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 306, 9);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 307, 20);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 308, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 308, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 314, 13);
        validateWarning(result, i++, "unused variable 'y'", 319, 5);
        validateWarning(result, i++, "unused variable 'z'", 320, 5);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 323, 9);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 324, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 325, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 333, 16);
        validateWarning(result, i++, "unused variable 'e'", 342, 7);
        validateWarning(result, i++, "unused variable 'k'", 343, 9);
        validateError(result, i++, ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK, 343, 17);
        validateWarning(result, i++, "unused variable 'k'", 350, 17);
        validateError(result, i++, ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK, 354, 9);
        validateWarning(result, i++, "unused variable 'e'", 359, 7);
        validateError(result, i++, ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK, 360, 9);
        validateWarning(result, i++, "unused variable 'e'", 367, 7);
        validateError(result, i++, ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK, 368, 9);
        validateWarning(result, i++, "unused variable 'e'", 373, 7);
        validateError(result, i++, ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK, 374, 9);
        validateError(result, i++, ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK, 374, 11);
        Assert.assertEquals(result.getErrorCount(), i - 14);
        Assert.assertEquals(result.getWarnCount(), 14);
    }

    @Test
    public void testIsolatedVariables() {
        CompileResult compileResult = BCompileUtil.compile("test-src/isolated-variables/isolated_variables.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);

        Assert.assertEquals(compileResult.getWarnCount(), 11);
        for (Diagnostic diagnostic : compileResult.getDiagnostics()) {
            Assert.assertTrue(diagnostic.message().startsWith("unused variable"));
        }
    }
}
