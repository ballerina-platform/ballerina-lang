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

import org.ballerinalang.config.utils.ConfigFileParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An extension of the generic AbstractConfigParser to parse ballerina.conf files.
 *
 * @since 0.95
 */
public class ConfigFileParser extends AbstractConfigParser {

    private static final String CONFIG_ENTRY_FORMAT = CONFIG_KEY_FORMAT + "=(.*)";
    private static final PrintStream console = System.err;

    private List<Integer> invalidConfigs;
    private int currentLine;

    public ConfigFileParser(File configFile) throws ConfigFileParserException {
        parse(configFile);
    }

    /**
     * Parses the given configuration file. The config file consists of two sections: the global config section and the
     * instance config section. The configs from the start of the file until the first instance ID tag (a string
     * enclosed within brackets: i.e- [http1]). The configs between two instance ID tags are considered to be configs
     * belonging to the first of these instance IDs.
     *
     * @param file The configuration file
     * @throws ConfigFileParserException Thrown if any errors are encountered while reading the file
     */
    private void parse(File file) throws ConfigFileParserException {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            invalidConfigs = new ArrayList<>();

            String line;
            for (currentLine = 1; (line = reader.readLine()) != null; currentLine++) {
                line = line.trim();
                if (line.matches(CONFIG_ENTRY_FORMAT)) {
                    parseGlobalConfigEntry(line);
                } else if (line.matches(INSTANCE_ID_FORMAT)) {
                    currentLine++;
                    break;
                } else if (!line.matches(COMMENT_OR_WS_FORMAT)) {
                    collectConfigError(currentLine);
                }
            }

            if (line == null) {
                return;
            }

            String currentInstance = extractInstanceId(line);
            instanceConfigs.put(currentInstance, new HashMap<>());

            for (; (line = reader.readLine()) != null; currentLine++) {
                line = line.trim();
                if (line.matches(CONFIG_ENTRY_FORMAT)) {
                    parseInstanceConfigEntry(currentInstance, line);
                } else if (line.matches(INSTANCE_ID_FORMAT)) {
                    currentInstance = extractInstanceId(line);
                } else if (!line.matches(COMMENT_OR_WS_FORMAT)) {
                    collectConfigError(currentLine);
                }
            }

            if (invalidConfigs.size() > 0) {
                throw new RuntimeException(
                        "invalid configuration(s) in the file at line(s): " + invalidConfigs.toString() +
                                ", config entries should conform to " + CONFIG_ENTRY_FORMAT);
            }
        } catch (UnsupportedEncodingException e) {
            throw new ConfigFileParserException("unsupported encoding detected", e);
        } catch (FileNotFoundException e) {
            throw new ConfigFileParserException("file not found: " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new ConfigFileParserException("failed to read the configuration file" + file.getAbsolutePath(), e);
        }
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
        String[] entryParts = configEntry.split("=");
        trimConfigEntry(entryParts);
        return entryParts;
    }

    private void collectConfigError(int invalidLine) {
        invalidConfigs.add(invalidLine);
    }

    private void trimConfigEntry(String[] entryParts) {
        entryParts[0] = entryParts[0].trim();
        entryParts[1] = entryParts[1].trim();
    }
}
