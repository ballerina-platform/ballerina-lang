/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.nativeimpl.functions.config;

import org.ballerinalang.config.utils.parser.ConfigFileParser;
import org.ballerinalang.config.utils.parser.ConfigParamParser;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for the configuration parser.
 */
public class ConfigParserTest {

    private static final String K_BALLERINA_HOME = "ballerina.home";
    private static final String K_HTTP_HOST = "ballerina.http.host";
    private static final String K_HTTP_INSTANCES = "ballerina.http.instances";
    private static final String K_ENV_PATH = "ballerina.env.PATH";
    private static final String K_INST1_HTTP_PORT = "[http1].ballerina.http.port";
    private static final String K_INST2_HTTP_PORT = "[http2].ballerina.http.port";

    private static final String V_BAL_TEST_HOME = "/home/user/ballerina";
    private static final String V_HTTP_HOST = "10.100.1.200";
    private static final String V_HTTP_INSTANCES = "ballerina.http.instances";
    private static final String V_ENV_PATH = System.getenv("PATH");
    private static final String V_INST1_HTTP_PORT = "8080";
    private static final String V_INST2_HTTP_PORT = "8082";

    private Map<String, String> cliParams;

    @BeforeClass
    public void setup() {
        System.setProperty(K_BALLERINA_HOME, V_BAL_TEST_HOME);
        cliParams = new HashMap<>();
        cliParams.put(K_HTTP_HOST, V_HTTP_HOST);
        cliParams.put(K_HTTP_INSTANCES, V_HTTP_INSTANCES);
        cliParams.put(K_ENV_PATH, "${env:PATH}");
        cliParams.put(K_BALLERINA_HOME, "${sys:ballerina.home}");
        cliParams.put(K_INST1_HTTP_PORT, V_INST1_HTTP_PORT);
        cliParams.put(K_INST2_HTTP_PORT, V_INST2_HTTP_PORT);
    }

    @Test
    public void testConfigFileParser() throws IOException {
        File configFile = new File(
                getClass().getClassLoader().getResource("datafiles/config/ballerina.conf").getPath());

        ConfigFileParser parser = new ConfigFileParser(configFile);
        Map<String, String> globalConfigs = parser.getGlobalConfigs();
        Map<String, Map<String, String>> instanceConfigs = parser.getInstanceConfigs();

        Assert.assertNotNull(globalConfigs);
        Assert.assertEquals(globalConfigs.size(), 4);
        Assert.assertEquals(instanceConfigs.size(), 2);
        Assert.assertEquals(globalConfigs.get(K_ENV_PATH), "Path variable: " + V_ENV_PATH);
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp =
                  "invalid configuration\\(s\\) in the file at line\\(s\\): \\[23, 26\\].*")
    public void testInvalidConfigFile() throws IOException {
        File configFile = new File(
                getClass().getClassLoader().getResource("datafiles/config/invalid-ballerina.conf").getPath());
        new ConfigFileParser(configFile);
    }

    @Test
    public void testConfigParamParser() {
        ConfigParamParser paramParser = new ConfigParamParser(cliParams);
        Map<String, String> globalConfigs = paramParser.getGlobalConfigs();
        Map<String, Map<String, String>> instanceConfigs = paramParser.getInstanceConfigs();

        Assert.assertNotNull(globalConfigs);
        Assert.assertEquals(globalConfigs.size(), 4);
        Assert.assertEquals(instanceConfigs.size(), 2);
        Assert.assertEquals(globalConfigs.get(K_ENV_PATH), V_ENV_PATH);
        Assert.assertEquals(globalConfigs.get(K_BALLERINA_HOME), V_BAL_TEST_HOME);
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "invalid configuration parameter key.*")
    public void testConfigParamParserInvalidKey() {
        Map<String, String> map = new HashMap<>(cliParams);
        map.put("invalid.$key", "valid-value");
        new ConfigParamParser(map);
    }
}
