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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BasicAuthenticationTest {
    private static final Log log = LogFactory.getLog(BasicAuthenticationTest.class);
    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private String resourceRoot;

    @BeforeClass
    public void setup() throws Exception {
        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "auth");
        Path ballerinaConfPath = Paths
                .get(resourceRoot, "datafiles", "config", "auth", "basicauth", "userstore", BALLERINA_CONF);

        compileResult = BCompileUtil.compile(sourceRoot.resolve("basic-authenticator-test.bal").toString());

        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(null, ballerinaConfPath.toString(), null);
    }

    @Test(description = "Test case for creating basic authenticator without a cache")
    public void testCreateBasicAuthenticatorWithoutCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBasicAuthenticatorCreationWithoutCache");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
        log.info(returns[0].stringValue());
        Assert.assertTrue(returns[1] != null);
        Assert.assertTrue(returns[2] == null);
    }

    @Test(description = "Test case for creating basic authenticator with a cache")
    public void testCreateBasicAuthenticatorWithCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBasicAuthenticatorCreationWithCache");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
        log.info(returns[0].stringValue());
        Assert.assertTrue(returns[1] != null);
        Assert.assertTrue(returns[2] != null);
    }


    @Test(description = "Test case for authenticating non-existing user")
    public void testAuthenticationForNonExistingUser() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationForNonExistingUser");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for authenticaing with invalid password")
    public void testAuthenticationWithWrongPassword() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationWithWrongPassword");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test username in authcontext with authentication failure")
    public void testUsernameInAuthContextWithAuthenticationFailure() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUsernameInAuthContextWithAuthenticationFailure");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue((returns[0]).stringValue().isEmpty());
    }

    @Test(description = "Test authentication success")
    public void testAuthenticationSuccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationSuccess");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test username in authcontext with successful authentication")
    public void testUsernameInAuthContextWithAuthenticationSuccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUsernameInAuthContextWithAuthenticationSuccess");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals((returns[0]).stringValue(), "isuru");
    }
}
