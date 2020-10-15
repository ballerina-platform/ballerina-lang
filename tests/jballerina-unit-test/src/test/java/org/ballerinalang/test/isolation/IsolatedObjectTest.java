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
 * @since Swan Lake
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
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 62, 39);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 65, 9);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 72, 16);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 82, 43);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 85, 13);
        validateError(result, i++,
                      "invalid access of a mutable field of an 'isolated' object outside a 'lock' statement", 92, 20);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 109, 30);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 115, 22);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 117, 23);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 117, 35);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 122, 18);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 128, 30);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 128, 42);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 129, 34);
        validateError(result, i++, "invalid initial value expression: expected a unique expression", 136, 22);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 157, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 158, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 159, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 168, 36);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 169, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 172, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 173, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 174, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 189, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 190, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 191, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 200, 36);
        validateError(result, i++, "invalid attempt to copy out a mutable value from an 'isolated' object", 201, 23);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 204, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 205, 25);
        validateError(result, i++, "invalid attempt to copy a mutable value into an 'isolated' object", 206, 23);
        Assert.assertEquals(result.getErrorCount(), i);
    }
}
