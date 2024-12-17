/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projects;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that acts as the compiler plugin cache for a project.
 *
 * @since 2201.8.7
 */
public class CompilerPluginCache {

    Map<String, Map<String, Object>> pluginMap;

    /**
     * Constructor to initialize plugin cache.
     */
    public CompilerPluginCache() {
        pluginMap = new HashMap<>();
    }

    /**
     * Returns the user data for a particular compiler plugin.
     *
     * @param key compiler plugin id
     * @return data holder map for the plugin
     */
    Map<String, Object> getData(String key) {
        if (!pluginMap.containsKey(key)) {
            pluginMap.put(key, new HashMap<>());
        }
        return pluginMap.get(key);
    }

    /**
     * Adds a data holder map for a compiler plugin.
     *
     * @param key   string fully qualified name of the compiler plugin
     * @param value user data as a map
     */
    public void putData(String key, Map<String, Object> value) {
        this.pluginMap.put(key, value);
    }
}
