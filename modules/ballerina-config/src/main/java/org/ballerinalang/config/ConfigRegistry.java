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

package org.ballerinalang.config;

import java.util.Map;

/**
 * ConfigRegistry caches configuration properties and provide API.
 *
 * @since 0.94.2
 */
public class ConfigRegistry {

    private static Map<String, String> globalConfigs;
    private static Map<String, Map<String, String>> instanceConfigs;
    private static String confLocation;
    private static boolean traceLogEnabled;

    /**
     * Sets global config properties as a map.
     *
     * @param globalConfigMap is a processed map of key value pairs.
     */
    protected static void setGlobalConfigs(Map<String, String> globalConfigMap) {
        globalConfigs = globalConfigMap;
    }

    /**
     * Returns global config value based on config name.
     *
     * @param configKey is the configuration.
     * @return config value
     */
    public static String getGlobalConfigValue(String configKey) {
        if (globalConfigs.isEmpty() || (globalConfigs.get(configKey) == null)) {
            return "";
        }
        return globalConfigs.get(configKey);
    }

    /**
     * Sets instance config properties as a map.
     *
     * @param instanceConfigMap is a processed map of key value pairs.
     */
    protected static void setInstanceConfigs(Map<String, Map<String, String>> instanceConfigMap) {
        instanceConfigs = instanceConfigMap;
    }

    /**
     * Returns instance config value based on instance id and config name.
     *
     * @param instanceId is the instance.
     * @param configKey is the configuration.
     * @return config value
     */
    public static String getInstanceConfigValue(String instanceId, String configKey) {
        if (instanceConfigs.isEmpty() || (instanceConfigs.get(instanceId) == null)) {
            return "";
        }

        Map<String, String> instanceConfig = instanceConfigs.get(instanceId);
        String configValue = instanceConfig.get(configKey);
        if (configValue == null) {
            return "";
        }
        return configValue;
    }

    /**
     * Sets TraceLogEnabled config properties.
     *
     * @param traceEnabled
     */
    protected static void setHttpTraceLogEnabled(boolean traceEnabled) {
        traceLogEnabled = traceEnabled;
    }

    /**
     * Returns traceLogEnabled property.
     *
     * @return property value
     */
    public static boolean isTraceLogEnabled() {
        return traceLogEnabled;
    }

    /**
     * Sets config property file location.
     *
     * @param confFileLocation
     */
    protected static void setConfLocation(String confFileLocation) {
        confLocation = confFileLocation;
    }

    /**
     * Returns config property file location.
     *
     * @return config file location
     */
    public static String getConfLocation() {
        return confLocation;
    }
}
