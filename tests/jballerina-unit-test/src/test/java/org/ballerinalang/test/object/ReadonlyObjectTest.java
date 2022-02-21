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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for `readonly object`s.
 *
 * @since 2.0.0
 */
public class ReadonlyObjectTest {

    @Test(dataProvider = "readOnlyObjectTests")
    public void testReadonlyObjects(String function) {
        CompileResult result = BCompileUtil.compile("test-src/object/readonly_objects.bal");
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "readOnlyObjectTests")
    public Object[] readOnlyObjectTests() {
        return new Object[]{
                "testBasicReadOnlyObject",
                "testInvalidReadOnlyObjectUpdateAtRuntime",
                "testReadOnlyObjectsForImmutableIntersections1",
                "testReadOnlyObjectsForImmutableIntersections2",
                "testReadOnlyServiceClass",
                "testReadOnlyClassIntersectionWithMismatchedQualifiersRuntimeNegative",
                "testReadOnlyClassIntersectionWithValidQualifiers",
                "testRecursiveObjectArrayReadonlyClone"
        };
    }

    @Test
    public void testReadonlyObjectsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/object/readonly_objects_negative.bal");
        int index = 0;

        validateError(result, index++, "incompatible types: expected '(Details & readonly)', found 'Details'", 32, 24);
        validateError(result, index++, "incompatible types: expected '(Department & readonly)', found 'Department'",
                      33, 21);
        validateError(result, index++, "cannot update 'readonly' value of type 'Employee'", 38, 9);
        validateError(result, index++, "cannot update 'readonly' value of type 'Employee'", 39, 9);
        validateError(result, index++, "cannot initialize abstract object '(Controller & readonly)'", 55, 31);
        validateError(result, index++, "invalid 'readonly object' 'InvalidReadOnlyObject': cannot have fields that " +
                "are never 'readonly'", 58, 1);
        validateError(result, index++, "incompatible types: expected 'int[] & readonly', found 'int[]'", 71, 18);
        validateError(result, index++, "incompatible types: expected '(Foo & readonly)', found 'Bar'", 102, 31);
        validateError(result, index++, "incompatible types: expected '(Foo & readonly)', found 'Baz'", 105, 35);
        assertEquals(result.getErrorCount(), index);
    }
}
