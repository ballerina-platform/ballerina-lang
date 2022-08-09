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
package org.ballerinalang.test.object;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test cases for distinct object type definitions.
 *
 * @since 2.0
 */
public class DistinctObjectTest {
    @Test
    public void testDistinctObjectAssignability() {
        CompileResult result = BCompileUtil.compile("test-src/object/distinct_objects_negative.bal");
        int index = 0;

        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Obj0'," +
                " found 'Obj2'", 55, 7);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Bar'," +
                " found 'object { int i; }'", 69, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Bar'," +
                " found 'object { int i; }'", 75, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'object {" +
                " int i; }', found 'object { int i; }'", 79, 11);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Bar'," +
                " found 'object { int i; }'", 82, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo'," +
                " found 'object { }'", 87, 14);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo2'," +
                " found 'object { }'", 88, 15);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo3'," +
                " found 'object { }'", 89, 15);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo4'," +
                " found 'object { }'", 90, 15);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo5'," +
                " found 'object { }'", 91, 15);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'object { }'," +
                " found 'Foo'", 93, 29);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'object { }'," +
                " found 'Foo2'", 94, 29);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'object { }'," +
                " found 'Foo3'", 95, 29);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'object { }'," +
                " found 'Foo4'", 96, 29);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'object { }'," +
                " found 'Foo5'", 97, 29);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo'," +
                " found 'Foo2'", 99, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo'," +
                " found 'Foo3'", 100, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo'," +
                " found 'Foo4'", 101, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo'," +
                " found 'Foo5'", 102, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo2'," +
                " found 'Foo'", 103, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo2'," +
                " found 'Foo3'", 104, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo2'," +
                " found 'Foo4'", 105, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo2'," +
                " found 'Foo5'", 106, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo3'," +
                " found 'Foo'", 107, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo3'," +
                " found 'Foo2'", 108, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo3'," +
                " found 'Foo4'", 109, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo3'," +
                " found 'Foo5'", 110, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo4'," +
                " found 'Foo'", 111, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo4'," +
                " found 'Foo2'", 112, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo4'," +
                " found 'Foo3'", 113, 10);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo4'," +
                " found 'Foo5'", 114, 10);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testDistinctObjectSubtyping() {
        CompileResult result = BCompileUtil.compile("test-src/object/distinct_objects_type_inclusion_test.bal");
        BRunUtil.invoke(result, "testDistinctObjectSubtyping");
    }
}
