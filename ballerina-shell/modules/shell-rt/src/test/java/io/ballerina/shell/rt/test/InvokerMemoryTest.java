/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.rt.test;

import io.ballerina.shell.rt.InvokerMemory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test Invoker memory functions.
 *
 * @since 2.0.0
 */
public class InvokerMemoryTest {
    @Test
    public void testMainFunctionality() {
        String contextId = "UNIQUE.ID.1";
        String contextId2 = "UNIQUE.ID.2";
        Object var1 = new Object();
        Object var2 = new Object();
        Object var3 = new Object();
        InvokerMemory.memorize(contextId, "var1", var1);
        InvokerMemory.memorize(contextId, "var2", var2);
        InvokerMemory.memorize(contextId2, "var3", var3);

        // Correctly memorized
        Assert.assertEquals(InvokerMemory.recall(contextId, "var1"), var1);
        Assert.assertNotEquals(InvokerMemory.recall(contextId, "var1"), var2);
        Assert.assertNotEquals(InvokerMemory.recall(contextId, "var2"), var1);
        Assert.assertEquals(InvokerMemory.recall(contextId, "var2"), var2);

        // Not memorized in other ids
        Assert.assertNull(InvokerMemory.recall(contextId, "var3"));
        Assert.assertEquals(InvokerMemory.recall(contextId2, "var3"), var3);

        // Memorized same obj in diff ids
        InvokerMemory.memorize(contextId, "var3", var3);
        Assert.assertEquals(InvokerMemory.recall(contextId, "var3"), var3);

        // Forget in only one id
        InvokerMemory.forgetAll(contextId2);
        Assert.assertNull(InvokerMemory.recall(contextId2, "var3"));
        Assert.assertEquals(InvokerMemory.recall(contextId, "var3"), var3);

        // Forgotten in all ids
        InvokerMemory.forgetAll(contextId);
        Assert.assertNull(InvokerMemory.recall(contextId, "var3"));
        Assert.assertNull(InvokerMemory.recall(contextId2, "var3"));
    }
}
