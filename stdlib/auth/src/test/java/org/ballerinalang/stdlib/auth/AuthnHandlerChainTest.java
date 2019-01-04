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

package org.ballerinalang.stdlib.auth;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Authentication handler chain Testcase.
 */
public class AuthnHandlerChainTest {

    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private Path secretCopyPath;

    @BeforeClass
    public void setup() throws IOException {
        String resourceRoot = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
                .getAbsolutePath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src");
        Path ballerinaConfPath = Paths.get(resourceRoot, "datafiles", "config", "authprovider", BALLERINA_CONF);

        // Copy the ballerina.conf to the source root before starting the tests
        compileResult = BCompileUtil.compile(sourceRoot.resolve("authn-handler-chain-test.bal").toString());

        String secretFile = "secret.txt";
        Path secretFilePath = Paths.get(resourceRoot, "datafiles", "config", secretFile);
        secretCopyPath = Paths.get(resourceRoot, "datafiles", "config", "authprovider", secretFile);
        Files.deleteIfExists(secretCopyPath);
        copySecretFile(secretFilePath.toString(), secretCopyPath.toString());

        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(Collections.singletonMap("b7a.config.secret", secretCopyPath.toString()),
                ballerinaConfPath.toString(), null);
    }

    private void copySecretFile(String from, String to) throws IOException {
        Files.copy(Paths.get(from), Paths.get(to));
    }

    @Test(description = "Test case for creating authn handler chain")
    public void testCreateAuthnHandlerChain() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateAuthnHandlerChain");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertNotNull(returns[0]);
    }

    @Test(description = "Test case for authn handler chain authn failure scenario")
    public void testAuthFailure() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthFailure");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for authn handler chain authn failure scenario with specific handlers")
    public void testAuthFailureWithSpecificHandlers() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthFailureWithSpecificHandlers");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for authn handler chain authn success scenario")
    public void testAuthSuccessWithSpecificHandlers() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthSuccessWithSpecificHandlers");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for authn handler chain authn success scenario with specific handlers")
    public void testAuthSuccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthSuccess");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @AfterClass
    public void tearDown() throws IOException {
        Files.deleteIfExists(secretCopyPath);
    }
}
