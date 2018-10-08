/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.constant;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Compile time constant negative tests.
 */
public class CompileTimeConstantsNegativeTest {

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/" +
                "compile-time-constants-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 4);
        String expectedErrMsg = "only simple literals can be assigned to a compile time constant";
        BAssertUtil.validateError(compileResult, 0, expectedErrMsg, 2, 1);
        BAssertUtil.validateError(compileResult, 1, expectedErrMsg, 3, 1);
        BAssertUtil.validateError(compileResult, 2, expectedErrMsg, 6, 1);
        BAssertUtil.validateError(compileResult, 3, expectedErrMsg, 7, 1);
    }
}
