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
package org.ballerinalang.stdlib.config;

import org.ballerinalang.config.ConfigProcessor;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Native functions in ballerina.config.
 */
public class ConfigTest {

    private static final ConfigRegistry registry = ConfigRegistry.getInstance();
    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private String resourceRoot;
    private Path sourceRoot;
    private Path ballerinaConfPath;
    private String customConfigFilePath;

    @BeforeClass
    public void setup() {
        resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        sourceRoot = Paths.get(resourceRoot, "test-src");
        ballerinaConfPath = Paths.get(resourceRoot, "datafiles", "default", BALLERINA_CONF);
        customConfigFilePath = Paths.get(resourceRoot, "datafiles", BALLERINA_CONF).toString();

        compileResult = BCompileUtil.compile(sourceRoot.resolve("config.bal").toString());
    }

    @Test(description = "test global method with runtime and custom config file properties")
    public void testGetAsStringWithRuntime() throws IOException {
        BString key = new BString("ballerina.http.host");
        BValue[] inputArg = {key};

        registry.initRegistry(getRuntimeProperties(), customConfigFilePath, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "10.100.1.201");
    }

    @Test(description = "test global method with default config file properties")
    public void testGetAsStringWithDefaultConfigFile() throws IOException {
        BString key = new BString("\"ballerina.http.host\"");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "10.100.1.205");
    }

    @Test(description = "test global method with runtime, custom config and default file properties")
    public void testGetAsStringWithAllProperties() throws IOException {
        BString key = new BString("ballerina.http.host");
        BValue[] inputArg = {key};

        registry.initRegistry(getRuntimeProperties(), customConfigFilePath, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "10.100.1.201");
    }

    @Test(description = "test get global method with unavailable config")
    public void testGetAsStringNegative() throws IOException {
        BString key = new BString("ballerina.wso2");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString || returnVals[0] == null);
        Assert.assertEquals(returnVals[0].stringValue(), "");
    }

    @Test(description = "test instance method with runtime and custom config file properties")
    public void testGetInstanceValuesWithRuntime() throws IOException {
        BString key = new BString("http1.port");
        BValue[] inputArg = {key};

        registry.initRegistry(getRuntimeProperties(), customConfigFilePath, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "8082");
    }

    @Test(description = "test instance method with default config file properties")
    public void testGetInstanceValuesWithDefaultConfigFile() throws IOException {
        BString key = new BString("http1.port");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "8085");
    }

    @Test(description = "test instance method with runtime, custom and default config file properties")
    public void testGetInstanceValuesWithAllProperties() throws IOException {
        BString key = new BString("http1.port");
        BValue[] inputArg = {key};

        registry.initRegistry(getRuntimeProperties(), customConfigFilePath, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "8082");
    }

    @Test(description = "test get Instance method with unavailable config")
    public void testGetInstanceValuesNegative() throws IOException {
        BString key = new BString("http1.ballerina.wso2");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString || returnVals[0] == null);
    }

    @Test(description = "Test config entries with trailing whitespaces")
    public void testEntriesWithTrailingWhitespace() throws IOException {
        BString key = new BString("http3.port");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "7070");
    }

    @Test(description = "Test setting configs from Ballerina source code")
    public void testSetConfig() throws IOException {
        BString key = new BString("ballerina.http.host");
        BString value = new BString("ballerinalang.org");
        BValue[] inputArgs = {key, value};

        registry.initRegistry(getRuntimeProperties(), customConfigFilePath, ballerinaConfPath);

        BRunUtil.invoke(compileResult, "testSetConfig", inputArgs);

        // The config we set in Ballerina code should overwrite the configs set from other sources
        Assert.assertEquals(registry.getAsString(key.stringValue()), value.stringValue());
    }

    @Test(description = "Test contains() method")
    public void testContains() throws IOException {
        BString key = new BString("\"ballerina.http.host\"");

        registry.initRegistry(null, null, ballerinaConfPath);

        BValue[] inputArg = {key};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testContains", inputArg);

        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue());
    }

    @Test(description = "Test for getAsInt")
    public void testGetAsInt() throws IOException {
        BString key = new BString("http1.port");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsInt", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), (long) 8085);
    }

    @Test(description = "Test for getAsFloat")
    public void testGetAsFloat() throws IOException {
        BString key = new BString("http1.eviction_factor");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsFloat", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returnVals[0]).floatValue(), 0.3455);
    }

    @Test(description = "Test for getAsFloat for scientific notation")
    public void testGetAsFloat2() throws IOException {
        BString key = new BString("expirationPeriod");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsFloat", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returnVals[0]).floatValue(), 1800000.0D);
    }

    @Test(description = "Test for getting an int as a float")
    public void testGetIntAsFloat() throws IOException {
        BString key = new BString("http1.request_limit");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsFloat", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returnVals[0]).floatValue(), 50.0D);
    }

    @Test(description = "Test for getAsBoolean")
    public void testGetAsBoolean() throws IOException {
        BString key = new BString("http1.caching_enabled");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsBoolean", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue());
    }

    @Test(description = "Test for non-string types as CLI params")
    public void testNonStringCLIParams() throws IOException {
        Map<String, String> runtimeParams = new HashMap<>();
        runtimeParams.put("host", "localhost");
        runtimeParams.put("port", "7777");
        runtimeParams.put("trace.enabled", "true");
        runtimeParams.put("evic_factor", "0.23333");

        registry.initRegistry(runtimeParams, null, null);

        BValue[] inputArg = {new BString("host")};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "localhost");

        inputArg = new BValue[]{new BString("port")};
        returnVals = BRunUtil.invoke(compileResult, "testGetAsInt", inputArg);
        Assert.assertTrue(returnVals[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 7777);

        inputArg = new BValue[]{new BString("trace.enabled")};
        returnVals = BRunUtil.invoke(compileResult, "testGetAsBoolean", inputArg);
        Assert.assertTrue(returnVals[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue());

        inputArg = new BValue[]{new BString("evic_factor")};
        returnVals = BRunUtil.invoke(compileResult, "testGetAsFloat", inputArg);
        Assert.assertTrue(returnVals[0] instanceof BFloat);
        Assert.assertEquals(((BFloat) returnVals[0]).floatValue(), 0.23333);
    }

    @Test(description = "Test \\ in the config value string")
    public void testBackwardSlashInString() throws IOException {
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        BValue[] inputArg = {new BString("\"path.test\"")};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);
        Assert.assertEquals(returnVals[0].stringValue(), "C:\\Users\\John");
    }

    @Test(description = "Test multiline config strings")
    public void testMultilineConfigStrings() throws IOException {
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        BValue[] inputArg = {new BString("multiline.strings.txt")};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);
        Assert.assertEquals(returnVals[0].stringValue(),
                            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                                    "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis " +
                                    "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                                    "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu " +
                                    "fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
                                    "culpa qui officia deserunt mollit anim id est laborum.");
    }

    @Test(description = "Test multiline config strings")
    public void testMultilineConfigStrings2() throws IOException {
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        BValue[] inputArg = {new BString("multiline.strings.txt2")};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);
        Assert.assertEquals(returnVals[0].stringValue(), "Hello\nWorld!!!\n");
    }

    @Test(description = "Test config keys with equals")
    public void testGetAsStringWithEquals() throws IOException {
        BString key = new BString("\\\"a=b\\\"");
        BValue[] inputArg = { key };

        registry.initRegistry(getRuntimeProperties(), customConfigFilePath, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsString", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "abcd");
    }

    @Test(description = "Test retrieving an invalid int config", expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*error: error occurred while trying to retrieve the value; config key " +
                  "'expirationPeriod' does not map to a valid 'int'.*")
    public void testGetAsIntNegative() throws IOException {
        BString key = new BString("expirationPeriod");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BRunUtil.invoke(compileResult, "testGetAsInt", inputArg);
    }

    @Test(description = "Test retrieving an invalid float config", expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*error: error occurred while trying to retrieve the value; config key " +
                  "'\"ballerina.http.host\"' does not map to a valid 'float'.*")
    public void testGetAsFloatNegative() throws IOException {
        BString key = new BString("\"ballerina.http.host\"");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BRunUtil.invoke(compileResult, "testGetAsFloat", inputArg);
    }

    @Test(description = "Test retrieving an invalid boolean config", expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*error: error occurred while trying to retrieve the value; config key " +
                  "'expirationPeriod' does not map to a valid 'boolean'.*")
    public void testGetAsBooleanNegative() throws IOException {
        BString key = new BString("expirationPeriod");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BRunUtil.invoke(compileResult, "testGetAsBoolean", inputArg);
    }

    @Test(description = "Test retrieving a config value as an array")
    public void testGetAsArray() throws IOException {
        BString key = new BString("listenerConfig.keyStore.paths");
        BValue[] inputArg = {key};
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testGetAsArray", inputArg);
        Assert.assertTrue(returnValues[0] instanceof BString);
        Assert.assertEquals(returnValues[0].stringValue(), "/etc");
        Assert.assertEquals(returnValues[1].stringValue(), "/tmp");
        Assert.assertEquals(returnValues[2].stringValue(), "/usr/lib/");
    }

    @Test(description = "Test retrieving a config value as an int array")
    public void testGetAsArray2() throws IOException {
        BString key = new BString("listenerConfig.keyStore.ports");
        BValue[] inputArg = {key};
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testGetAsArray2", inputArg);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);
        Assert.assertTrue((((BValueArray) returnValues[0]).elementType).getTag() == TypeTags.INT);
        Assert.assertEquals((returnValues[0]).stringValue(), "[9090, 9091, 9092]");
    }

    @Test(description = "Test retrieving a config value as an array of maps")
    public void testGetAsArray3() throws IOException {
        BValue[] inputArg = {new BString("foo")};
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testGetAsArray3", inputArg);
        Assert.assertTrue(returnValues[0] instanceof BMap);
        Assert.assertEquals((returnValues[0]).stringValue(), "{\"a\":1, \"b\":2}");
        Assert.assertEquals((returnValues[1]).stringValue(), "{\"a\":3, \"b\":4}");
    }

    @Test(description = "Test retrieving a config value of type multidimensional array")
    public void testGetAsArray4() throws IOException {
        BValue[] inputArg = {new BString("array.multidimensional")};
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testGetAsArray", inputArg);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);
        Assert.assertEquals((returnValues[0]).stringValue(), "[1, 2]");
        Assert.assertEquals((returnValues[1]).stringValue(), "[\"a\", \"b\", \"c\"]");
    }

    @Test(description = "Test retrieving a config value of type array of tables")
    public void testGetAsArray5() throws IOException {
        BValue[] inputArg = {new BString("products")};
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testGetAsArray", inputArg);
        Assert.assertTrue(returnValues[0] instanceof BMap);
        Assert.assertEquals((returnValues[0]).stringValue(), "{\"name\":\"Hammer\", \"sku\":738594937}");
        Assert.assertEquals((returnValues[1]).stringValue(), "{}");
        Assert.assertEquals((returnValues[2]).stringValue(),
                            "{\"color\":\"gray\", \"name\":\"Nail\", \"sku\":284758393}");
    }

    @Test(description = "Test retrieving a config value as a string array")
    public void testGetAsStringArray() throws IOException {
        BString key = new BString("listenerConfig.keyStore.paths");
        BValue[] inputArg = {key};
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testGetAsStringArray", inputArg);
        Assert.assertTrue(returnValues[0] instanceof BString);
        Assert.assertEquals(returnValues[0].stringValue(), "/etc");
        Assert.assertEquals(returnValues[1].stringValue(), "/tmp");
        Assert.assertEquals(returnValues[2].stringValue(), "/usr/lib/");
    }

    @Test(description = "Test retrieving a config value as a string map")
    public void testGetAsStringMap() throws IOException {
        BString key = new BString("employee");
        BValue[] inputArg = {key};
        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsStringMap", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap table = (BMap) returnVals[0];
        Assert.assertEquals(table.get("name").stringValue(), "John");
        Assert.assertEquals(table.get("city").stringValue(), "Sydney");
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*negative becomes a heterogeneous array on line 2")
    public void testGetAsArrayNegative() throws IOException {
        String negativeConf = Paths.get(resourceRoot, "datafiles", "array_negative.conf").toString();
        ConfigProcessor.processConfiguration(null, negativeConf, null);
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "Duplicate table definition on line 7:.*")
    public void testGetAsArrayNegative2() throws IOException {
        String negativeConf = Paths.get(resourceRoot, "datafiles", "array_negative2.conf").toString();
        ConfigProcessor.processConfiguration(null, negativeConf, null);
    }

    private Map<String, String> getRuntimeProperties() {
        Map<String, String> runtimeConfigs = new HashMap<>();
        runtimeConfigs.put("ballerina.http.host", "10.100.1.201");
        runtimeConfigs.put("http1.port", "8082");
        runtimeConfigs.put("\\\"a=b\\\"", "abcd");
        return runtimeConfigs;
    }
}
