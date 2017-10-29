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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to take a Ballerina config file and parse it.
 *
 * @since 0.95
 */
public class ConfigFileParser {

    private static final String configEntryFormat = "([a-zA-Z0-9.])+=([\\ -~])+";
    private static final String instanceIdFormat = "\\[[a-zA-Z0-9]+\\]";
    private static final String commentOrWSFormat = "[\\s]*#[\\ -~]*|[\\s]*"; // to skip comments or whitespace
    private static final String variableFormat = "\\$(env|sys)\\{([a-zA-Z_]+[a-zA-Z0-9_\\.]*)\\}";
    private static final Pattern variablePattern = Pattern.compile(variableFormat);

    private static final String ENVIRONMENT_VARIABLE = "env";
    private static final String SYSTEM_PROPERTY = "sys";

    private Map<String, String> globalConfigs;
    private Map<String, Map<String, String>> instanceConfigs;

    private List<String> invalidConfigs;
    private int currentLine;

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
            invalidConfigs = new ArrayList<>();

            String line;
            for (currentLine = 1; (line = reader.readLine()) != null; currentLine++) {
                if (line.matches(configEntryFormat)) {
                    parseGlobalConfigEntry(line);
                } else if (line.matches(instanceIdFormat)) {
                    currentLine++;
                    break;
                } else if (!line.matches(commentOrWSFormat)) {
                    accumulateError(currentLine);
                }
            }

            String currentInstance = extractInstanceId(line);
            instanceConfigs.put(currentInstance, new HashMap<>());

            for (; (line = reader.readLine()) != null; currentLine++) {
                if (line.matches(configEntryFormat)) {
                    parseInstanceConfigEntry(currentInstance, line);
                } else if (line.matches(instanceIdFormat)) {
                    currentInstance = extractInstanceId(line);
                } else if (!line.matches(commentOrWSFormat)) {
                    accumulateError(currentLine);
                }
            }

            if (invalidConfigs.size() > 0) {
                // TODO: Fix this with the API to print to the console
                PrintStream console = System.out;
                invalidConfigs.forEach(console::println);
                throw new BallerinaException("");
            }
        }
    }

    private String parseConfigValue(String value) {
        Matcher matcher = variablePattern.matcher(value);

        if (!matcher.find()) {
            if (value.matches("[^\\n]*\\$[^\\n]+")) {
                accumulateError(currentLine, value);
            }
            return value;
        }

        StringBuilder varReplacedValue = new StringBuilder();
        int i = 0;
        do {
            if (matcher.start() != i) {
                varReplacedValue.append(value.substring(i, matcher.start()));
            }

            String varType = matcher.group(1);
            String key = matcher.group(2);
            switch (varType) {
                case ENVIRONMENT_VARIABLE:
                    varReplacedValue.append(System.getenv(key));
                    break;
                case SYSTEM_PROPERTY:
                    varReplacedValue.append(System.getProperty(key));
                    break;
            }
        } while (matcher.find());

        return varReplacedValue.toString();
    }

    private void parseGlobalConfigEntry(String configEntry) {
        String[] entryParts = getConfigKeyAndValue(configEntry);
        globalConfigs.put(entryParts[0], parseConfigValue(entryParts[1]));
    }

    private void parseInstanceConfigEntry(String instanceId, String configEntry) {
        String[] entryParts = getConfigKeyAndValue(configEntry);
        if (instanceConfigs.containsKey(instanceId)) {
            instanceConfigs.get(instanceId).put(entryParts[0], parseConfigValue(entryParts[1]));
        } else {
            Map<String, String> map = new HashMap<>();
            map.put(entryParts[0], parseConfigValue(entryParts[1]));
            instanceConfigs.put(instanceId, map);
        }
    }

    private String[] getConfigKeyAndValue(String configEntry) {
        return configEntry.split("=");
    }

    private String extractInstanceId(String id) {
        return id.substring(1, id.length() - 1);
    }

    private void accumulateError(int lineNum) {
        invalidConfigs.add("ballerina: invalid configuration at line: " + lineNum);
    }

    private void accumulateError(int lineNum, String invalidVal) {
        invalidConfigs.add("ballerina: invalid variable format found at line #" + lineNum + ": " + invalidVal);
    }
}
