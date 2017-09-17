/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.siddhi.core.event.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Holder class for general transport options.
 */
public class Option {
    private final String key;
    private final TemplateBuilder templateBuilder;
    private String value;
    private List<String> variableValues = new ArrayList<>();
    private int dataIndex = -1;

    public Option(String key, String value, TemplateBuilder templateBuilder) {
        this.key = key;
        this.value = value;
        this.templateBuilder = templateBuilder;
    }

    public Option(int dataIndex) {
        this.key = null;
        this.templateBuilder = null;
        this.dataIndex = dataIndex;
    }

    int addVariableValue(String value) {
        variableValues.add(value);
        return (variableValues.size() - 1);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isStatic() {
        return templateBuilder == null;
    }

    public String getValue(DynamicOptions dynamicOptions) {
        if (dynamicOptions.getVariableOptionIndex() != -1 && !variableValues.isEmpty()) {
            return variableValues.get(dynamicOptions.getVariableOptionIndex());
        } else if (value != null) {
            return value;
        } else if (templateBuilder != null) {
            return (String) templateBuilder.build(dynamicOptions.getEvent());
        } else if (dataIndex != -1) {
            return (String) dynamicOptions.getEvent().getData(dataIndex);
        } else {
            return null;
        }
    }

    public String getValue(Event event) {
        if (value != null) {
            return value;
        } else if (templateBuilder != null) {
            return (String) templateBuilder.build(event);
        } else if (dataIndex != -1) {
            return (String) event.getData(dataIndex);
        } else {
            return null;
        }
    }

}
