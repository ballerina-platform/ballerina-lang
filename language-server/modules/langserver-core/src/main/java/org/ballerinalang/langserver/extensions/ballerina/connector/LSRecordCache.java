/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.connector;

import com.google.gson.JsonElement;

import java.util.HashMap;

/**
 * Cache to contain record information.
 */
public class LSRecordCache {

    private static final ConnectorExtContext.Key<LSRecordCache> LS_RECORD_CACHE_KEY = new ConnectorExtContext.Key<>();

    private static final Object LOCK = new Object();

    HashMap<String, JsonElement> recordASTs;

    public static LSRecordCache getInstance(ConnectorExtContext context) {
        LSRecordCache recordCache = context.get(LS_RECORD_CACHE_KEY);
        if (recordCache == null) {
            synchronized (LOCK) {
                recordCache = context.get(LS_RECORD_CACHE_KEY);
                if (recordCache == null) {
                    recordCache = new LSRecordCache();
                    context.put(LS_RECORD_CACHE_KEY, recordCache);
                }
            }
        }
        return recordCache;
    }

    private LSRecordCache() {
        recordASTs = new HashMap<>();
    }

    public void addRecordAST(String org, String module, String version, String record,
                             JsonElement ast) {
        this.recordASTs.put(createKey(org, module, version, record), ast);
    }

    public JsonElement getRecordAST(String org, String module, String version, String record) {
        return this.recordASTs.get(createKey(org, module, version, record));
    }

    private String createKey(String org, String module, String version, String record) {
        return org + "-" + module + "-" + version + "-" + record;
    }
}
