/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.error;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test case for Error StackTrace.
 *
 * @since 2.0.0
 */
public class ErrorStackTraceTest {

    private CompileResult compileResult;
    private CompileResult compileResultWithInterop;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/error/stacktrace-project");
        compileResultWithInterop = BCompileUtil.compile("test-src/error/stacktrace_test_with_interop.bal");
    }

    @Test
    public void testStackTraceElements() {
        BRunUtil.invoke(compileResult, "testStackTraceElements");
    }

    @Test
    public void testErrorStacktraceWithInterop() {
        Exception expectedException = null;
        try {
            BRunUtil.invoke(compileResultWithInterop, "testErrorStacktraceWithInterop");
        } catch (Exception e) {
            expectedException = e;
        }

        Assert.assertNotNull(expectedException);
        String message = expectedException.getMessage();
        Assert.assertEquals(message, "error: error!!!\n" +
                "\tat stacktrace_test_with_interop:errorStacktraceTest(stacktrace_test_with_interop.bal:23)\n" +
                "\t   stacktrace_test_with_interop:" +
                "testErrorStacktraceWithInterop(stacktrace_test_with_interop.bal:20)");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }

}
