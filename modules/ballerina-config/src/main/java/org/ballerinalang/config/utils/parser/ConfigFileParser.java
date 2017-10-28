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

package org.ballerinalang.config.utils.parser;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to take a Ballerina config file and parse it.
 *
 * @since 0.95
 */
public class ConfigFileParser {

    private static final String configEntryFormat = "([a-zA-Z0-9.])+=([\\ -~])+";
    private static final String instanceIdFormat = "\\[[a-zA-Z0-9]+\\]";
    private static final String commentOrWSFormat = "[\\t\\ ]*#[\\ -~]*|[\\t\\ ]*"; // to skip comments or whitespace

    private Map<String, String> globalConfigs;
    private Map<String, Map<String, String>> instanceConfigs;

    public ConfigFileParser(File configFile) throws IOException {
        parse(configFile);
    }

    /**
     * Returns the parsed global configurations as a map.
     *
     * @return Global configurations map
     */
    public Map<String, String> getGlobalConfigs() {
        return globalConfigs;
    }

    /**
     * Returns the parsed instance level configurations as a map.
     *
     * @return Instance configurations map
     */
    public Map<String, Map<String, String>> getInstanceConfigs() {
        return instanceConfigs;
    }

    /**
     * Parses the given configuration file. The config file consists of two sections: the global config section and the
     * instance config section. The configs from the start of the file until the first instance ID tag (a string
     * enclosed within brackets: i.e- [http1]). The configs between two instance ID tags are considered to be configs
     * belonging to the first of these instance IDs.
     *
     * @param file The configuration file
     * @throws IOException Thrown if any errors are encountered while reading the file
     */
    private void parse(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            globalConfigs = new HashMap<>();
            instanceConfigs = new HashMap<>();

            String line;
            for (int i = 1; (line = reader.readLine()) != null; i++) {
                if (line.matches(configEntryFormat)) {
                    parseGlobalConfigEntry(line);
                } else if (line.matches(instanceIdFormat)) {
                    break;
                } else if (!line.matches(commentOrWSFormat)) {
                    throw new BallerinaException("invalid configuration at line #" + i);
                }
            }

            String currentInstance = extractInstanceId(line);
            instanceConfigs.put(currentInstance, new HashMap<>());

            for (int i = 1; (line = reader.readLine()) != null; i++) {
                if (line.matches(configEntryFormat)) {
                    parseInstanceConfigEntry(currentInstance, line);
                } else if (line.matches(instanceIdFormat)) {
                    currentInstance = extractInstanceId(line);
                } else if (!line.matches(commentOrWSFormat)) {
                    throw new BallerinaException("invalid configuration at line #" + i);
                }
            }
        }
    }

    private void parseGlobalConfigEntry(String configEntry) {
        String[] entryParts = getConfigKeyAndValue(configEntry);
        globalConfigs.put(entryParts[0], entryParts[1]);
    }

    private void parseInstanceConfigEntry(String instanceId, String configEntry) {
        String[] entryParts = getConfigKeyAndValue(configEntry);
        if (instanceConfigs.containsKey(instanceId)) {
            instanceConfigs.get(instanceId).put(entryParts[0], entryParts[1]);
        } else {
            Map<String, String> map = new HashMap<>();
            map.put(entryParts[0], entryParts[1]);
            instanceConfigs.put(instanceId, map);
        }
    }

    private String[] getConfigKeyAndValue(String configEntry) {
        return configEntry.split("=");
    }

    private String extractInstanceId(String id) {
        return id.substring(1, id.length() - 1);
    }
}
