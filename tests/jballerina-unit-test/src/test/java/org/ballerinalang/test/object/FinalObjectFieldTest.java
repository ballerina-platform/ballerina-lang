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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for `readonly` object fields.
 *
 * @since 2.0.0
 */
@Test(groups = { "brokenOnOldParser" })
public class FinalObjectFieldTest {

    @Test (enabled = false)
    public void testFinalObjectFields() {
        CompileResult result = BCompileUtil.compile("test-src/object/final_object_fields.bal");
        BRunUtil.invoke(result, "testFinalObjectFields");
    }

    @Test
    public void testFinalObjectFieldsSemanticsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/object/final_object_fields_semantics_negative.bal");
        int index = 0;

        validateError(result, index++, "incompatible types: expected '(Details & readonly)', found 'Details'", 48, 23);
        validateError(result, index++, "incompatible types: expected '(Foo & readonly)', found 'Bar'", 89, 25);
        validateError(result, index++, "incompatible types: expected '(Foo & readonly)', found 'Baz'", 90, 25);
        validateError(result, index++, "incompatible types: expected '(Foo & readonly)', found 'Qux'", 91, 25);
        validateError(result, index++, "incompatible types: expected 'Quux', found '" +
                "object { final int i; string s; final boolean b; }'", 107, 14);
        validateError(result, index++, "incompatible types: expected 'readonly', found 'Controller'", 122, 19);
        validateError(result, index++, "incompatible types: expected 'readonly', found 'object { final string id; " +
                "final map<int> config; }'", 133, 20);
        assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testFinalObjectFieldsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/object/final_object_fields_negative.bal");
        int index = 0;

        validateError(result, index++, "cannot update 'final' object field 'name'", 30, 5);
        validateError(result, index++, "cannot update 'final' object field 'details'", 56, 5);
        validateError(result, index++, "cannot update 'final' object field 'name'", 74, 5);
        validateError(result, index++, "cannot update 'final' object field 'name'", 91, 5);
        validateError(result, index++, "cannot update 'final' object field 'name'", 92, 5);
        assertEquals(result.getErrorCount(), index);
    }
}
