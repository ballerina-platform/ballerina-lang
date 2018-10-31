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
 * Constant negative tests.
 */
public class ConstantNegativeTest {

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/constant-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 11);
        String expectedErrMsg1 = "only simple literals can be assigned to a constant";
        String expectedErrMsg2 = "cannot assign a value to a constant";
        int index = 0;
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 2, 21);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 3, 29);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 6, 13);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 7, 21);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg2, 13, 5);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg2, 14, 5);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg2, 19, 9);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'", 26, 21);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 28, 18);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'abc'", 32, 1);
        BAssertUtil.validateError(compileResult, index, "redeclared symbol 'def'", 37, 5);
    }
}
