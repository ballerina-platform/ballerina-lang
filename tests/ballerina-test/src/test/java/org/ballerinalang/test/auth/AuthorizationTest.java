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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthorizationTest {
    private static final Log log = LogFactory.getLog(AuthorizationTest.class);

    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private String resourceRoot;

    @BeforeClass
    public void setup() throws Exception {
        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "auth");
        Path ballerinaConfPath = Paths
                .get(resourceRoot, "datafiles", "config", "auth", "authorization", "permissionstore", BALLERINA_CONF);

        compileResult = BCompileUtil.compile(sourceRoot.resolve("authorization-test.bal").toString());

        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(null, ballerinaConfPath.toString(), null);
    }

    @Test(description = "Test case for creating authz checker without a cache")
    public void testCreateAuthzCheckerWithoutCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthzCheckerCreationWithoutCache");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
        log.info(returns[0].stringValue());
        Assert.assertTrue(returns[1] != null);
        Assert.assertTrue(returns[2] == null);
    }

    @Test(description = "Test case for creating authz checker with cache")
    public void testCreateAuthzCheckerWithCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthzCheckerCreationWithCache");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
        log.info(returns[0].stringValue());
        Assert.assertTrue(returns[1] != null);
        Assert.assertTrue(returns[2] != null);
    }

//    @Test(description = "Test case for creating authz checker without permission store", expectedExceptions =
//            BLangRuntimeException.class)
//    public void testAuthzCheckerWithoutPermissionstore() {
//        BRunUtil.invoke(compileResult, "testAuthzCheckerWithoutPermissionstore");
//    }

    @Test(description = "Test case for checking authorization for non existing user")
    public void testAuthorizationForNonExistingUser() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthorizationForNonExistingUser");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for checking authorization for non existing scope")
    public void testAuthorizationForNonExistingScope() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthorizationForNonExistingScope");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for checking authorization success")
    public void testAuthorizationSuccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthorizationSuccess");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }
}
