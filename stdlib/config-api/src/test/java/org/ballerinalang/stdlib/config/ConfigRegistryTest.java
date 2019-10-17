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

package org.ballerinalang.stdlib.config;

import org.ballerinalang.config.ConfigRegistry;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for the Java API for the Config Registry.
 *
 * @since 0.982.0
 */
public class ConfigRegistryTest {

    private static final ConfigRegistry registry = ConfigRegistry.getInstance();

    @Test
    public void testAddAndGetConfiguration() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("helloService", "http_port", 8080);
        registry.addConfiguration("helloService", "http_host", "localhost");

        Object conf = registry.getConfiguration("helloService.http_port");
        Assert.assertTrue(conf instanceof Integer);
        Assert.assertEquals((int) conf, 8080);

        conf = registry.getConfiguration("helloService", "http_port");
        Assert.assertTrue(conf instanceof Integer);
        Assert.assertEquals((int) conf, 8080);

        conf = registry.getConfiguration("helloService.http_host");
        Assert.assertTrue(conf instanceof String);
        Assert.assertEquals((String) conf, "localhost");

        conf = registry.getConfiguration("helloService", "http_host");
        Assert.assertTrue(conf instanceof String);
        Assert.assertEquals((String) conf, "localhost");

        conf = registry.getConfiguration("non_existent_config");
        Assert.assertNull(conf);

        conf = registry.getConfiguration("non_existent_config", "field");
        Assert.assertNull(conf);
    }

    @Test
    public void testGetAsBoolean() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("hello.http.port", 8080);
        registry.addConfiguration("hello.tracelog.enable", true);
        registry.addConfiguration("hello.accesslog.enable", false);

        Assert.assertTrue(registry.getAsBoolean("hello.tracelog.enable"));
        Assert.assertTrue(registry.getAsBoolean("hello", "tracelog.enable"));

        Assert.assertFalse(registry.getAsBoolean("hello.accesslog.enable"));
        Assert.assertFalse(registry.getAsBoolean("hello", "accesslog.enable"));

        // tests parsing null as a boolean
        Assert.assertFalse(registry.getAsBoolean("invalid$_env_key"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = ".*config key 'hello.http.port' does not map to a valid 'boolean'")
    public void testGetAsBooleanNegative() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("hello.http.port", 8080);
        registry.getAsBoolean("hello.http.port");
    }

    @Test
    public void testGetAsInt() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("hello.http.port", 8080L);

        Assert.assertEquals(registry.getAsInt("hello.http.port"), 8080);
        Assert.assertEquals(registry.getAsInt("hello", "http.port"), 8080);
    }

    @Test
    public void testGetAsInt2() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        Assert.assertEquals(registry.getAsInt("hello.http.port"), 5656);
        Assert.assertEquals(registry.getAsInt("hello", "http.port"), 5656);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = ".*config key 'hello.http.port' does not map to a valid 'int'")
    public void testGetAsIntNegative() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("hello.http.port", "invalid port");
        registry.getAsInt("hello.http.port");
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void testGetAsIntNegative2() {
        registry.getAsInt("non_existent_key");
    }

    @Test
    public void testGetAsFloat() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("hello.cache.evictionFactor", 0.25D);

        Assert.assertEquals(registry.getAsFloat("hello.cache.evictionFactor"), 0.25);
        Assert.assertEquals(registry.getAsFloat("hello", "cache.evictionFactor"), 0.25D);
    }

    @Test
    public void testGetAsFloat2() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        Assert.assertEquals(registry.getAsFloat("hello.eviction.fac"), 0.2333333);
        Assert.assertEquals(registry.getAsFloat("hello", "eviction.fac"), 0.2333333);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = ".*config key 'hello.cache.evictionFactor' does not map to a valid " +
                  "'float'")
    public void testGetAsFloatNegative() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("hello.cache.evictionFactor", "invalid float");
        registry.getAsFloat("hello.cache.evictionFactor");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetAsFloatNegative2() {
        registry.getAsFloat("non_existent_key");
    }

    @Test
    public void testGetAsMap() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("hello.http.port", 8080);
        registry.addConfiguration("hello.tracelog.enable", true);
        registry.addConfiguration("hello.accesslog.enable", false);

        Map<String, Object> configTable = registry.getAsMap("hello");
        Assert.assertEquals(configTable.get("http.port"), 8080);
        Assert.assertEquals(configTable.get("tracelog.enable"), true);
        Assert.assertEquals(configTable.get("accesslog.enable"), false);
    }

    @Test
    public void testGetAsStringAndRemove() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("echo.http.host", "localhost");

        Assert.assertEquals(registry.getAsString("echo.http.host"), "localhost");
        Assert.assertEquals(registry.getAsString("echo", "http.host"), "localhost");

        registry.removeConfiguration("echo.http.host");
        Assert.assertNull(registry.getAsString("echo.http.host"));
        Assert.assertNull(registry.getAsString("echo", "http.host"));
    }

    @Test
    public void testInvalidEnvKey() {
        Assert.assertNull(registry.getAsString("invalid$_env_key"));
    }

    @Test
    public void testContains() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("hello.http.port", 8080L);

        Assert.assertTrue(registry.contains("hello.http.port"));
        Assert.assertTrue(registry.contains("hello", "http.port"));

        registry.removeConfiguration("hello.http.port");
        Assert.assertFalse(registry.contains("hello.http.port"));
        Assert.assertFalse(registry.contains("hello", "http.port"));
    }

    @Test
    public void testGetConfigOrDefault() throws IOException {
        registry.initRegistry(new HashMap<>(), null, null);
        registry.addConfiguration("echo.http.host", "192.168.1.7");

        String host = registry.getConfigOrDefault("echo.http.host", "localhost");
        Assert.assertEquals(host, "192.168.1.7");

        String port = registry.getConfigOrDefault("echo.http.port", "9090");
        Assert.assertEquals(port, "9090");
    }

    @AfterClass
    public void resetRegistry() {
        registry.resetRegistry();
    }
}
