/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.jwt;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Test JWT auth provider.
 */
public class JwtAuthProviderTest {

    /**
     * #JWT auth provider configurations.
     * [auth_provider_jwt]
     * issuer=&lt;jwt token issuer>
     * audience=&lt;audience>
     * certificateAlias=&lt;public certificate of the issuer>
     * <p>
     * #JWT auth provider cache configuration
     * [jwt_auth_cache]
     * enabled=&lt;true of false>
     * expiryTime=&lt;expiry time in milliseconds>
     * capacity=&lt;capacity eg: 100>
     * evictionFactor=&lt;evictionFactor eg: 0.25>
     * <p>
     * #Keystore configuration
     * [keyStore]
     * location=&lt;keyStore location>
     * type=&lt;keystore type eg: PKCS12>
     * keyStorePassword=&lt;keystore password>
     * keyAlias=&lt;default private key alias>
     * keyPassword=&lt;default private key password>
     * <p>
     * #Truststore configuration
     * [trustStore]
     * location=&lt;trustStore location>
     * type=&lt;trustStore type eg: PKCS12>
     * trustStorePassword=&lt;trustStore password>
     */

    private Path ballerinaKeyStoreCopyPath;
    private Path ballerinaTrustStoreCopyPath;
    private CompileResult compileResult;
    private String resourceRoot;
    private String jwtToken;
    private String trustStorePath;
    private String keyStorePath;
    private static final String BALLERINA_CONF = "ballerina.conf";
    private static final String KEY_STORE = "ballerinaKeystore.p12";
    private static final String TRUST_SORE = "ballerinaTruststore.p12";

    @BeforeClass
    public void setup() throws Exception {
        trustStorePath = getClass().getClassLoader().getResource(
                "datafiles/keystore/ballerinaTruststore.p12").getPath();
        keyStorePath = getClass().getClassLoader().getResource(
                "datafiles/keystore/ballerinaKeystore.p12").getPath();
        resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path sourceRoot = Paths.get(resourceRoot, "test-src");
        Path ballerinaConfPath = Paths.get(resourceRoot, "datafiles", "config", BALLERINA_CONF);
        Path ballerinaKeyStorePath = Paths.get(resourceRoot, "datafiles", "keystore", KEY_STORE);
        ballerinaKeyStoreCopyPath = sourceRoot.resolve(KEY_STORE);
        Path ballerinaTrustStorePath = Paths.get(resourceRoot, "datafiles", "keystore", TRUST_SORE);
        ballerinaTrustStoreCopyPath = sourceRoot.resolve(TRUST_SORE);
        // Copy test resources to source root before starting the tests
        Files.copy(ballerinaKeyStorePath, ballerinaKeyStoreCopyPath, new CopyOption[]{REPLACE_EXISTING});
        Files.copy(ballerinaTrustStorePath, ballerinaTrustStoreCopyPath, new CopyOption[]{REPLACE_EXISTING});

        compileResult = BCompileUtil.compile(sourceRoot.resolve("jwt-auth-provider-test.bal").toString());
        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(getRuntimeProperties(), ballerinaConfPath.toString(), null);
    }

    @Test(description = "Test JWT issuer", priority = 1)
    private void testGenerateJwt() {
        BValue[] inputBValues = {new BString(keyStorePath)};
        BValue[] returns = BRunUtil.invoke(compileResult, "generateJwt", inputBValues);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[0].stringValue().startsWith("eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QifQ.eyJzdWIiO" +
                "iJKb2huIiwgImlzcyI6IndzbzIiLCAiZXhwIjozMjQ3NTI1MTE4OTAwMCwgImF1ZCI6ImJhbGxlcmluYSJ9."));

        jwtToken = returns[0].stringValue();
    }

    @Test(description = "Test JWT verification", priority = 2)
    private void testVerifyJwt() {
        BValue[] inputBValues = {new BString(jwtToken), new BString(trustStorePath)};
        BValue[] returns = BRunUtil.invoke(compileResult, "verifyJwt", inputBValues);
        Assert.assertTrue(returns[0] instanceof BMap);
    }

    @Test(description = "Test case for creating JWT auth provider with a cache", priority = 2)
    public void testCreateJwtAuthProvider() {
        BValue[] inputBValues = {new BString(trustStorePath)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateJwtAuthProvider", inputBValues);
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns[0] instanceof BMap);
    }

    @Test(description = "Test case for JWT auth provider for authentication success", priority = 2)
    public void testJwtAuthProviderAuthenticationSuccess() {
        BValue[] inputBValues = {new BString(jwtToken), new BString(trustStorePath)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testJwtAuthProviderAuthenticationSuccess", inputBValues);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @AfterClass
    public void tearDown() throws IOException {
        Files.deleteIfExists(ballerinaKeyStoreCopyPath);
        Files.deleteIfExists(ballerinaTrustStoreCopyPath);
    }

    private Map<String, String> getRuntimeProperties() {
        Map<String, String> runtimeConfigs = new HashMap<>();
        runtimeConfigs.put(BALLERINA_CONF,
                Paths.get(resourceRoot, "datafiles", "config", "jwt", BALLERINA_CONF).toString());
        return runtimeConfigs;
    }
}
