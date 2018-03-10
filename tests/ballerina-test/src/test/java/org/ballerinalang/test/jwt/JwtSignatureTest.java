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

package org.ballerinalang.test.jwt;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
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
 * Test JWT token signing and signature verification functions.
 */
public class JwtSignatureTest {

    private static final String KEY_STORE_CONFIG = "keyStore";
    private static final String KEY_STORE_LOCATION = "location";
    private static final String KEY_STORE_TYPE = "type";
    private static final String KEY_STORE_PASSWORD = "keyStorePassword";
    private static final String TRUST_STORE_CONFIG = "trustStore";
    private static final String TRUST_STORE_LOCATION = "location";
    private static final String TRUST_STORE_TYPE = "type";
    private static final String TRUST_STORE_PASSWORD = "trustStorePassword";

    private ConfigRegistry initialConfigRegistry;
    private String resourceRoot;
    private CompileResult compileResult;

    @BeforeClass
    public void setup() throws Exception {
        initialConfigRegistry = ConfigRegistry.getInstance();

        ConfigRegistry configRegistry = mock(ConfigRegistry.class);
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

        Field field = ConfigRegistry.class.getDeclaredField("configRegistry");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(ConfigRegistry.class, configRegistry);
        modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
        modifiersField.setAccessible(false);
        field.setAccessible(false);

        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "jwt");
        compileResult = BCompileUtil.compile(sourceRoot.resolve("signature.bal").toString());
    }

    @Test(description = "Test case for JWT signature verification with valid signature")
    public void testVerifyJWTSignature() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testVerifyJWTSignature");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for JWT signing with valid data")
    public void testSignJWTData() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSignJWTData");
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
