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
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Configuration auth provider testcase.
 */
public class InboundBasicAuthProviderTest {

    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private Path secretCopyFilePath;

    @BeforeClass
    public void setup() throws IOException {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path sourceRoot = Paths.get(resourceRoot, "test-src");
        Path ballerinaConfPath = Paths.get(resourceRoot, "datafiles", BALLERINA_CONF);

        compileResult = BCompileUtil.compile(sourceRoot.resolve("inbound_basic_auth_provider_test.bal").toString());

        String secretFile = "secret.txt";
        Path secretFilePath = Paths.get(resourceRoot, "datafiles", secretFile);
        String secretCopyFile = "secret-copy.txt";
        secretCopyFilePath = Paths.get(resourceRoot, "datafiles", secretCopyFile);
        copySecretFile(secretFilePath.toString(), secretCopyFilePath.toString());

        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(Collections.singletonMap("b7a.config.secret", secretCopyFilePath.toString()),
                ballerinaConfPath.toString(), null);
    }

    @Test(description = "Test case for creating inbound basic auth provider")
    public void testCreateInboundAuthProvider() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateInboundBasicAuthProvider");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns[0] instanceof BMap);
    }

    @Test(description = "Test case for authenticating non-existing user")
    public void testAuthenticationOfNonExistingUser() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationOfNonExistingUser");
        assertFailureOfResults(returns);
    }

    @Test(description = "Test case for authenticating with invalid password")
    public void testAuthenticationOfNonExistingPassword() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationOfNonExistingPassword");
        assertFailureOfResults(returns);
    }

    @Test(description = "Test case for successful authentication")
    public void testAuthentication() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthentication");
        assertSuccessOfResults(returns);
    }

    @Test(description = "Test case for unsuccessful authentication when username is empty")
    public void testAuthenticationWithEmptyUsername() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationWithEmptyUsername");
        assertFailureOfResults(returns);
    }

    @Test(description = "Test case for unsuccessful authentication when password is empty")
    public void testAuthenticationWithEmptyPassword() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationWithEmptyPassword");
        assertFailureOfResults(returns);
    }

    @Test(description = "Test case for unsuccessful authentication when password is empty and username is invalid")
    public void testAuthenticationWithEmptyPasswordAndInvalidUsername() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationWithEmptyPasswordAndInvalidUsername");
        assertFailureOfResults(returns);
    }

    @Test(description = "Test case for unsuccessful authentication when username and password are empty")
    public void testAuthenticationWithEmptyUsernameAndEmptyPassword() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationWithEmptyUsernameAndEmptyPassword");
        assertFailureOfResults(returns);
    }

    @Test(description = "Test case for successful authentication with sha-256 hashed password")
    public void testAuthenticationSha256() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationSha256");
        assertSuccessOfResults(returns);
    }

    @Test(description = "Test case for successful authentication with sha-384 hashed password")
    public void testAuthenticationSha384() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationSha384");
        assertSuccessOfResults(returns);
    }

    @Test(description = "Test case for successful authentication with sha-512 hashed password")
    public void testAuthenticationSha512() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationSha512");
        assertSuccessOfResults(returns);
    }

    @Test(description = "Test case for successful authentication with plain text password")
    public void testAuthenticationPlain() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationPlain");
        assertSuccessOfResults(returns);
    }

    @Test(description = "Test case for unsuccessful authentication with sha-512 hashed password, using invalid " +
            "password")
    public void testAuthenticationSha512Negative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationSha512Negative");
        assertFailureOfResults(returns);
    }

    @Test(description = "Test case for unsuccessful authentication with plain text password, using invalid password")
    public void testAuthenticationPlainNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationPlainNegative");
        assertFailureOfResults(returns);
    }

    @Test(description = "Test case for successful authentication with users from custom table name")
    public void testAuthenticationWithCustomTableName() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationWithCustomTableName");
        assertSuccessOfResults(returns);
    }

    @Test(description = "Test case for unsuccessful authentication with users from invalid table name")
    public void testAuthenticationWithNonExistingTableName() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationWithNonExistingTableName");
        assertFailureOfResults(returns);
    }

    @AfterClass
    public void tearDown() throws IOException {
        Files.deleteIfExists(secretCopyFilePath);
    }

    private void assertSuccessOfResults(BValue[] returns) {
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns[0] instanceof BBoolean && ((BBoolean) returns[0]).booleanValue());
    }

    private void assertFailureOfResults(BValue[] returns) {
        Assert.assertNotNull(returns);
        Assert.assertFalse(returns[0] instanceof BBoolean && ((BBoolean) returns[0]).booleanValue());
    }

    private void copySecretFile(String from, String to) throws IOException {
        Files.deleteIfExists(Paths.get(to));
        Files.copy(Paths.get(from), Paths.get(to));
    }
}
