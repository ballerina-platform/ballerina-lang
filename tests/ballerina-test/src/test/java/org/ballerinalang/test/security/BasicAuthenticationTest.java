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

public class BasicAuthenticationTest {
    private static final Log log = LogFactory.getLog(BasicAuthenticationTest.class);
    private CompileResult compileResult;

    private final String userDir = "user.dir";
    private String userDirectory;

    @BeforeClass
    public void setup() throws Exception {
        compileResult = BCompileUtil.compile("test-src/security/basic-authenticator-test.bal");
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
                .getResource("datafiles/config/security/basicauth/userstore/" + ballerinaConf).getPath());
        return runtimeConfigs;
    }

    private void printDiagnostics(CompileResult timerCompileResult) {
        Arrays.asList(timerCompileResult.getDiagnostics()).
                forEach(e -> log.info(e.getMessage() + " : " + e.getPosition()));
    }

    @Test
    public void testCreateBasicAuthenticatorWithoutCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBasicAuthenticatorCreationWithoutCache");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
        log.info(returns[0].stringValue());
        Assert.assertTrue(returns[1] != null);
        Assert.assertTrue(returns[2] == null);
    }

    @Test
    public void testCreateBasicAuthenticatorWithCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBasicAuthenticatorCreationWithCache");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0] instanceof BStruct);
        log.info(returns[0].stringValue());
        Assert.assertTrue(returns[1] != null);
        Assert.assertTrue(returns[2] != null);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateBasicAuthenticatorWithoutUserstore () {
        BRunUtil.invoke(compileResult, "testCreateBasicAuthenticatorWithoutUserstore");
    }

    @Test
    public void testAuthenticationForNonExistingUser () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationForNonExistingUser");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAuthenticationWithWrongPassword () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationWithWrongPassword");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAuthenticationSuccess () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthenticationSuccess");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @AfterClass
    public void tearDown() throws ConfigFileParserException {
        System.setProperty(userDir, userDirectory);
    }
}
