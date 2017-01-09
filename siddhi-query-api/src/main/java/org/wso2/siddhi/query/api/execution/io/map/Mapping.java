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

package org.wso2.siddhi.query.api.execution.io.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Mapping {
    private String format;
    private Map<String, String> options = new HashMap<String, String>();
    private Map<String, String> dynamicOptions = new HashMap<String, String>();
    private List<AttributeMapping> attributeMappingList = new ArrayList<AttributeMapping>();

    private Mapping(String format) {
        this.format = format;
    }

    public static Mapping format(String type) {
        return new Mapping(type);
    }

    public Mapping option(String key, String value) {
        if (Pattern.matches("\\{\\{.*?}}", value)) {
            dynamicOptions.put(key, value);
        } else {
            options.put(key, value);
        }
        return this;
    }

    public Mapping map(String mapping) {
        attributeMappingList.add(new AttributeMapping(mapping));
        return this;
    }

    public Mapping map(String rename, String mapping) {
        attributeMappingList.add(new AttributeMapping(rename, mapping));
        return this;
    }

    public String getFormat() {
        return format;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public Map<String, String> getDynamicOptions() {
        return dynamicOptions;
    }

    public List<AttributeMapping> getAttributeMappingList() {
        return attributeMappingList;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "format='" + format + '\'' +
                ", options=" + options +
                ", dynamicOptions=" + dynamicOptions +
                ", attributeMappingList=" + attributeMappingList + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mapping mapping = (Mapping) o;

        if (format != null ? !format.equals(mapping.format) : mapping.format != null) return false;
        if (options != null ? !options.equals(mapping.options) : mapping.options != null) return false;
        return !(attributeMappingList != null ? !attributeMappingList.equals(mapping.attributeMappingList) : mapping.attributeMappingList != null);

    }

    @Override
    public int hashCode() {
        int result = format != null ? format.hashCode() : 0;
        result = 31 * result + (options != null ? options.hashCode() : 0);
        result = 31 * result + (dynamicOptions != null ? dynamicOptions.hashCode() : 0);
        result = 31 * result + (attributeMappingList != null ? attributeMappingList.hashCode() : 0);
        return result;
    }
}
