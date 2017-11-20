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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extension of the generic AbstractConfigParser to parse CLI config parameters.
 * i.e: parameters passed as -B&lt;key&gt;=&lt;value&gt;
 *
 * @since 0.95
 */
public class ConfigParamParser extends AbstractConfigParser {

    private static final String INSTANCE_CONFIG_KEY_FORMAT = "(\\[[a-zA-Z0-9._]+\\])\\.([a-zA-Z0-9._]+)";
    private static final Pattern INSTANCE_CONFIG_KEY_PATTERN = Pattern.compile(INSTANCE_CONFIG_KEY_FORMAT);

    public ConfigParamParser(Map<String, String> cliParams) {
        parse(cliParams);
    }

    private void parse(Map<String, String> cliParams) {
        cliParams.forEach((key, val) -> {
            if (key.matches(CONFIG_KEY_FORMAT)) {
                globalConfigs.put(key, parseConfigValue(val));
            } else if (key.matches(INSTANCE_CONFIG_KEY_FORMAT)) { // Check if key matches the "exact" specified format
                Matcher matcher = INSTANCE_CONFIG_KEY_PATTERN.matcher(key);
                matcher.find();

                String instanceId = extractInstanceId(matcher.group(1));
                String configKey = matcher.group(2);
                if (instanceConfigs.containsKey(instanceId)) {
                    instanceConfigs.get(instanceId).put(configKey, parseConfigValue(val));
                } else {
                    Map<String, String> map = new HashMap<>();
                    map.put(configKey, parseConfigValue(val));
                    instanceConfigs.put(instanceId, map);
                }
            } else {
                throw new RuntimeException(
                        "invalid configuration parameter key: " + key + ", key should conform to " +
                                CONFIG_KEY_FORMAT + " or " + INSTANCE_CONFIG_KEY_FORMAT);
            }
        });
    }
}
