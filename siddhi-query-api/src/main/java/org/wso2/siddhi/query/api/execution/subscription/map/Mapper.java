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

package org.wso2.siddhi.query.api.execution.subscription.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mapper {
    private String type;
    private Map<String, String> options = new HashMap<String, String>();
    private List<Mapping> mappingList = new ArrayList<Mapping>();

    private Mapper(String type) {
        this.type = type;
    }

    public static Mapper mapper(String type) {
        return new Mapper(type);
    }

    public Mapper option(String key, String value) {
        options.put(key, value);
        return this;
    }

    public Mapper map(String mapping) {
        mappingList.add(new Mapping(mapping));
        return null;
    }

    public Mapper map(String rename, String mapping) {
        mappingList.add(new Mapping(rename, mapping));
        return null;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public List<Mapping> getMappingList() {
        return mappingList;
    }
}
