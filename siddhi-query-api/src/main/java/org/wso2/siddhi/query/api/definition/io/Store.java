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

package org.wso2.siddhi.query.api.definition.io;

import java.util.HashMap;
import java.util.Map;

public class Store {
    private String type;
    private Map<String, String> options = new HashMap<String, String>();

    private Store(String type){
        this.type = type;
    }

    public static Store store(String type) {
        return new Store(type);
    }

    public Store option(String key, String value) {
        options.put(key, value);
        return this;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "Store{" +
                "type='" + type + '\'' +
                ", options=" + options +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Store store = (Store) o;

        if (type != null ? !type.equals(store.type) : store.type != null) return false;
        return !(options != null ? !options.equals(store.options) : store.options != null);

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (options != null ? options.hashCode() : 0);
        return result;
    }
}
