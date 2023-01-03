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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for runtime api.
 *
 * @since 2.0.0
 */
public class RuntimeAPITest {

    @Test(dataProvider = "packageNameProvider")
    public void testRuntimeAPIs(String packageName) {
        CompileResult result = BCompileUtil.compile("test-src/runtime/api/" + packageName);
        BRunUtil.invoke(result, "main");
    }

    @DataProvider
    public Object[] packageNameProvider() {
        return new String[]{
                "values",
                "errors",
                "types",
                "invalid_values",
                "async",
                "utils",
                "stop_handler",
                "identifier_utils"
        };
    }
}
