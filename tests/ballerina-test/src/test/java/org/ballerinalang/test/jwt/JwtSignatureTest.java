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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

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

    private ConfigRegistry configRegistry;
    private String resourceRoot;
    private CompileResult compileResult;

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
}
