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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test cases related to isolated objects.
 *
 * @since 2.0.0
 */
public class IsolatedObjectTest {

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
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 118, 30);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 131, 18);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 131, 24);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 131, 36);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 134, 18);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 141, 30);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 141, 42);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 142, 34);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 157, 22);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 179, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 180, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 181, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 190, 36);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 191, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 194, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 195, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 196, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 211, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 212, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 213, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 222, 36);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 223, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 226, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 227, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 228, 23);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 247, 19);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 248, 27);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 249, 20);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 250, 20);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 260, 23);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 263, 19);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 264, 19);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 278, 23);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 279, 23);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 280, 24);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 281, 24);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 291, 27);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 294, 31);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 295, 23);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 309, 20);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 310, 20);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 312, 17);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 325, 20);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 326, 20);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 328, 17);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 334, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 335, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 339, 5);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 347, 13);
        validateError(result, i++, "invalid access of a mutable field of an 'isolated' object outside a 'lock' " +
                "statement", 348, 9);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 374, 40);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testIsolatedObjects() {
        CompileResult compileResult = BCompileUtil.compile("test-src/isolated-objects/isolated_objects.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
    }
}
