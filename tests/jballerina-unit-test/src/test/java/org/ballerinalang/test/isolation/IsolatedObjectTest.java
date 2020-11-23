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
 * Test cases related to isolated objects.
 *
 * @since 2.0.0
 */
public class IsolatedObjectTest {

    private static final String ERROR_EXPECTED_AN_ISOLATED_EXPRESSION =
            "invalid initial value expression: expected an isolated expression";
    private static final String ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE =
            "cannot assign to a variable outside the 'lock' statement with restricted variable usage, " +
                    "if not just a variable name";
    private static final String ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE =
            "invalid attempt to transfer a value into a 'lock' statement with restricted variable usage";
    private static final String ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE =
            "invalid attempt to transfer out a value from a 'lock' statement with restricted variable usage";
    private static final String ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE =
            "invalid invocation of a non-isolated function in a 'lock' statement with restricted variable usage";

    private static final String ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC =
            "invalid access of mutable storage in an 'isolated' function";

    @Test
    public void testIsolatedObjectSemanticNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolated-objects/isolated_objects_semantic_negative.bal");
        int i = 0;
        validateError(result, i++, "incompatible types: expected 'Foo', found 'isolated object { }'", 69, 14);
        validateError(result, i++, "incompatible types: expected 'Baz', found 'isolated object { }'", 70, 14);
        validateError(result, i++, "incompatible types: expected 'Bar', found 'Baz'", 78, 14);
        validateError(result, i++, "incompatible types: expected 'Baz', found 'Bar'", 79, 14);
        validateError(result, i++, "incompatible types: expected 'Qux', found 'Quux'", 81, 14);
        validateError(result, i++, "incompatible types: expected 'Foo', found 'Quux'", 83, 19);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testIsolatedObjectIsolationNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolated-objects/isolated_objects_isolation_negative.bal");
        int i = 0;
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 18, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 19, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 31, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 32, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 47, 6);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 47, 6);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 55, 97);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 55, 97);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 71, 39);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 74, 9);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 81, 16);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 91, 43);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 94, 13);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 101, 20);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 118, 30);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 131, 18);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 131, 24);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 131, 36);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 134, 18);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 141, 30);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 141, 42);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 142, 34);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 157, 22);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 179, 25);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 180, 25);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 181, 23);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 190, 36);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 191, 23);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 194, 25);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 195, 25);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 196, 23);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 211, 25);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 212, 25);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 213, 23);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 222, 36);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 223, 23);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 226, 25);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 227, 25);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 228, 23);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 247, 19);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 248, 27);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 249, 20);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 250, 20);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 260, 23);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 263, 19);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 264, 19);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 278, 23);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 279, 23);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 280, 24);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 281, 24);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 291, 27);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 294, 31);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 295, 23);
        validateError(result, i++, ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 309, 20);
        validateError(result, i++, ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 310, 20);
        validateError(result, i++, ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 312, 17);
        validateError(result, i++, ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 325, 20);
        validateError(result, i++, ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 326, 20);
        validateError(result, i++, ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 328, 17);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 334, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 335, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 339, 5);
        validateError(result, i++, "invalid reference to 'self' outside a 'lock' statement in an 'isolated' object",
                      347, 13);
        validateError(result, i++, "invalid reference to 'self' outside a 'lock' statement in an 'isolated' object",
                      348, 9);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 374, 40);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 386, 27);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 388, 27);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 389, 22);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 390, 33);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 393, 31);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 396, 35);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 404, 35);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 430, 14);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 430, 17);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 431, 14);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 431, 18);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 431, 33);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 441, 14);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 441, 17);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 441, 25);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 442, 14);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 442, 17);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 456, 18);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 461, 36);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 461, 42);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 461, 45);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 462, 38);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 462, 59);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 463, 49);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 463, 71);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 468, 35);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 471, 36);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 471, 36);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 471, 42);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 471, 59);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 471, 59);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 471, 112);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 471, 115);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testIsolatedObjects() {
        CompileResult compileResult = BCompileUtil.compile("test-src/isolated-objects/isolated_objects.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
    }
}
