/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util.transport;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holder object to contain {@link Option}
 */
public class OptionHolder {

    private Map<String, Option> options = new HashMap<>();
    private Extension extension;
    private Set<String> dynamicOptionsKeys = new HashSet<>();
    private Set<String> staticOptionsKeys = new HashSet<>();

    public OptionHolder(StreamDefinition streamDefinition, Map<String, String> staticOptions,
                        Map<String, String> dynamicOptions, Extension extension) {
        this.extension = extension;
        for (Map.Entry<String, String> entry : staticOptions.entrySet()) {
            options.put(entry.getKey(), new Option(entry.getKey(), entry.getValue(), null));
        }
        for (Map.Entry<String, String> entry : dynamicOptions.entrySet()) {
            options.put(entry.getKey(), new Option(entry.getKey(), null,
                                                   new TemplateBuilder(streamDefinition, entry.getValue())));
        }

        staticOptions.keySet().forEach(key -> staticOptionsKeys.add(key));
        dynamicOptions.keySet().forEach(key -> dynamicOptionsKeys.add(key));
    }

    public Option validateAndGetOption(String optionKey) {
        Option option = options.get(optionKey);
        if (option == null) {
            throw new SiddhiAppValidationException("Option '" + optionKey + "' does not exist in the " +
                    "configuration of '" + extension.namespace() + ":" +
                    extension.name() + "'.");
        }
        return option;
    }

    public Option getOrCreateOption(String optionKey, String defaultValue) {
        Option option = options.get(optionKey);
        if (option == null) {
            option = new Option(optionKey, defaultValue, null);
        }
        return option;
    }

    Option getOrAddStaticOption(String optionKey, String value) {
        Option option = options.get(optionKey);
        if (option == null) {
            option = new Option(optionKey, value, null);
            options.put(optionKey, option);

        }
        return option;
    }

    public String validateAndGetStaticValue(String optionKey, String defaultValue) {
        Option option = options.get(optionKey);
        if (option != null) {
            if (!option.isStatic()) {
                throw new SiddhiAppValidationException("'" + optionKey + "' is not a 'static' " +
                        "option in the configuration of " +
                        extension.namespace() + ":" + extension.name() +
                        ".");
            }
            return option.getValue();
        } else {
            return defaultValue;
        }
    }

    public String validateAndGetStaticValue(String optionKey) {
        Option option = options.get(optionKey);
        if (option != null) {
            if (!option.isStatic()) {
                throw new SiddhiAppValidationException("'" + optionKey + "' is defined as a 'dynamic' option " +
                        "but it has to be a 'static' option for the " +
                        extension.namespace() + ":" +
                        extension.name() + " configuration.");
            }
            return option.getValue();
        } else {
            throw new SiddhiAppValidationException("'" + optionKey + "' 'static' option is not " +
                    "defined in the configuration of " +
                    extension.namespace() + ":" + extension.name() + ".");
        }
    }

    OptionHolder merge(OptionHolder optionHolderToMerge) {
        optionHolderToMerge.getDynamicOptionsKeys().forEach(key -> {
            Option optionToMerge = optionHolderToMerge.validateAndGetOption(key);
            options.put(key, optionToMerge);
            dynamicOptionsKeys.add(key);
        });

        optionHolderToMerge.getStaticOptionsKeys().forEach(key -> {
            Option optionToMerge = optionHolderToMerge.validateAndGetOption(key);
            options.put(key, optionToMerge);
            staticOptionsKeys.add(key);
        });

        return this;
    }

    public boolean isOptionExists(String optionKey) {
        return (options.get(optionKey) != null);
    }

    public Set<String> getDynamicOptionsKeys() {
        return dynamicOptionsKeys;
    }

    public Set<String> getStaticOptionsKeys() {
        return staticOptionsKeys;
    }

}
