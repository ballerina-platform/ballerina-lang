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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

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
            "invalid attempt to transfer out a value from a 'lock' statement with restricted variable usage";
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
        validateError(result, i++, ERROR_INVALID_ISOLATED_VAR_ACCESS_OUTSIDE_LOCK, 81, 24);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 90, 21);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 91, 13);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 97, 21);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 98, 13);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 99, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 111, 9);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 111, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 112, 9);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 112, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 113, 14);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 113, 19);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 122, 32);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 123, 19);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 126, 9);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 126, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 127, 9);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 127, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 128, 14);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 128, 19);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 129, 15);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 138, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 139, 16);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 149, 19);
        validateError(result, i++, ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 150, 17);
        validateError(result, i++, "invalid invocation of a non-isolated function in an 'isolated' function", 150, 17);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 152, 13);
        validateError(result, i++, ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 159, 13);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 168, 21);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MULTIPLE_RESTRICTER_VARS, 172, 9);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testIsolatedVariables() {
        CompileResult compileResult = BCompileUtil.compile("test-src/isolated-variables/isolated_variables.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
    }
}
