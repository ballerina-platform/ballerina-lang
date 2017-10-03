/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.statements.reply;

import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for reply statement.
 */
public class ReplyStmtTest {
    private CompileResult result;

    @Test(description = "Test reply statement in a function")
    public void testReplyFromFunction() {
        result = BTestUtils.compile("test-src/statements/reply/reply-from-function.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BTestUtils.validateError(result, 0, "this function must return a result", 1, 0);

    }

    @Test(description = "Test reply statement in a action", enabled = false)
    public void testReplyFromAction() {
        result = BTestUtils.compile("test-src/statements/reply/reply-from-action.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BTestUtils.validateError(result, 0, "this function must return a result", 1, 0);
    }

}
