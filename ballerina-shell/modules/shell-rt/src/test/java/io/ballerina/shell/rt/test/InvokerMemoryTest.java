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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

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

    @Test
    public void testPrinterr() {
        PrintStream origOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos, true, Charset.defaultCharset());
        System.setOut(out);
        InvokerMemory.printerr(new IllegalStateException("Error Object"));
        Assert.assertEquals(fixLineEnds(baos.toString()),
                "Exception occurred: java.lang.IllegalStateException: Error Object\n");
        System.setOut(origOut);
    }

    @Test
    public void testSprintf() {
        String result = InvokerMemory.sprintf("Hello %s %s!", "World", "Sunera");
        Assert.assertEquals(result, "Hello World Sunera!");
    }

    @Test
    public void testPrintln() {
        PrintStream origOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos, true, Charset.defaultCharset());
        System.setOut(out);
        InvokerMemory.println("Hello", 1, "no", false);
        Assert.assertEquals(fixLineEnds(baos.toString()), "Hello1nofalse\n");
        System.setOut(origOut);
    }

    private String fixLineEnds(String input) {
        return input.replace("\r\n", "\n");
    }
}
