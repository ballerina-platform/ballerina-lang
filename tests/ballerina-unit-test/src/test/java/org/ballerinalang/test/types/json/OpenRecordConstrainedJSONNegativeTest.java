/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.types.json;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for constraining json types with open records.
 *
 * @since 0.982.0
 */
public class OpenRecordConstrainedJSONNegativeTest {

    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        negativeResult = BCompileUtil.compile("test-src/types/jsontype/open_record_constrained_json_negative_test.bal");
    }

    @Test(description = "Test basic json struct constraint")
    public void testConstrainedJSONNegative() {
        Assert.assertEquals(negativeResult.getErrorCount(), 4);
        BAssertUtil.validateError(negativeResult, 0,
                "incompatible types: json cannot be constrained with open record type 'json'", 16, 5);
        BAssertUtil.validateError(negativeResult, 1, "invalid literal for type 'other'", 16, 22);
        BAssertUtil.validateError(negativeResult,
                2, "incompatible types: 'json' cannot be constrained with 'Student'", 20, 5);
        BAssertUtil.validateError(negativeResult, 3, "invalid literal for type 'other'", 20, 23);
    }
}
