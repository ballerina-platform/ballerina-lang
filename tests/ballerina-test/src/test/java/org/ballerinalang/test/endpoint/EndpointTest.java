/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.endpoint;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for Ballerina endpoint.
 *
 * @since 0.966.0
 */
public class EndpointTest {

    @Test(description = "Test endpoint testEndpoint In Function")
    public void testEndpointInFunction() {
        CompileResult testEndpointsInFunction = BCompileUtil.compile("test-src/endpoint/testEndpointInFunction.bal");

        BValue[] returns = BRunUtil.invoke(testEndpointsInFunction, "test1");
        Assert.assertTrue(returns.length == 1);
        Assert.assertEquals(returns[0].stringValue(), "init:DummyEndpoint;start:DummyEndpoint;" +
                "getClient:DummyEndpoint;invoke1:DummyClient;getClient:DummyEndpoint;invoke2:DummyClient;t2");

        returns = BRunUtil.invoke(testEndpointsInFunction, "test2");
        Assert.assertTrue(returns.length == 1);
        Assert.assertEquals(returns[0].stringValue(), "init:DummyEndpoint;start:DummyEndpoint;<test2Caller>" +
                "getClient:DummyEndpoint;invoke1:DummyClient;getClient:DummyEndpoint;invoke2:DummyClient;t2");

        returns = BRunUtil.invoke(testEndpointsInFunction, "test3");
        Assert.assertTrue(returns.length == 1);
        Assert.assertEquals(returns[0].stringValue(), "<test3>init:DummyEndpoint;start:DummyEndpoint;" +
                "getClient:DummyEndpoint;invoke1:DummyClient;getClient:DummyEndpoint;invoke2:DummyClient;t2");

        try {
            BRunUtil.invoke(testEndpointsInFunction, "test4");
        } catch (BLangRuntimeException e) {
            Assert.fail("NullReferenceException was thrown at test4 of EndpointTest.");
        }
    }

    @Test(description = "Test endpoint testEndpoint with Service")
    public void testEndpointWithService() {
        CompileResult testEndpointsInFunction = BCompileUtil.compile("test-src/endpoint/testEndpointWithService.bal");

        BValue[] returns = BRunUtil.invoke(testEndpointsInFunction, "test1");
        Assert.assertTrue(returns.length == 1);
        Assert.assertEquals(returns[0].stringValue(), "init:DummyEndpoint;register:DummyEndpoint;start:DummyEndpoint;" +
                "<test1>");
    }

    @Test(description = "Test anonymous endpoint testEndpoint with Service")
    public void testAnonymousEndpointWithService() {
        CompileResult testEndpointsInFunction = BCompileUtil.compile("test-src/endpoint/test_anonymous_endpoint.bal");

        BValue[] returns = BRunUtil.invoke(testEndpointsInFunction, "test1");
        Assert.assertTrue(returns.length == 1);
        Assert.assertEquals(returns[0].stringValue(), "init:DummyEndpoint;register:DummyEndpoint;start:DummyEndpoint;" +
                "<test1>");
    }

    @Test(description = "Test anonymous endpoint testEndpoint with Service")
    public void testAnonymousEndpointNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/endpoint/test_anonymous_endpoint_negative.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(compileResult, 0, "undefined field 'confX' in struct 'DummyEndpointConfig'", 62, 39);
    }

}
