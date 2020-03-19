/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under
 * this license, please see the license as well as any agreement you’ve
 * entered into with WSO2 governing the purchase of this software and any
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
