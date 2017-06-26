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
package org.wso2.siddhi.doc.gen.commons;

/**
 * POJO for holding extension meta data
 */
public class ExtensionMetaData {
    private String name;
    private String namespace;
    private String description;
    private ParameterMetaData[] parameters;
    private SystemParameterMetaData[] systemParameters;
    private ReturnAttributeMetaData[] returnAttributes;
    private ExampleMetaData[] examples;

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

    public ParameterMetaData[] getParameters() {
        return parameters;
    }

    public void setParameters(ParameterMetaData[] parameters) {
        this.parameters = parameters;
    }

    public SystemParameterMetaData[] getSystemParameters() {
        return systemParameters;
    }

    public void setSystemParameters(SystemParameterMetaData[] systemParameters) {
        this.systemParameters = systemParameters;
    }

    public ReturnAttributeMetaData[] getReturnAttributes() {
        return returnAttributes;
    }

    public void setReturnAttributes(ReturnAttributeMetaData[] returnAttributes) {
        this.returnAttributes = returnAttributes;
    }

    public ExampleMetaData[] getExamples() {
        return examples;
    }

    public void setExamples(ExampleMetaData[] examples) {
        this.examples = examples;
    }
}
