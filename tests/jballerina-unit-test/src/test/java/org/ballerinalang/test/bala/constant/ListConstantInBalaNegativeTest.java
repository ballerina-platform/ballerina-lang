/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */
package org.ballerinalang.test.bala.constant;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative Bala test cases for list constants.
 *
 * @since 2201.9.0
 */

public class ListConstantInBalaNegativeTest {

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
    }

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/bala/test_bala/constant/list_constant_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected '[string,string,int...]', found '[\"a\",\"b\",\"c\"] & readonly'", 4, 34);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected 'int[]', found '[true,false,true] & readonly'", 5, 15);
        BAssertUtil.validateError(compileResult, i++,
                "cannot update 'readonly' value of type '[\"a\",\"b\",\"c\"] & readonly'", 10, 5);
        BAssertUtil.validateError(compileResult, i++,
                "cannot update 'readonly' value of type '[\"a\",\"b\",\"c\"] & readonly'", 11, 5);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected '(\"a\"|\"b\"|\"c\")', found 'string:Char'", 11, 12);
        BAssertUtil.validateError(compileResult, i++,
                "cannot update 'readonly' value of type '[1,\"f\",\"g\"] & readonly'", 14, 5);
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected '(1|\"f\"|\"g\")', found 'string'",
                14, 12);
        BAssertUtil.validateError(compileResult, i++, "undefined symbol 'l10'", 18, 17);
        BAssertUtil.validateError(compileResult, i++, "undefined symbol 'l11'", 19, 18);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }

}
