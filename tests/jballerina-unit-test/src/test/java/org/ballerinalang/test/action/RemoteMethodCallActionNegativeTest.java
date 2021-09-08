/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.action;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test case for invalid usage of remote method call action.
 *
 * @since 2.0
 */
public class RemoteMethodCallActionNegativeTest {

    @Test
    public void testInvalidUsageOfRemoteMethodCallAction() {
        CompileResult result = BCompileUtil.compile("test-src/action/remote_method_call_action_negative.bal");
        int i = 0;
        validateError(result, i++, "undefined module 'wss'", 27, 27);
        validateError(result, i++, "missing identifier", 28, 1);
        validateError(result, i++, "invalid token 'checkpanic'", 28, 4);
        validateError(result, i++, "action invocation as an expression not allowed here", 28, 16);
        validateError(result, i++, "missing comma token", 28, 16);
        validateError(result, i++, "missing close parenthesis token", 28, 90);
        assertEquals(result.getErrorCount(), i);
    }
}
