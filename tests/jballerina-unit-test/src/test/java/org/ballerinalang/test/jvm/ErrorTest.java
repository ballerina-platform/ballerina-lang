/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.jvm;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test cases to cover error related tests on JBallerina.
 *
 * @since 0.995.0
 */
public class ErrorTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/errors.bal");
    }

    @Test(description = "Test panic an error", expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: reason foo 1 \\{\"message\":\"int value\"\\}\n\tat errors:foo\\" +
                    "(errors.bal:91\\)\n\t   errors:testPanic\\(errors.bal:20\\)")
    public void testPanic() {
        BRunUtil.invoke(compileResult, "testPanic", new Object[]{0});
    }

    @Test(description = "Test trap an error")
    public void testTrap() {
        BRunUtil.invoke(compileResult, "testTrap", new Object[]{0});
    }

    @Test(description = "Test handle errors of nested function calls with single trap")
    public void testNestedCallsWithSingleTrap() {
        // Run with zero integer input
        BRunUtil.invoke(compileResult, "testNestedCallsWithSingleTrap", new Object[]{0});

        // Run with non zero integer input
        BRunUtil.invoke(compileResult, "testNestedCallsWithSingleTrap", new Object[]{1});
    }

    @Test(description = "Test handle errors of nested function calls with trap expression for each function call")
    public void testNestedCallsWithAllTraps() {
        // Run with zero integer input
        BRunUtil.invoke(compileResult, "testNestedCallsWithAllTraps", new Object[]{0});

        // Run with non zero integer input
        BRunUtil.invoke(compileResult, "testNestedCallsWithSingleTrap", new Object[]{1});
    }

    @Test
    public void testSelfReferencingObject() {
        BRunUtil.invoke(compileResult, "testSelfReferencingError");
    }

    @Test(description = "Test runtime out of memory error", expectedExceptions = java.lang.RuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina}OutOfMemoryError \\{\"message\":\"Java heap " +
                    "space\"}.*")
    public void testRuntimeOOMError() {
        CompileResult compileResult = BCompileUtil.compile("test-src/jvm/runtime-oom-error.bal");
        final List<String> javaOpts = new ArrayList<>();
        javaOpts.add(0, "-Xms256m");
        javaOpts.add(1, "-Xmx256m");
        BRunUtil.runMain(compileResult, javaOpts);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
