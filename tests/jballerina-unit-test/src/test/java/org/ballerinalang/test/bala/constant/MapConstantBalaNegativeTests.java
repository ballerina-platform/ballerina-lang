/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.bala.constant;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative Bala test cases for mapping constants.
 *
 * @since 2.0.1
 */
public class MapConstantBalaNegativeTests {

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
    }

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/bala/test_bala/constant/map_constant_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected 'record {| 1 a; |}', found " +
                "'testorg/foo:1.0.0:(testorg/foo:1:$anonType$_190 & readonly)'", 20, 27);
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected 'record {| readonly (" +
                "record {| 1 a; 2 b; |} & readonly) a; readonly (record {| 3 a; |} & readonly) b; |} & readonly', " +
                "found 'testorg/foo:1.0.0:(testorg/foo:1:$anonType$_191 & readonly)'", 21, 80);
        BAssertUtil.validateError(compileResult, i++, "cannot update 'readonly' value of type " +
                "'testorg/foo:1.0.0:(testorg/foo:1:$anonType$_190 & readonly)'", 26, 5);
        BAssertUtil.validateError(compileResult, i++, "cannot update 'readonly' value of type " +
                "'testorg/foo:1.0.0:(testorg/foo:1:$anonType$_190 & readonly)'", 27, 5);
        BAssertUtil.validateError(compileResult, i++, "cannot update 'readonly' value of " +
                                          "type 'testorg/foo:1.0.0:(testorg/foo:1:$anonType$_191 & readonly)'",
                                  30, 5);
        BAssertUtil.validateError(compileResult, i++, "cannot update 'readonly' value of type " +
                                          "'testorg/foo:1.0.0:(testorg/foo:1:$anonType$_191 & readonly)'",
                                  31, 5);
        BAssertUtil.validateError(compileResult, i++, "cannot update 'readonly' value of type " +
                                          "'testorg/foo:1.0.0:(testorg/foo:1:$anonType$_191 & readonly)'",
                                  32, 5);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }
}
