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
package org.ballerinalang.stdlib.streams.negative;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
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
        incompatibleArgsResult =
                BCompileUtil.compile("test-src/negative/incompatible-args-in-window-negative-test.bal");
        notFoundResult =
                BCompileUtil.compile("test-src/negative/window-func-not-found-negative-test.bal");
        windowReturnResult =
                BCompileUtil.compile("test-src/negative/incompatible-return-type-window-negative-test.bal");
    }

    @Test(description = "Checks if the args of window functions have correct types",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*Time window expects an int parameter.*")
    public void testArgTypes() {
        BRunUtil.invoke(incompatibleArgsResult, "startTimeWindowTest");
    }

    @Test(description = "Checks whether the window function exists or not")
    public void testForWindowFunction() {
        Assert.assertEquals(notFoundResult.getErrorCount(), 1);
        BAssertUtil.validateError(notFoundResult, 0, "undefined function 'nonExistingWindow'", 62, 47);
    }

    @Test(description = "Checks whether the window function returns 'streams:Window' object")
    public void testWindowFunctionReturnType() {
        Assert.assertEquals(windowReturnResult.getErrorCount(), 1);
        BAssertUtil.validateError(windowReturnResult, 0,
                                  "incompatible types: expected 'streams:Window', found 'Teacher'",
                                  67, 47);
    }
}
