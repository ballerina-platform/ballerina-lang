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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.endpoint;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Endpoint related test cases.
 *
 * @since 0.985.0
 */
public class NewEndpointTest {

    @Test
    public void testRemoteBasicsNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/remote_basic_negative.bal");
        int errIndex = 0;
        BAssertUtil.validateError(compileResult, errIndex++,
                "remote modifier not allowed in non-object attached function test1", 2, 1);
        BAssertUtil.validateError(compileResult, errIndex++,
                "remote modifier not allowed in non-object attached function test2", 6, 1);
        BAssertUtil.validateError(compileResult, errIndex++,
                "remote modifier not allowed in non-object attached function test3", 10, 1);

        BAssertUtil
                .validateError(compileResult, errIndex++, "attempt to refer non-remote function abc as remote", 25, 1);
        BAssertUtil.validateError(compileResult, errIndex++, "remote modifier required here", 30, 1);
        BAssertUtil.validateError(compileResult, errIndex++,
                "invalid remote function invocation syntax, use '->' operator", 37, 13);
        BAssertUtil.validateError(compileResult, errIndex++, "undefined remote function 'abc' in endpoint Foo", 39, 13);

        BAssertUtil.validateError(compileResult, errIndex++, "unknown type 'XXX'", 45, 5);
        BAssertUtil.validateError(compileResult, errIndex++, "invalid remote function invocation, expected an endpoint",
                47, 13);
        BAssertUtil.validateError(compileResult, errIndex++, "invalid remote function invocation, expected an endpoint",
                51, 9);
        BAssertUtil.validateError(compileResult, errIndex++, "invalid remote function invocation, expected an endpoint",
                55, 9);
        BAssertUtil.validateError(compileResult, errIndex++,
                "endpoint declaration not allowed here, declare at the top of a function or at module level", 69, 9);
        BAssertUtil.validateError(compileResult, errIndex++,
                "endpoint declaration not allowed here, declare at the top of a function or at module level", 77, 9);

        BAssertUtil.validateError(compileResult, errIndex++,
                "invalid remote function invocation syntax, use '->' operator", 92, 13);
        BAssertUtil.validateError(compileResult, errIndex++,
                "invalid remote function invocation syntax, use '->' operator", 100, 13);
        BAssertUtil.validateError(compileResult, errIndex++,
                "endpoint declaration not allowed here, declare at the top of a function or at module level", 106, 5);
        BAssertUtil.validateError(compileResult, errIndex++,
                "endpoint declaration not allowed here, declare at the top of a function or at module level", 114, 5);

        Assert.assertEquals(compileResult.getErrorCount(), errIndex);
    }
}
