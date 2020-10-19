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
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 29, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 30, 5);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 45, 6);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 45, 6);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 53, 101);
        validateError(result, i++, "invalid non-private mutable field in an 'isolated' object", 53, 101);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 69, 39);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 72, 9);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 79, 16);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 89, 43);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 92, 13);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 99, 20);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 116, 30);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 122, 22);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 124, 23);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 124, 35);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 129, 18);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 135, 30);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 135, 42);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 136, 34);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 143, 22);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 164, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 165, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 166, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 175, 36);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 176, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 179, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 180, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 181, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 196, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 197, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 198, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 207, 36);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 208, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 211, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 212, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 213, 23);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 232, 19);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 233, 27);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 234, 20);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 235, 20);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 245, 23);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 248, 19);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 249, 19);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 263, 23);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 264, 23);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 265, 24);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 266, 24);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 276, 27);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 279, 31);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 280, 23);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 294, 20);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 295, 20);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 297, 17);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 310, 20);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 311, 20);
        validateError(result, i++, "invalid invocation of a non-isolated function in a method accessing a mutable " +
                "field of an 'isolated' object", 313, 17);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testIsolatedObjects() {
        CompileResult compileResult = BCompileUtil.compile("test-src/isolated-objects/isolated_objects.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
    }
}
