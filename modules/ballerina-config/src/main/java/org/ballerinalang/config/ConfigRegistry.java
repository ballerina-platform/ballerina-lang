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

import java.util.HashMap;
import java.util.Map;

/**
 * ConfigRegistry caches configuration properties.
 *
 * @since 0.95
 */
public class ConfigRegistry {

    private static Map<String, String> cliParams = new HashMap<>();
    private static Map<String, String> globalConfMap;
    private static Map<String, Map<String, String>> instancMap;

    public ConfigRegistry() {
        Parser parser = new Parser();
        globalConfMap = parser.loadGlobalConfiguration();
        instancMap = parser.loadInstanceConfiguration();
    }

    public static void setCLIConfiguration(Map<String, String> params) {
        cliParams = params;
    }

    private void processGlobalValue() {

    }

    private void processInstanceValue() {

    }


    public static String getGlobalValue(String configKey) {

        if (globalConfMap.isEmpty() || (globalConfMap.get(configKey) == null)) {
            return "";
        }
        return null;
    }

    public static String getInstanceValue(String instanceId, String configKey) {
        return null;
    }
}
