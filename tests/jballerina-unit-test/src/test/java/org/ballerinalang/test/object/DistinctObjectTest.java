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
                " found 'Obj2'", 51, 7);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Foo'," +
                " found 'object { }'", 60, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Bar'," +
                " found 'object { int i; }'", 70, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Bar'," +
                " found 'object { int i; }'", 76, 13);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testDistinctObjectSubtyping() {
        CompileResult result = BCompileUtil.compile("test-src/object/distinct_objects_type_inclusion_test.bal");
        BRunUtil.invoke(result, "testDistinctObjectSubtyping");
    }
}
