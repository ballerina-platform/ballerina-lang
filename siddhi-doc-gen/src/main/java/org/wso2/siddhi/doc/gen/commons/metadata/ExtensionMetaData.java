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
package org.wso2.siddhi.doc.gen.commons.metadata;

import java.util.List;

/**
 * POJO for holding extension meta data
 */
public class ExtensionMetaData {
    private String name;
    private String namespace;
    private String description;
    private List<ParameterMetaData> parameters;
    private List<SystemParameterMetaData> systemParameters;
    private List<ReturnAttributeMetaData> returnAttributes;
    private List<ExampleMetaData> examples;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ParameterMetaData> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterMetaData> parameters) {
        this.parameters = parameters;
    }

    public List<SystemParameterMetaData> getSystemParameters() {
        return systemParameters;
    }

    public void setSystemParameters(List<SystemParameterMetaData> systemParameters) {
        this.systemParameters = systemParameters;
    }

    public List<ReturnAttributeMetaData> getReturnAttributes() {
        return returnAttributes;
    }

    public void setReturnAttributes(List<ReturnAttributeMetaData> returnAttributes) {
        this.returnAttributes = returnAttributes;
    }

    public List<ExampleMetaData> getExamples() {
        return examples;
    }

    public void setExamples(List<ExampleMetaData> examples) {
        this.examples = examples;
    }
}
