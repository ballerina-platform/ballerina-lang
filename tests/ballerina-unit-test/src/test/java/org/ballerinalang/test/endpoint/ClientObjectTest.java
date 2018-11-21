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
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Client Object and Remote function related test cases.
 *
 * @since 0.985.0
 */
public class ClientObjectTest {

    private CompileResult remoteBasic;

    @BeforeClass
    public void setupRemoteBasic() {
        remoteBasic = BCompileUtil.compile("test-src/endpoint/new/remote_basic.bal");
    }

    @Test
    public void testRemoteFunctions() {
        BValue[] returns = BRunUtil.invoke(remoteBasic, "test1", new BValue[] { new BInteger(4) });
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "false");

        returns = BRunUtil.invoke(remoteBasic, "test1", new BValue[] { new BInteger(10) });
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test
    public void testFunctions() {
        BValue[] returns = BRunUtil.invoke(remoteBasic, "test2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "5");
    }

    @Test
    public void testReferringEndpointInDifferentPkg() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/endpoint", "pkg.bc");

        BValue[] result = BRunUtil.invoke(compileResult, "testCheck");
        Assert.assertEquals(result.length, 1);
        Assert.assertEquals(result[0].stringValue(), "i1 {}");

        result = BRunUtil.invoke(compileResult, "testNewEP", new BValue[] { new BString("done") });
        Assert.assertEquals(result.length, 1);
        Assert.assertEquals(result[0].stringValue(), "donedone");
    }

    @Test
    public void testRemoteBasicsNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/new/remote_basic_negative.bal");
        int errIdx = 0;
        BAssertUtil.validateError(compileResult, errIdx++,
                "remote modifier not allowed in non-object attached function test1", 22, 1);
        BAssertUtil.validateError(compileResult, errIdx++,
                "remote modifier not allowed in non-object attached function test2", 26, 1);
        BAssertUtil.validateError(compileResult, errIdx++,
                "remote modifier not allowed in non-object attached function test3", 30, 1);

        BAssertUtil.validateError(compileResult, errIdx++, "attempt to refer non-remote function abc as remote", 45, 1);
        BAssertUtil.validateError(compileResult, errIdx++, "remote modifier required here", 50, 1);
        BAssertUtil
                .validateError(compileResult, errIdx++, "invalid remote function invocation syntax, use '->' operator",
                        57, 13);
        BAssertUtil
                .validateError(compileResult, errIdx++, "undefined remote function 'abc' in client object Foo", 59, 13);

        BAssertUtil.validateError(compileResult, errIdx++, "unknown type 'XXX'", 65, 5);
        BAssertUtil
                .validateError(compileResult, errIdx++, "invalid remote function invocation, expected an client object",
                        67, 13);
        BAssertUtil
                .validateError(compileResult, errIdx++, "invalid remote function invocation, expected an client object",
                        71, 9);
        BAssertUtil
                .validateError(compileResult, errIdx++, "invalid remote function invocation, expected an client object",
                        75, 9);
        BAssertUtil.validateError(compileResult, errIdx++,
                "client object declaration not allowed here, declare at the top of a function or at module level", 89,
                9);
        BAssertUtil.validateError(compileResult, errIdx++,
                "client object declaration not allowed here, declare at the top of a function or at module level", 97,
                9);

        BAssertUtil
                .validateError(compileResult, errIdx++, "invalid remote function invocation syntax, use '->' operator",
                        112, 13);
        BAssertUtil
                .validateError(compileResult, errIdx++, "invalid remote function invocation syntax, use '->' operator",
                        120, 13);
        BAssertUtil.validateError(compileResult, errIdx++,
                "client object declaration not allowed here, declare at the top of a function or at module level", 126,
                5);
        BAssertUtil.validateError(compileResult, errIdx++,
                "client object declaration not allowed here, declare at the top of a function or at module level", 134,
                5);
        BAssertUtil.validateError(compileResult, errIdx++, "variable 'ep' is not initialized", 142, 12);
        BAssertUtil.validateError(compileResult, errIdx++, "variable 'ep' is not initialized", 149, 13);

        BAssertUtil.validateError(compileResult, errIdx++, "a remote function in a non client object", 154, 5);
        BAssertUtil.validateError(compileResult, errIdx++, "a remote function in a non client object", 163, 5);
        BAssertUtil
                .validateError(compileResult, errIdx++, "client objects requires at least one remote function", 170, 5);

        Assert.assertEquals(compileResult.getErrorCount(), errIdx);
    }
}
