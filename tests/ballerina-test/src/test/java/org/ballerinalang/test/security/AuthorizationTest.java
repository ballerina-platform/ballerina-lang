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

package org.ballerinalang.test.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.config.utils.ConfigFileParserException;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationTest {
    private static final Log log = LogFactory.getLog(AuthorizationTest.class);
    private CompileResult compileResult;

    private final String userDir = "user.dir";
    private String userDirectory;

    @BeforeClass
    public void setup() throws Exception {
        compileResult = BCompileUtil.compile("test-src/security/authorization-test.bal");
        printDiagnostics(compileResult);
        userDirectory = System.getProperty(userDir);
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(getRuntimeProperties());
        registry.loadConfigurations();
    }

    private Map<String, String> getRuntimeProperties() {
        String ballerinaConf = "ballerina.conf";
        Map<String, String> runtimeConfigs = new HashMap<>();
        runtimeConfigs.put(ballerinaConf, getClass().getClassLoader()
                .getResource("datafiles/config/security/authorization/permissionstore/" +
                        ballerinaConf).getPath());
        return runtimeConfigs;
    }

    private void printDiagnostics(CompileResult timerCompileResult) {
        Arrays.asList(timerCompileResult.getDiagnostics()).
                forEach(e -> log.info(e.getMessage() + " : " + e.getPosition()));
    }

    @Test
    public void testCreateAuthzCheckerWithoutCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthzCheckerCreationWithoutCache");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
        log.info(returns[0].stringValue());
        Assert.assertTrue(returns[1] != null);
        Assert.assertTrue(returns[2] == null);
    }

    @Test
    public void testCreateAuthzCheckerWithCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthzCheckerCreationWithCache");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
        log.info(returns[0].stringValue());
        Assert.assertTrue(returns[1] != null);
        Assert.assertTrue(returns[2] != null);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateBasicAuthenticatorWithoutPermissionstore () {
        BRunUtil.invoke(compileResult, "testCreateBasicAuthenticatorWithoutPermissionstore");
    }

    @Test
    public void testAuthorizationForNonExistingUser () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthorizationForNonExistingUser");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAuthorizationForNonExistingScope () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthorizationForNonExistingScope");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAuthorizationSuccess () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthorizationSuccess");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @AfterClass
    public void tearDown() throws ConfigFileParserException {
        System.setProperty(userDir, userDirectory);
    }
}
