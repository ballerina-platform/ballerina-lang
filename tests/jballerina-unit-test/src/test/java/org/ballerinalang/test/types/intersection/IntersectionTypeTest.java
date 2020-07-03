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
package org.ballerinalang.test.types.intersection;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for intersection types.
 *
 * @since 1.3.0
 */
public class IntersectionTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/intersection/test_intersection_type.bal");
    }

    @Test
    public void testImmutableTypes() {
        BRunUtil.invoke(result, "testIntersectionTypes");
    }

    @Test
    public void testImmutableTypesNegative() {
        CompileResult result = BCompileUtil.compile("test-src/types/intersection/test_intersection_type_negative.bal");
        int index = 0;

        validateError(result, index++, "invalid intersection type with 'readonly', 'future<int>' can never be " +
                "'readonly'", 19, 5);
        validateError(result, index++, "invalid intersection type 'json & int', intersection types are currently " +
                "supported only with 'readonly'", 23, 5);
        validateError(result, index++, "invalid intersection type '(Bar & readonly)': no intersection", 26, 45);
        validateError(result, index++, "invalid intersection type '(Baz & readonly)': no intersection", 32, 45);

        assertEquals(result.getErrorCount(), index);
    }
}
