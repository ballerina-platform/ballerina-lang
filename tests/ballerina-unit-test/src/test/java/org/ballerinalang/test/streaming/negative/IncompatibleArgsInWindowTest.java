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
package org.ballerinalang.test.streaming.negative;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test the behavior when there are incompatible record attribute types.
 *
 * @since 0.982.0
 */
public class IncompatibleArgsInWindowTest {

    private CompileResult incompatibleArgsResult;
    private CompileResult notFoundResult;
    private CompileResult windowReturnResult;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        incompatibleArgsResult =
                BCompileUtil.compile("test-src/streaming/negative/incompatible-args-in-window-negative-test.bal");
        notFoundResult =
                BCompileUtil.compile("test-src/streaming/negative/window-func-not-found-negative-test.bal");
        windowReturnResult =
                BCompileUtil.compile("test-src/streaming/negative/incompatible-return-type-window-negative-test.bal");
    }

    @Test(description = "Checks if the args of window functions have correct types")
    public void testArgTypes() {
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertEquals(incompatibleArgsResult.getErrorCount(), 1);
        BAssertUtil.validateError(incompatibleArgsResult, 0,
                                  "incompatible types: expected 'int', found 'string'",
                                  62, 58);
    }

    @Test(description = "Checks whether the window function exists or not")
    public void testForWindowFunction() {
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertEquals(notFoundResult.getErrorCount(), 1);
        BAssertUtil.validateError(notFoundResult, 0,
                                  "undefined function 'nonExistingWindow'",
                                  62, 47);
    }

    @Test(description = "Checks whether the window function returns 'streams:Window' object")
    public void testWindowFunctionReturnType() {
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertEquals(windowReturnResult.getErrorCount(), 1);
        BAssertUtil.validateError(windowReturnResult, 0,
                                  "incompatible types: expected 'streams:Window', found 'Teacher'",
                                  67, 47);
    }
}
