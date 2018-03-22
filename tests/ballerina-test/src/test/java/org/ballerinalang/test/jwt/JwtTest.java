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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test JWT token creation and verification.
 */
public class JwtTest {

    private static final String KEY_STORE_CONFIG = "keyStore";
    private static final String KEY_STORE_LOCATION = "location";
    private static final String KEY_STORE_TYPE = "type";
    private static final String KEY_STORE_PASSWORD = "keyStorePassword";
    private static final String TRUST_STORE_CONFIG = "trustStore";
    private static final String TRUST_STORE_LOCATION = "location";
    private static final String TRUST_STORE_TYPE = "type";
    private static final String TRUST_STORE_PASSWORD = "trustStorePassword";

    private ConfigRegistry configRegistry;
    private String resourceRoot;
    private CompileResult compileResult;
    private String jwtToken;

    @BeforeClass
    public void setup() throws Exception {
        configRegistry = ConfigRegistry.getInstance();

        configRegistry.addConfiguration(KEY_STORE_CONFIG, KEY_STORE_LOCATION, getClass().getClassLoader().getResource(
                "datafiles/security/keyStore/ballerinaKeystore.p12").getPath());
        configRegistry.addConfiguration(KEY_STORE_CONFIG, KEY_STORE_PASSWORD, "ballerina");
        configRegistry.addConfiguration(KEY_STORE_CONFIG, KEY_STORE_TYPE, "pkcs12");

        configRegistry.addConfiguration(TRUST_STORE_CONFIG, TRUST_STORE_LOCATION,
                getClass().getClassLoader().getResource(
                        "datafiles/security/keyStore/ballerinaTruststore.p12").getPath());
        configRegistry.addConfiguration(TRUST_STORE_CONFIG, TRUST_STORE_PASSWORD, "ballerina");
        configRegistry.addConfiguration(TRUST_STORE_CONFIG, TRUST_STORE_TYPE, "pkcs12");

        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "jwt");
        compileResult = BCompileUtil.compile(sourceRoot.resolve("jwt-test.bal").toString());
    }

    @Test(priority = 1, description = "Test case for issuing JWT token with valid data")
    public void testIssueJwt() throws Exception {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIssueJwt");
        Assert.assertTrue(returns[0] instanceof BString);
        jwtToken = returns[0].stringValue();
    }

    @Test(priority = 2, description = "Test case for validating JWT token")
    public void testValidateJwt() throws Exception {
        BValue[] inputBValues = {new BString(jwtToken)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testValidateJwt", inputBValues);
        Assert.assertTrue((returns[0]) instanceof BBoolean);
    }

    @AfterClass
    public void tearDown() throws Exception {
        Field field = ConfigRegistry.class.getDeclaredField("configRegistry");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(ConfigRegistry.class, configRegistry);
        modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
        modifiersField.setAccessible(false);
        field.setAccessible(false);
    }
}
