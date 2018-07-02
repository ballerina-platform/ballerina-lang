/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.caching;

import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.util.Map;

import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_MAX_AGE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_MAX_STALE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_MIN_FRESH_FIELD;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_NO_CACHE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_NO_STORE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_NO_TRANSFORM_FIELD;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_ONLY_IF_CACHED_FIELD;
import static org.ballerinalang.net.http.HttpUtil.TRUE;

/**
 * An abstraction for the RequestCacheControl struct. This can be used for creating and populating
 * RequestCacheControl structs based on the Cache-Control header.
 *
 * @since 0.965.0
 */
public class RequestCacheControlStruct {

    private BMap<String, BValue> requestCacheControl;

    public RequestCacheControlStruct(StructureTypeInfo structInfo) {
        requestCacheControl = BLangVMStructs.createBStruct(structInfo);

        // Initialize the struct fields to default values we use
        requestCacheControl.put(REQ_CACHE_CONTROL_NO_TRANSFORM_FIELD, new BBoolean(TRUE));
        requestCacheControl.put(REQ_CACHE_CONTROL_MAX_AGE_FIELD, new BInteger(-1));
        requestCacheControl.put(REQ_CACHE_CONTROL_MAX_STALE_FIELD, new BInteger(-1));
        requestCacheControl.put(REQ_CACHE_CONTROL_MIN_FRESH_FIELD, new BInteger(-1));
    }

    public RequestCacheControlStruct(BMap<String, BValue> requestCacheControl) {
        this.requestCacheControl = requestCacheControl;

        // Initialize the struct fields to default values we use
        requestCacheControl.put(REQ_CACHE_CONTROL_NO_TRANSFORM_FIELD, new BBoolean(TRUE));
        requestCacheControl.put(REQ_CACHE_CONTROL_MAX_AGE_FIELD, new BInteger(-1));
        requestCacheControl.put(REQ_CACHE_CONTROL_MAX_STALE_FIELD, new BInteger(-1));
        requestCacheControl.put(REQ_CACHE_CONTROL_MIN_FRESH_FIELD, new BInteger(-1));
    }

    public BMap<String, BValue> getStruct() {
        return requestCacheControl;
    }

    public RequestCacheControlStruct setNoCache(boolean noCache) {
        requestCacheControl.put(REQ_CACHE_CONTROL_NO_CACHE_FIELD, new BBoolean(noCache));
        return this;
    }

    public RequestCacheControlStruct setNoStore(boolean noStore) {
        requestCacheControl.put(REQ_CACHE_CONTROL_NO_STORE_FIELD, new BBoolean(noStore));
        return this;
    }

    public RequestCacheControlStruct setNoTransform(boolean noTransform) {
        requestCacheControl.put(REQ_CACHE_CONTROL_NO_TRANSFORM_FIELD, new BBoolean(noTransform));
        return this;
    }

    public RequestCacheControlStruct setOnlyIfCached(boolean onlyIfCached) {
        requestCacheControl.put(REQ_CACHE_CONTROL_ONLY_IF_CACHED_FIELD, new BBoolean(onlyIfCached));
        return this;
    }

    public RequestCacheControlStruct setMaxAge(long maxAge) {
        requestCacheControl.put(REQ_CACHE_CONTROL_MAX_AGE_FIELD, new BInteger(maxAge));
        return this;
    }

    public RequestCacheControlStruct setMaxStale(long maxStale) {
        requestCacheControl.put(REQ_CACHE_CONTROL_MAX_STALE_FIELD, new BInteger(maxStale));
        return this;
    }

    public RequestCacheControlStruct setMinFresh(long minFresh) {
        requestCacheControl.put(REQ_CACHE_CONTROL_MIN_FRESH_FIELD, new BInteger(minFresh));
        return this;
    }

    public void populateStruct(String cacheControlHeaderVal) {
        Map<CacheControlDirective, String> controlDirectives = CacheControlParser.parse(cacheControlHeaderVal);

        controlDirectives.forEach((directive, value) -> {
            switch (directive) {
                case NO_CACHE:
                    requestCacheControl.put(REQ_CACHE_CONTROL_NO_CACHE_FIELD, new BBoolean(TRUE));
                    break;
                case NO_STORE:
                    requestCacheControl.put(REQ_CACHE_CONTROL_NO_STORE_FIELD, new BBoolean(TRUE));
                    break;
                case NO_TRANSFORM:
                    requestCacheControl.put(REQ_CACHE_CONTROL_NO_TRANSFORM_FIELD, new BBoolean(TRUE));
                    break;
                case ONLY_IF_CACHED:
                    requestCacheControl.put(REQ_CACHE_CONTROL_ONLY_IF_CACHED_FIELD, new BBoolean(TRUE));
                    break;
                case MAX_AGE:
                    requestCacheControl.put(REQ_CACHE_CONTROL_MAX_AGE_FIELD, new BInteger(Long.parseLong(value)));
                    break;
                case MAX_STALE:
                    requestCacheControl.put(REQ_CACHE_CONTROL_MAX_STALE_FIELD, new BInteger(Long.parseLong(value)));
                    break;
                case MIN_FRESH:
                    requestCacheControl.put(REQ_CACHE_CONTROL_MIN_FRESH_FIELD, new BInteger(Long.parseLong(value)));
                    break;
                default:
                    break;
            }
        });
    }
}
