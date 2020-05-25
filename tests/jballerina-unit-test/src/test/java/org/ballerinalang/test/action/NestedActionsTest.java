/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for nested actons.
 *
 * @since 1.3.0
 */
public class NestedActionsTest {

    @Test
    public void testNestedClientObjectActions() {
        CompileResult result = BCompileUtil.compile("test-src/action/nested_actions.bal");
        BRunUtil.invoke(result, "testNestedClientObjectActions");
    }

    @Test
    public void testNegatives() {
        CompileResult result = BCompileUtil.compile("test-src/action/nested_actions_negative.bal");
        validateError(result, 0, "action invocation as an expression not allowed here", 35, 33);
        validateError(result, 1, "action invocation as an expression not allowed here", 35, 45);
        assertEquals(result.getErrorCount(), 2);
    }
}
