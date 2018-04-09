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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthnHandlerChainTest {
    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private String resourceRoot;
    private Path ballerinaConfCopyPath;

    @BeforeClass
    public void setup() throws Exception {
        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "auth");
        Path ballerinaConfPath = Paths
                .get(resourceRoot, "datafiles", "config", "auth", "configauthprovider", BALLERINA_CONF);
        ballerinaConfCopyPath = sourceRoot.resolve(BALLERINA_CONF);

        // Copy the ballerina.conf to the source root before starting the tests
        compileResult = BCompileUtil.compile(sourceRoot.resolve("authn-handler-chain-test.bal").toString());

        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(null, ballerinaConfPath.toString(), null);
    }

    @Test(description = "Test case for creating authn handler chain")
    public void testCreateAuthnHandlerChain() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateAuthnHandlerChain");
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertTrue(((BStruct) returns[0]) != null);
    }

    @Test(description = "Test case for authn handler chain authn failure scenario")
    public void testAuthFailure() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthFailure");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for authn handler chain authn success scenario")
    public void testAuthSuccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthSuccess");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @AfterClass
    public void tearDown() throws IOException {
        Files.deleteIfExists(ballerinaConfCopyPath);
    }
}
