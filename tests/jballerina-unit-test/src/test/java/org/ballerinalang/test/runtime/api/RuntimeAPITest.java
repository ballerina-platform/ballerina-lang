/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.runtime.api;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

/**
 * Test cases for runtime api.
 *
 * @since 2.0.0
 */
public class RuntimeAPITest {

    @Test
    public void valueCreatorTest() {
        CompileResult result = BCompileUtil.compile("test-src/runtime/api/values");
        BRunUtil.invoke(result, "main");
    }

    @Test
    public void errorCreatorTest() {
        CompileResult result = BCompileUtil.compile("test-src/runtime/api/errors");
        BRunUtil.invoke(result, "main");
    }

    @Test
    public void methodTypeTest() {
        CompileResult result = BCompileUtil.compile("test-src/runtime/api/types");
        BRunUtil.invoke(result, "main");
    }

    @Test
    public void asyncCallTest() {
        CompileResult result = BCompileUtil.compile("test-src/runtime/api/async");
        BRunUtil.invoke(result, "main");
    }

    @Test
    public void utilsTest() {
        CompileResult result = BCompileUtil.compile("test-src/runtime/api/util");
        BRunUtil.invoke(result, "main");
    }
}
