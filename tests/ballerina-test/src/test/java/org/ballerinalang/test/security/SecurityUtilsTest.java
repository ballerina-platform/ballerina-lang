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
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SecurityUtilsTest {

    private static final Log log = LogFactory.getLog(SecurityUtilsTest.class);
    private CompileResult compileResult;

    private final String userDir = "user.dir";
    private String userDirectory;

    @BeforeClass
    public void setup() throws Exception {
        compileResult = BCompileUtil.compile("test-src/security/sec-utils.bal");
        printDiagnostics(compileResult);
        userDirectory = System.getProperty(userDir);
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(getRuntimeProperties());
        registry.loadConfigurations();
    }

    private void printDiagnostics(CompileResult timerCompileResult) {
        Arrays.asList(timerCompileResult.getDiagnostics()).
                forEach(e -> log.info(e.getMessage() + " : " + e.getPosition()));
    }

    private Map<String, String> getRuntimeProperties() {
        String ballerinaConf = "ballerina.conf";
        Map<String, String> runtimeConfigs = new HashMap<>();
        runtimeConfigs.put(ballerinaConf, getClass().getClassLoader()
                .getResource("datafiles/config/security/caching/" + ballerinaConf).getPath());
        return runtimeConfigs;
    }

    @Test
    public void testCreateDisabledBasicAuthCache () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateDisabledBasicAuthCache");
        Assert.assertTrue(returns != null);
        // auth cache is disabled, hence should be null
        Assert.assertTrue(returns[0] == null);
    }

    @Test
    public void testCreateAuthzCache () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateAuthzCache");
        Assert.assertTrue(returns != null);
        // authz cache is enabled, hence should not be null
        Assert.assertTrue(returns[0] != null);
    }

    @Test
    public void testExtractBasicAuthCredentialsFromInvalidHeader () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExtractBasicAuthCredentialsFromInvalidHeader");
        Assert.assertTrue(returns != null);
        Assert.assertTrue(returns[0].stringValue() == null);
        Assert.assertTrue(returns[1].stringValue() == null);
        // an error should be returned
        Assert.assertTrue(returns[2] != null);
    }

    @Test
    public void testExtractBasicAuthCredentials () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExtractBasicAuthCredentials");
        Assert.assertTrue(returns != null);
        // username and password should ne returned
        Assert.assertTrue(returns[0].stringValue() != null);
        Assert.assertTrue(returns[1].stringValue() != null);
        // no error should be returned
        Assert.assertTrue(returns[2] == null);
    }

    @Test
    public void testExtractInvalidBasicAuthHeaderValue () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExtractInvalidBasicAuthHeaderValue");
        Assert.assertTrue(returns != null);
        // basic auth header should be null
        Assert.assertTrue(returns[0].stringValue() == null);
        // an error should be returned
        Assert.assertTrue(returns[1] != null);
    }

    @Test
    public void testExtractBasicAuthHeaderValue () {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExtractBasicAuthHeaderValue");
        Assert.assertTrue(returns != null);
        // basic auth header should not be null
        Assert.assertTrue(returns[0].stringValue() != null);
        // no error should be returned
        Assert.assertTrue(returns[1] == null);
    }

    @AfterClass
    public void tearDown() throws ConfigFileParserException {
        System.setProperty(userDir, userDirectory);
    }
}
