/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.action.start;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Start action negative test cases.
 */
public class StartActionTest {

    @Test (description = "Test negative start action usage")
    public void testStartActionNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/action/start/start-action-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 8);
        BAssertUtil.validateError(negativeResult, 0, "action invocation as an expression not allowed here", 37, 23);
        BAssertUtil.validateError(negativeResult, 1, "action invocation as an expression not allowed here", 38, 38);
        BAssertUtil.validateError(negativeResult, 2, "action invocation as an expression not allowed here", 39, 43);
        BAssertUtil.validateError(negativeResult, 3, "action invocation as an expression not allowed here", 56, 37);
        BAssertUtil.validateError(negativeResult, 4, "action invocation as an expression not allowed here", 58, 49);
        BAssertUtil.validateError(negativeResult, 5, "action invocation as an expression not allowed here", 71, 17);
        BAssertUtil.validateError(negativeResult, 6, "action invocation as an expression not allowed here", 72, 28);
        BAssertUtil.validateError(negativeResult, 7, "action invocation as an expression not allowed here", 76, 25);
    }
}
