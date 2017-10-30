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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extension of the generic AbstractConfigParser to parse CLI config parameters (i.e: parameters passed as -B<key>=<value>).
 *
 * @since 0.95
 */
public class ConfigParamParser extends AbstractConfigParser {

    private static final String globalConfigKeyFormat = "[a-zA-Z0-9.]+";
    private static final String instanceConfigKeyFormat = "(\\[[a-zA-Z0-9]+\\])\\.([a-zA-Z0-9.]+)";
    private static final String configValueFormat = "[\\ -~]+";
    private static final Pattern instanceConfigKeyPattern = Pattern.compile(instanceConfigKeyFormat);

    public ConfigParamParser(Map<String, String> cliParams) {
        parse(cliParams);
    }

    private void parse(Map<String, String> cliParams) {
        cliParams.forEach((key, val) -> {
            if (val.matches(configValueFormat)) {
                if (key.matches(globalConfigKeyFormat)) {
                    globalConfigs.put(key, parseConfigValue(val));
                } else if (key.matches(instanceConfigKeyFormat)) {
                    Matcher matcher = instanceConfigKeyPattern.matcher(key);
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
                    throw new BallerinaException("invalid configuration parameter key: " + key);
                }
            } else {
                throw new BallerinaException("invalid configuration parameter value: " + val);
            }
        });
    }
}
