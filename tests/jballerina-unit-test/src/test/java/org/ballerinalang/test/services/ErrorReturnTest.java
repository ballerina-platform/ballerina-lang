/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.ballerinalang.test.services;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

/**
 * Tests calling of a function with error return inside a service.
 */
public class ErrorReturnTest {

    @Test(description = "Tests invoking a function returning an error in a service")
    public void testErrorReturnFunction() {
        CompileResult compileResult = BCompileUtil.compile("test-src/services/error_return_function.bal");
        BRunUtil.invoke(compileResult, "testErrorFunction");
    }

    @Test(description = "Tests invoking a function with default worker returning an error union value in a service")
    public void testErrorReturnFunctionWithDistinctListenerArg() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/services/error_union_return_with_default_worker.bal");
        BRunUtil.invoke(compileResult, "testErrorUnionWithDefaultWorkerFunction");
    }
}
