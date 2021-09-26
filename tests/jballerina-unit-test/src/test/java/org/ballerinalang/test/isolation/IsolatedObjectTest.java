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
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

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
            "invalid attempt to transfer out a value from a 'lock' statement with restricted variable usage: expected" +
                    " an isolated expression";
    private static final String ERROR_INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE =
            "invalid invocation of a non-isolated function in a 'lock' statement with restricted variable usage";

    private static final String ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC =
            "invalid access of mutable storage in an 'isolated' function";
    private static final String WARN_FUNCTION_SHOULD_RETURN_NIL = "this function should explicitly return a value";

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
        validateError(result, i++, "incompatible types: expected 'isolated object { }', " +
                "found 'object { final int[] x; final NonIsolatedClass y; }'", 96, 37);
        validateError(result, i++, "incompatible types: expected 'isolated object { int x; }', found 'object { " +
                "private int x; }'", 101, 42);
        validateError(result, i++, "incompatible types: expected 'isolated object { }', found 'object { private int " +
                "x; }'", 106, 38);
        validateError(result, i++, "incompatible types: expected 'isolated object { int x; }', found 'object { " +
                "private int x; }'", 109, 46);
        validateError(result, i++, "incompatible types: expected 'isolated function () returns ()', found 'function " +
                "() returns ()'", 118, 39);
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
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 55, 106);
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
        validateWarning(result, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 130, 39);
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
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 356, 40);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 368, 27);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 370, 27);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 371, 22);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 372, 33);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 375, 31);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 378, 35);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 386, 35);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 412, 14);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 412, 17);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 413, 14);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 413, 18);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 413, 33);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 423, 14);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 423, 17);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 423, 25);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 424, 14);
        validateError(result, i++, ERROR_INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 424, 17);
        validateError(result, i++, ERROR_EXPECTED_AN_ISOLATED_EXPRESSION, 438, 18);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 443, 36);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 443, 42);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 443, 45);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 444, 38);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 444, 59);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 445, 49);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 445, 71);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 450, 35);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 453, 36);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 453, 36);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 453, 42);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 453, 59);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 453, 59);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 453, 112);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 453, 115);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 459, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 460, 5);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 475, 40);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 487, 6);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 498, 5);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 501, 9);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 505, 9);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 509, 9);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 513, 9);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 517, 9);
        validateError(result, i++, "cannot access more than one variable for which usage is restricted in a single " +
                "'lock' statement", 522, 13);
        validateError(result, i++, "cannot access more than one variable for which usage is restricted in a single " +
                "'lock' statement", 523, 13);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 529, 5);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 532, 9);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 536, 9);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 540, 9);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 544, 9);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 548, 9);
        validateError(result, i++, "cannot access more than one variable for which usage is restricted in a single " +
                "'lock' statement", 553, 13);
        validateError(result, i++, "cannot access more than one variable for which usage is restricted in a single " +
                "'lock' statement", 554, 13);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 560, 5);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 563, 9);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 567, 9);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 571, 9);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 575, 9);
        validateError(result, i++, ERROR_INVALID_ACCESS_OF_MUTABLE_STORAGE_IN_ISOLATED_FUNC, 579, 9);
        validateError(result, i++, "cannot access more than one variable for which usage is restricted in a single " +
                "'lock' statement", 584, 13);
        validateError(result, i++, "cannot access more than one variable for which usage is restricted in a single " +
                "'lock' statement", 585, 13);
        validateError(result, i++, "cannot access more than one variable for which usage is restricted in a single " +
                "'lock' statement", 591, 13);
        validateError(result, i++, "cannot access more than one variable for which usage is restricted in a single " +
                "'lock' statement", 592, 13);
        validateWarning(result, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 599, 38);
        validateWarning(result, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 601, 44);
        validateWarning(result, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 603, 45);
        validateWarning(result, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 605, 57);
        validateWarning(result, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 607, 78);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 614, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 614, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 621, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 642, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 643, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 644, 24);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 652, 17);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 664, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 665, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 666, 24);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 667, 20);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 667, 20);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 673, 17);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 686, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 687, 17);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 688, 20);
        validateWarning(result, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 699, 50);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 702, 23);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 724, 20);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 735, 31);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 741, 62);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 747, 31);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 748, 31);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 748, 57);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 748, 70);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 759, 20);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 768, 33);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 774, 24);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 783, 48);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 788, 17);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 792, 17);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 796, 33);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 799, 13);
        validateError(result, i++, ERROR_INVALID_TRANSFER_OUT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 808, 20);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 817, 74);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 822, 62);
        validateError(result, i++, ERROR_INVALID_TRANSFER_IN_IN_LOCK_WITH_RESTRICTED_VAR_USAGE, 822, 71);
        Assert.assertEquals(result.getErrorCount(), i - 7);
        Assert.assertEquals(result.getWarnCount(), 7);
    }

    @Test
    public void testIsolatedObjects() {
        CompileResult compileResult = BCompileUtil.compile("test-src/isolated-objects/isolated_objects.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        BRunUtil.invoke(compileResult, "testRuntimeIsolatedFlag");
    }
}
