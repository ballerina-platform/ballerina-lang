/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.config;

import org.ballerinalang.bcl.parser.BConfig;
import org.ballerinalang.config.ConfigProcessor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for the config processor which manages the parsing of the configs and consolidation with env vars.
 *
 * @since 0.982.0
 */
public class ConfigProcessorTest {

    private Map<String, String> runtimeParams;
    private String resourceRoot;
    private String confFile;

    @BeforeClass
    public void setup() {
        resourceRoot = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
                .getAbsolutePath();
        confFile = Paths.get(resourceRoot, "datafiles", "config", "lookupenv.conf").toString();

        runtimeParams = new HashMap<>();
        runtimeParams.put("echo.http.host", "10.100.1.201");
        runtimeParams.put("echo.http.port", "8082");
    }

    @Test
    public void testProcessConfiguration() throws IOException {
        BConfig bConfig = ConfigProcessor.processConfiguration(runtimeParams, confFile, null);
        Map<String, Object> configs = bConfig.getConfigurations();

        // Verify runtime params were correctly parsed and processed
        Assert.assertTrue(bConfig.getConfigurations().keySet().containsAll(runtimeParams.keySet()));
        Assert.assertTrue(bConfig.getConfigurations().values().containsAll(runtimeParams.values()));

        // Verify config file was correctly parsed and env values were correctly looked up
        Object obj = configs.get("hello.http.host");
        Assert.assertTrue(obj instanceof String);
        Assert.assertEquals(obj, "192.168.1.11");

        obj = configs.get("hello.http.port");
        Assert.assertTrue(obj instanceof Long);
        Assert.assertEquals(obj, 5656L);

        obj = configs.get("hello.cache.enabled");
        Assert.assertTrue(obj instanceof Boolean);

        obj = configs.get("hello.eviction.fac");
        Assert.assertTrue(obj instanceof Double);
        Assert.assertEquals(obj, 0.2333333D);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = ".*invalid int value received from environment to 'helloneg.http.port': " +
                  "invalid port")
    public void testEnvLookupNegative() throws IOException {
        String negativeConf = Paths.get(resourceRoot, "datafiles", "config", "lookupenv_negative.conf").toString();
        ConfigProcessor.processConfiguration(null, negativeConf, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = ".*invalid float value received from environment to 'helloneg.eviction" +
                  ".fac': invalid eviction factor")
    public void testEnvLookupNegative2() throws IOException {
        String negativeConf = Paths.get(resourceRoot, "datafiles", "config", "lookupenv_negative2.conf").toString();
        ConfigProcessor.processConfiguration(null, negativeConf, null);
    }

    // Not checking the error message in this because the order of the messages cannot be guaranteed.
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEnvLookupNegative3() throws IOException {
        String negativeConf = Paths.get(resourceRoot, "datafiles", "config", "lookupenv_negative3.conf").toString();
        ConfigProcessor.processConfiguration(null, negativeConf, null);
    }
}
