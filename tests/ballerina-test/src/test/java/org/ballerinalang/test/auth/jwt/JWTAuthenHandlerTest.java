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

package org.ballerinalang.test.auth.jwt;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.internal.jwt.crypto.JWSSigner;
import org.ballerinalang.nativeimpl.internal.jwt.crypto.RSASigner;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Test Http JWT authentication handler.
 */
public class JWTAuthenHandlerTest {

    /**
     * #JWT Authenticator configurations
     * [authenticator_jwt]
     * issuer=<jwt token issuer>
     * audience=<audience>
     * certificateAlias=<public certificate of the issuer>
     * <p>
     * #JWT Authenticator cache configuration
     * [jwt_auth_cache]
     * enabled=<true of false>
     * expiryTime=<expiry time in milliseconds>
     * capacity=<capacity eg: 100>
     * evictionFactor=<evictionFactor eg: 0.25>
     * <p>
     * #Keystore configuration
     * [keyStore]
     * location=<keyStore location>
     * type=<keystore type eg: PKCS12>
     * keyStorePassword=<keystore password>
     * keyAlias=<default private key alias>
     * keyPassword=<default private key password>
     * <p>
     * #Truststore configuration
     * [trustStore]
     * location=<trustStore location>
     * type=<trustStore type eg: PKCS12>
     * trustStorePassword=<trustStore password>
     */

    private Path ballerinaKeyStoreCopyPath;
    private Path ballerinaTrustStoreCopyPath;
    private CompileResult compileResult;
    private String resourceRoot;
    private String jwtToken;
    private String trustStorePath;
    private static final String BALLERINA_CONF = "ballerina.conf";
    private static final String KEY_STORE = "ballerinaKeystore.p12";
    private static final String TRUST_SORE = "ballerinaTruststore.p12";

    @BeforeClass
    public void setup() throws Exception {
        trustStorePath = getClass().getClassLoader().getResource(
                "datafiles/security/keyStore/ballerinaTruststore.p12").getPath();
        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "auth");
        Path ballerinaConfPath = Paths
                .get(resourceRoot, "datafiles", "config", "auth", "jwt", BALLERINA_CONF);
        Path ballerinaKeyStorePath = Paths
                .get(resourceRoot, "datafiles", "security", "keyStore", KEY_STORE);
        ballerinaKeyStoreCopyPath = sourceRoot.resolve(KEY_STORE);
        Path ballerinaTrustStorePath = Paths
                .get(resourceRoot, "datafiles", "security", "keyStore", TRUST_SORE);
        ballerinaTrustStoreCopyPath = sourceRoot.resolve(TRUST_SORE);
        // Copy test resources to source root before starting the tests
        Files.copy(ballerinaKeyStorePath, ballerinaKeyStoreCopyPath, new CopyOption[]{REPLACE_EXISTING});
        Files.copy(ballerinaTrustStorePath, ballerinaTrustStoreCopyPath, new CopyOption[]{REPLACE_EXISTING});

        compileResult = BCompileUtil.compile(sourceRoot.resolve("jwt-authn-handler-test.bal").toString());
        // load configs
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(getRuntimeProperties(), ballerinaConfPath.toString(), null);

        jwtToken = generateJWT();
    }

    @Test(description = "Test case for JWT auth interceptor canHandle method, without the bearer header")
    public void testCanHandleHttpJwtAuthWithoutHeader() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCanHandleHttpJwtAuthWithoutHeader");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for JWT auth interceptor canHandle method")
    public void testCanHandleHttpJwtAuth() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCanHandleHttpJwtAuth");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for JWT auth interceptor authentication failure")
    public void testHandleHttpJwtAuthFailure() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testHandleHttpJwtAuthFailure");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for JWT auth interceptor authentication success")
    public void testHandleHttpJwtAuth() {
        BValue[] inputBValues = {new BString(jwtToken), new BString(trustStorePath)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testHandleHttpJwtAuth", inputBValues);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @AfterClass
    public void tearDown() throws Exception {
        Files.deleteIfExists(ballerinaKeyStoreCopyPath);
        Files.deleteIfExists(ballerinaTrustStoreCopyPath);
    }

    String generateJWT() throws Exception {
        String header = buildHeader();
        String jwtHeader = new String(Base64.getUrlEncoder().encode(header.getBytes()));
        String body = buildBody();
        String jwtBody = new String(Base64.getUrlEncoder().encode(body.getBytes()));
        String assertion = jwtHeader + "." + jwtBody;
        String algorithm = "RS256";
        PrivateKey privateKey = getPrivateKey();
        JWSSigner signer = new RSASigner(privateKey);
        String signature = signer.sign(assertion, algorithm);
        return assertion + "." + signature;
    }

    private PrivateKey getPrivateKey() throws Exception {
        KeyStore keyStore;
        InputStream file = new FileInputStream(new File(getClass().getClassLoader().getResource(
                "datafiles/security/keyStore/ballerinaKeystore.p12").getPath()));
        keyStore = java.security.KeyStore.getInstance("pkcs12");
        keyStore.load(file, "ballerina".toCharArray());
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("ballerina", new KeyStore
                .PasswordProtection("ballerina".toCharArray()));
        return pkEntry.getPrivateKey();
    }

    String buildHeader() {
        return "{\n" +
                "  \"alg\": \"RS256\",\n" +
                "  \"typ\": \"JWT\"\n" +
                "}";
    }

    String buildBody() {
        long time = System.currentTimeMillis() + 10000000;
        return "{\n" +
                "  \"sub\": \"John\",\n" +
                "  \"iss\": \"wso2\",\n" +
                "  \"aud\": \"ballerina\",\n" +
                "  \"scope\": \"John test Doe\",\n" +
                "  \"roles\": [\"admin\",\"admin2\"],\n" +
                "  \"exp\": " + time + "\n" +
                "}";
    }

    private Map<String, String> getRuntimeProperties() {
        Map<String, String> runtimeConfigs = new HashMap<>();
        runtimeConfigs.put(BALLERINA_CONF,
                Paths.get(resourceRoot, "datafiles", "config", "auth", "jwt", BALLERINA_CONF).toString());
        return runtimeConfigs;
    }

}
