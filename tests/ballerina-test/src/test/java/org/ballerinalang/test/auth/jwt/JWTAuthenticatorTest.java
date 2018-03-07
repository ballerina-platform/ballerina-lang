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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test JWT authenticator.
 */
public class JWTAuthenticatorTest {

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

    private static final String KEY_STORE_CONFIG = "keyStore";
    private static final String KEY_STORE_LOCATION = "location";
    private static final String KEY_STORE_TYPE = "type";
    private static final String KEY_STORE_PASSWORD = "keyStorePassword";
    private static final String TRUST_STORE_CONFIG = "trustStore";
    private static final String TRUST_STORE_LOCATION = "location";
    private static final String TRUST_STORE_TYPE = "type";
    private static final String TRUST_STORE_PASSWORD = "trustStorePassword";

    private static final String JWT_AUTHENTICATOR_CONFIG = "authenticator_jwt";
    private static final String ISSUER = "issuer";
    private static final String AUDIENCE = "audience";
    private static final String CERTIFICATE_ALIAS = "certificateAlias";
    private static final String JWT_AUTH_CACHE_CONFIG = "jwt_auth_cache";
    private static final String ENABLED = "enabled";
    private static final String EXPIRY_TIME = "expiryTime";
    private static final String CAPACITY = "capacity";
    private static final String EVICTION_FACTOR = "evictionFactor";

    private ConfigRegistry initialConfigRegistry;
    private CompileResult compileResult;
    private String jwtToken;

    @BeforeClass
    public void setup() throws Exception {
        initialConfigRegistry = ConfigRegistry.getInstance();

        ConfigRegistry configRegistry = mock(ConfigRegistry.class);
        //KeyStore configurations
        when(configRegistry.getInstanceConfigValue(KEY_STORE_CONFIG, KEY_STORE_LOCATION))
                .thenReturn(getClass().getClassLoader().getResource(
                        "datafiles/security/keyStore/ballerinaKeystore.p12").getPath());
        when(configRegistry.getInstanceConfigValue(KEY_STORE_CONFIG, KEY_STORE_PASSWORD))
                .thenReturn("ballerina");
        when(configRegistry.getInstanceConfigValue(KEY_STORE_CONFIG, KEY_STORE_TYPE))
                .thenReturn("pkcs12");
        when(configRegistry.getInstanceConfigValue(TRUST_STORE_CONFIG, TRUST_STORE_LOCATION))
                .thenReturn(getClass().getClassLoader().getResource(
                        "datafiles/security/keyStore/ballerinaTruststore.p12").getPath());
        when(configRegistry.getInstanceConfigValue(TRUST_STORE_CONFIG, TRUST_STORE_PASSWORD))
                .thenReturn("ballerina");
        when(configRegistry.getInstanceConfigValue(TRUST_STORE_CONFIG, TRUST_STORE_TYPE))
                .thenReturn("pkcs12");
        //Authenticator configurations
        when(configRegistry.getInstanceConfigValue(JWT_AUTHENTICATOR_CONFIG, ISSUER))
                .thenReturn("wso2");
        when(configRegistry.getInstanceConfigValue(JWT_AUTHENTICATOR_CONFIG, AUDIENCE))
                .thenReturn("ballerina");
        when(configRegistry.getInstanceConfigValue(JWT_AUTHENTICATOR_CONFIG, CERTIFICATE_ALIAS))
                .thenReturn("ballerina");
        //Authenticator cache configurations
        when(configRegistry.getInstanceConfigValue(JWT_AUTH_CACHE_CONFIG, ENABLED))
                .thenReturn("true");
        when(configRegistry.getInstanceConfigValue(JWT_AUTH_CACHE_CONFIG, EXPIRY_TIME))
                .thenReturn("10000");
        when(configRegistry.getInstanceConfigValue(JWT_AUTH_CACHE_CONFIG, CAPACITY))
                .thenReturn("100");
        when(configRegistry.getInstanceConfigValue(JWT_AUTH_CACHE_CONFIG, EVICTION_FACTOR))
                .thenReturn("0.25");

        Field field = ConfigRegistry.class.getDeclaredField("configRegistry");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(ConfigRegistry.class, configRegistry);
        modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
        modifiersField.setAccessible(false);
        field.setAccessible(false);

        String resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "auth");
        compileResult = BCompileUtil.compile(sourceRoot.resolve("jwt-authenticator-test.bal").toString());

        //Generate a JWT token
        Path jwtIssuerSourceRoot = Paths.get(resourceRoot, "test-src", "jwt");
        CompileResult jwtIssuerCompileResult = BCompileUtil.compile(
                jwtIssuerSourceRoot.resolve("jwt-test.bal").toString());
        BValue[] returns = BRunUtil.invoke(jwtIssuerCompileResult, "testIssueJwt");
        jwtToken = returns[0].stringValue();
    }

    @Test(description = "Test case for creating JWT authenticator with a cache")
    public void testCreateJwtAuthenticatorWithCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJwtAuthenticatorCreationWithCache");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns[0] instanceof BStruct);
    }

    @Test(description = "Test JWT authentication success")
    public void testAuthenticationSuccess() {
        BValue[] inputBValues = {new BString(jwtToken)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationSuccess", inputBValues);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @AfterClass
    public void tearDown() throws Exception {
        Field field = ConfigRegistry.class.getDeclaredField("configRegistry");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(ConfigRegistry.class, initialConfigRegistry);
        modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
        modifiersField.setAccessible(false);
        field.setAccessible(false);
    }

}
