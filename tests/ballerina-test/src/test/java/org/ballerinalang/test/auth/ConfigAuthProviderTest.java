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
import org.ballerinalang.model.values.BStringArray;
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
import java.util.Collections;

public class ConfigAuthProviderTest {
    private static final Log log = LogFactory.getLog(ConfigAuthProviderTest.class);
    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private String resourceRoot;
    private String secretFile = "secret.txt";
    private Path secretCopyPath;

    @BeforeClass
    public void setup() throws Exception {
        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "auth");
        Path ballerinaConfPath = Paths
                .get(resourceRoot, "datafiles", "config", "auth", "configauthprovider", BALLERINA_CONF);

        compileResult = BCompileUtil.compile(sourceRoot.resolve("config_auth_provider_test.bal").toString());

        Path secretFilePath = Paths.get(resourceRoot, "datafiles", "config", secretFile);
        secretCopyPath = Paths.get(resourceRoot, "datafiles", "config", "auth", "configauthprovider",
                secretFile);
        Files.deleteIfExists(secretCopyPath);
        copySecretFile(secretFilePath.toString(), secretCopyPath.toString());

        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(Collections.singletonMap("ballerina.config.secret", secretCopyPath.toString()),
                ballerinaConfPath.toString(), null);
    }

    private void copySecretFile (String from, String to) throws IOException {
        Files.copy(Paths.get(from), Paths.get(to));
    }

    @Test(description = "Test case for creating file based userstore")
    public void testCreateConfigAuthProvider() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateConfigAuthProvider");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
    }

    @Test(description = "Test case for authenticating non-existing user")
    public void testAuthenticationOfNonExistingUser() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationOfNonExistingUser");
        Assert.assertTrue(returns != null);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for authenticating with invalid password")
    public void testAuthenticationOfNonExistingPassword() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationOfNonExistingPassword");
        Assert.assertTrue(returns != null);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for successful authentication")
    public void testAuthentication() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthentication");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for reading groups of non-existing user")
    public void testReadScopesOfNonExistingUser() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadScopesOfNonExistingUser");
        Assert.assertTrue(returns != null);
        Assert.assertEquals(((BStringArray) returns[0]).size(), 0);
    }

    @Test(description = "Test case for reading groups of a user")
    public void testReadScopesOfUser() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReadScopesOfUser");
        Assert.assertTrue(returns != null);
        BStringArray groups = ((BStringArray) returns[0]);
        Assert.assertEquals(groups.size(), 1);

        Assert.assertEquals(groups.get(0), "scope1");
    }

    @AfterClass
    public void tearDown() throws IOException {
        Files.deleteIfExists(secretCopyPath);
    }
}
