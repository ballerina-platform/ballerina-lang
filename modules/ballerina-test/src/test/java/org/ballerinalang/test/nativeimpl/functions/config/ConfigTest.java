/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.nativeimpl.functions.config;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.config.utils.ConfigFileParserException;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Native functions in ballerina.config.
 */
public class ConfigTest {

    private CompileResult compileResult;
    private final String ballerinaConf = "ballerina.conf";
    private final String userDir = "user.dir";
    private String userDirectory;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/config.bal");
        userDirectory = System.getProperty(userDir);
        System.setProperty(userDir, getClass().getClassLoader().getResource("datafiles/config/default").getPath());
    }

    @Test(description = "test global method with runtime and custom config file properties")
    public void testGetGlobalValuesWithRuntime() throws IOException {

        BString key = new BString("ballerina.http.host");
        BValue[] inputArg = {key};
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(getRuntimeProperties());
        registry.loadConfigurations();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetGlobalValues", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "10.100.1.201");
    }

    @Test(description = "test global method with default config file properties")
    public void testGetGlobalValuesWithDefaultConfigFile() throws IOException {

        BString key = new BString("ballerina.http.host");
        BValue[] inputArg = {key};
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(new HashMap<>());
        registry.loadConfigurations();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetGlobalValues", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "10.100.1.205");
    }

    @Test(description = "test global method with runtime, custom config and default file properties")
    public void testGetGlobalValuesWithAllProperties() throws IOException {

        BString key = new BString("ballerina.http.host");
        BValue[] inputArg = {key};
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(getRuntimeProperties());
        registry.loadConfigurations();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetGlobalValues", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "10.100.1.201");
    }

    @Test(description = "test get global method with unavailable config")
    public void testGetGlobalValuesNegative() throws IOException {

        BString key = new BString("ballerina.wso2");
        BValue[] inputArg = {key};
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(new HashMap<>());
        registry.loadConfigurations();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetGlobalValues", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertNull(returnVals[0].stringValue());
    }

    @Test(description = "test instance method with runtime and custom config file properties")
    public void testGetInstanceValuesWithRuntime() throws IOException {

        BString id = new BString("http1");
        BString key = new BString("ballerina.http.port");
        BValue[] inputArg = {id, key};
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(getRuntimeProperties());
        registry.loadConfigurations();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetInstanceValues", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "8082");
    }

    @Test(description = "test instance method with default config file properties")
    public void testGetInstanceValuesWithDefaultConfigFile() throws IOException {

        BString id = new BString("http1");
        BString key = new BString("ballerina.http.port");
        BValue[] inputArg = {id, key};
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(new HashMap<>());
        registry.loadConfigurations();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetInstanceValues", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "8085");
    }

    @Test(description = "test instance method with runtime, custom and default config file properties")
    public void testGetInstanceValuesWithAllProperties() throws IOException {

        BString id = new BString("http1");
        BString key = new BString("ballerina.http.port");
        BValue[] inputArg = {id, key};
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(getRuntimeProperties());
        registry.loadConfigurations();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetInstanceValues", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "8082");
    }

    @Test(description = "test get Instance method with unavailable config")
    public void testGetInstanceValuesNegative() throws IOException {

        BString id = new BString("http1");
        BString key = new BString("ballerina.wso2");
        BValue[] inputArg = {id, key};
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(new HashMap<>());
        registry.loadConfigurations();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetInstanceValues", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertNull(returnVals[0].stringValue());
    }

    @Test(description = "Test config entries with trailing whitespaces")
    public void testEntriesWithTrailingWhitespace() throws ConfigFileParserException {
        BString id = new BString("http3");
        BString key = new BString("ballerina.http.port");
        BValue[] inputArg = {id, key};
        ConfigRegistry registry = ConfigRegistry.getInstance();
        registry.initRegistry(new HashMap<>());
        registry.loadConfigurations();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testConfigsWithWhitespace", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "7070");
    }

    private Map<String, String> getRuntimeProperties() {
        Map<String, String> runtimeConfigs = new HashMap<>();
        runtimeConfigs.put(ballerinaConf, getClass().getClassLoader()
                .getResource("datafiles/config/ballerina.conf").getPath());
        runtimeConfigs.put("ballerina.http.host", "10.100.1.201");
        runtimeConfigs.put("[http1].ballerina.http.port", "8082");
        return runtimeConfigs;
    }

    @AfterClass
    public void tearDown() {
        System.setProperty(userDir, userDirectory);
    }
}
