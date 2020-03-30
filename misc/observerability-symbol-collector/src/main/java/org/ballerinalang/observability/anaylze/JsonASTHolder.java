/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.observability.anaylze;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hold AST JSON data for later storage.
 */
public class JsonASTHolder {

    private static final JsonASTHolder dataHolder = new JsonASTHolder();

    private Map<String, JsonObject> astMap = new ConcurrentHashMap<>();

    public void addAST(String moduleId, JsonObject ast) {
        this.astMap.put(moduleId, ast);
    }

    public void clearASTMap() {
        this.astMap.clear();
    }

    public Map<String, JsonObject> getASTMap() {
        return astMap;
    }

    public static JsonASTHolder getInstance() {
        return dataHolder;
    }
}
