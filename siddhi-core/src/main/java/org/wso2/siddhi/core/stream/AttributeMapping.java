/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.stream;

/**
 * Holder object to store mapping information for a given Siddhi {@link org.wso2.siddhi.query.api.definition.Attribute}
 */
public class AttributeMapping {
    private String mapping;
    private String rename;

    public AttributeMapping(String mapping) {
        this.mapping = mapping;
    }

    public AttributeMapping(String rename, String mapping) {
        this.rename = rename;
        this.mapping = mapping;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getRename() {
        return rename;
    }

    public void setRename(String rename) {
        this.rename = rename;
    }

    @Override
    public String toString() {
        return "AttributeMapping{" +
                "mapping='" + mapping + '\'' +
                ", rename='" + rename + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AttributeMapping that = (AttributeMapping) o;

        if (mapping != null ? !mapping.equals(that.mapping) : that.mapping != null) {
            return false;
        }
        return !(rename != null ? !rename.equals(that.rename) : that.rename != null);

    }

    @Override
    public int hashCode() {
        int result = mapping != null ? mapping.hashCode() : 0;
        result = 31 * result + (rename != null ? rename.hashCode() : 0);
        return result;
    }
}
