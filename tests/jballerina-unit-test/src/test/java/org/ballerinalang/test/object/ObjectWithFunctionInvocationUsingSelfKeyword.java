/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.object;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for function invocation using self keyword reference type in ballerina.
 */
public class ObjectWithFunctionInvocationUsingSelfKeyword {

    @Test(description = "Test the function invocation using self keyword when the fields are not initialized.")
    public void testSelfKeywordInvocationNegative() {

        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_self_function_invocation_negative" +
                ".bal");

        Assert.assertEquals(compileResult.getErrorCount(), 1);
        BAssertUtil.validateError(compileResult, 0, "all fields must be initialized", 5, 9);
    }
}
