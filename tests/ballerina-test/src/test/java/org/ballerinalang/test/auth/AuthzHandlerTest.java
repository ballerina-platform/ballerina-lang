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


package org.ballerinalang.test.auth;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthzHandlerTest {
    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private String resourceRoot;

    @BeforeClass
    public void setup() throws Exception {
        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "auth");
        Path ballerinaConfPath = Paths
                .get(resourceRoot, "datafiles", "config", "auth", "authorization", "permissionstore", BALLERINA_CONF);

        compileResult = BCompileUtil.compile(sourceRoot.resolve("authz-handler-test.bal").toString());

        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(null, ballerinaConfPath.toString(), null);
    }

    @Test(description = "Test case for authorization failure")
    public void testHandleHttpAuthzFailure() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testHandleHttpAuthzFailure");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for authorization failure with no user set in authcontext")
    public void testHandleAuthzFailureWithNoUsernameInAuthContext() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testHandleAuthzFailureWithNoUsernameInAuthContext");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for canHandle failure with no user set in authcontext")
    public void testCanHandleAuthzFailureWithNoUsernameInAuthContext() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCanHandleAuthzFailureWithNoUsernameInAuthContext");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for canHandle success")
    public void testCanHandleAuthz() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCanHandleAuthz");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for authorization success")
    public void testHandleAuthz() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testHandleAuthz");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for authorization success with multiple scopes")
    public void testHandleAuthzWithMultipleScopes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testHandleAuthzWithMultipleScopes");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for extracting non-existing basic auth header value")
    public void testNonExistingBasicAuthHeaderValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNonExistingBasicAuthHeaderValue");
        Assert.assertTrue(returns != null);
        // basic auth header should be null
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test case for extracting basic auth header value")
    public void testExtractBasicAuthHeaderValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExtractBasicAuthHeaderValue");
        Assert.assertTrue(returns != null);
        // basic auth header should not be null
        Assert.assertEquals(returns[0].stringValue(), "Basic aXN1cnU6eHh4");
    }
}
