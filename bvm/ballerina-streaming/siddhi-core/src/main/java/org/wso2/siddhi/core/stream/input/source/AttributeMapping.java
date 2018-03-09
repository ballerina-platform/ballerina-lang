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

package org.wso2.siddhi.core.stream.input.source;

/**
 * Holder object to store mapping information for a given Siddhi Attribute
 * {@link org.wso2.siddhi.query.api.definition.Attribute}
 */
public class AttributeMapping {
    protected String name;
    protected int position;
    private String mapping = null;

    public AttributeMapping(String name, int position, String mapping) {
        this.name = name;
        this.position = position;
        this.mapping = mapping;
    }

    public String getMapping() {
        return mapping;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "AttributeMapping{" +
                "name='" + name + '\'' +
                ", mapping='" + mapping + '\'' +
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

        if (position != that.position) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return mapping != null ? mapping.equals(that.mapping) : that.mapping == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + position;
        result = 31 * result + (mapping != null ? mapping.hashCode() : 0);
        return result;
    }
}
