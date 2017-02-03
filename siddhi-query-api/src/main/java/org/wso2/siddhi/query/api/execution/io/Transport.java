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

package org.wso2.siddhi.query.api.execution.io;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Transport {
    private String type;
    private Map<String, String> options = new HashMap<String, String>();
    private Map<String, String> dynamicOptions = new HashMap<String, String>();

    private Transport(String type) {
        this.type = type;
    }

    public static Transport transport(String type) {
        return new Transport(type);
    }

    public Transport option(String key, String value) {
        if (Pattern.matches("\\{\\{.*?}}", value)) {
            dynamicOptions.put(key, value);
        } else {
            options.put(key, value);
        }
        return this;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public Map<String, String> getDynamicOptions() {
        return dynamicOptions;
    }

    @Override
    public String toString() {
        return "Transport{" +
                "type='" + type + '\'' +
                ", options=" + options +
                ", dynamicOptions=" + dynamicOptions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transport transport = (Transport) o;

        if (type != null ? !type.equals(transport.type) : transport.type != null) return false;
        else if (options != null ? !options.equals(transport.options) : transport.options != null) return false;
        else
            return dynamicOptions != null ? dynamicOptions.equals(transport.dynamicOptions) : transport.dynamicOptions == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (options != null ? options.hashCode() : 0);
        result = 31 * result + (dynamicOptions != null ? dynamicOptions.hashCode() : 0);
        return result;
    }
}
