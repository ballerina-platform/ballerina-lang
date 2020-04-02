/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.globalvar;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Global variable error scenarios.
 */
public class GlobalVarNegativeTest {

    @Test
    public void testGlobalVarNegatives() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/statements/variabledef/global_variable_negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 6);
        BAssertUtil.validateError(resultNegative, 0, "invalid token 'int'", 27, 8);
        BAssertUtil.validateError(resultNegative, 1, "invalid token 'int'", 29, 8);
        BAssertUtil.validateError(resultNegative, 2, "mismatched input ';'. expecting '='", 31, 32);
        BAssertUtil.validateError(resultNegative, 3, "mismatched input ';'. expecting '='", 33, 27);
        BAssertUtil.validateError(resultNegative, 4, "mismatched input ';'. expecting '='", 35, 46);
        BAssertUtil.validateError(resultNegative, 5, "mismatched input ';'. expecting '='", 37, 59);
    }
}
