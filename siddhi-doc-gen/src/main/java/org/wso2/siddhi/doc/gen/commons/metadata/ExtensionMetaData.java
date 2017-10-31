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
public class ExtensionMetaData implements Comparable<ExtensionMetaData> {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExtensionMetaData that = (ExtensionMetaData) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) {
            return false;
        }
        if (systemParameters != null ? !systemParameters.equals(that.systemParameters) :
                that.systemParameters != null) {
            return false;
        }
        if (returnAttributes != null ? !returnAttributes.equals(that.returnAttributes) :
                that.returnAttributes != null) {
            return false;
        }
        return examples != null ? examples.equals(that.examples) : that.examples == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + (systemParameters != null ? systemParameters.hashCode() : 0);
        result = 31 * result + (returnAttributes != null ? returnAttributes.hashCode() : 0);
        result = 31 * result + (examples != null ? examples.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(ExtensionMetaData o) {
        if (o == null) {
            return -1;
        }
        if (this.equals(o)) {
            return 0;
        }
        int result;
        if (namespace != null && o.namespace != null) {
            result = namespace.compareTo(o.namespace);
        } else if (namespace == null && o.namespace == null) {
            result = 0;
        } else if (namespace == null) {
            return -1;
        } else {
            return 1;
        }
        if (result == 0) {
            if (name != null && o.name != null) {
                result = name.compareTo(o.name);
            } else if (name == null && o.name == null) {
                result = 0;
            } else if (name == null) {
                return -1;
            } else {
                return 1;
            }
            if (result == 0) {
                result = description.compareTo(o.description);
            }
        }
        return result;
    }
}
