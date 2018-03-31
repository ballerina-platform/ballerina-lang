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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.StructInfo;

import java.util.Map;

import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_MAX_AGE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_MAX_STALE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_MIN_FRESH_INDEX;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_NO_CACHE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_NO_STORE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_NO_TRANSFORM_INDEX;
import static org.ballerinalang.net.http.HttpConstants.REQ_CACHE_CONTROL_ONLY_IF_CACHED_INDEX;
import static org.ballerinalang.net.http.HttpUtil.FALSE;
import static org.ballerinalang.net.http.HttpUtil.TRUE;

/**
 * An abstraction for the RequestCacheControl struct. This can be used for creating and populating
 * RequestCacheControl structs based on the Cache-Control header.
 *
 * @since 0.965.0
 */
public class RequestCacheControlStruct {

    private BStruct requestCacheControl;

    public RequestCacheControlStruct(StructInfo structInfo) {
        requestCacheControl = BLangVMStructs.createBStruct(structInfo);

        // Initialize the struct fields to default values we use
        requestCacheControl.setBooleanField(REQ_CACHE_CONTROL_NO_TRANSFORM_INDEX, 1);
        requestCacheControl.setIntField(REQ_CACHE_CONTROL_MAX_AGE_INDEX, -1);
        requestCacheControl.setIntField(REQ_CACHE_CONTROL_MAX_STALE_INDEX, -1);
        requestCacheControl.setIntField(REQ_CACHE_CONTROL_MIN_FRESH_INDEX, -1);
    }

    public RequestCacheControlStruct(BStruct requestCacheControl) {
        this.requestCacheControl = requestCacheControl;

        // Initialize the struct fields to default values we use
        requestCacheControl.setBooleanField(REQ_CACHE_CONTROL_NO_TRANSFORM_INDEX, 1);
        requestCacheControl.setIntField(REQ_CACHE_CONTROL_MAX_AGE_INDEX, -1);
        requestCacheControl.setIntField(REQ_CACHE_CONTROL_MAX_STALE_INDEX, -1);
        requestCacheControl.setIntField(REQ_CACHE_CONTROL_MIN_FRESH_INDEX, -1);
    }

    public BStruct getStruct() {
        return requestCacheControl;
    }

    public RequestCacheControlStruct setNoCache(boolean noCache) {
        requestCacheControl.setBooleanField(REQ_CACHE_CONTROL_NO_CACHE_INDEX, noCache ? TRUE : FALSE);
        return this;
    }

    public RequestCacheControlStruct setNoStore(boolean noStore) {
        requestCacheControl.setBooleanField(REQ_CACHE_CONTROL_NO_STORE_INDEX, noStore ? TRUE : FALSE);
        return this;
    }

    public RequestCacheControlStruct setNoTransform(boolean noTransform) {
        requestCacheControl.setBooleanField(REQ_CACHE_CONTROL_NO_TRANSFORM_INDEX, noTransform ? TRUE : FALSE);
        return this;
    }

    public RequestCacheControlStruct setOnlyIfCached(boolean onlyIfCached) {
        requestCacheControl.setBooleanField(REQ_CACHE_CONTROL_ONLY_IF_CACHED_INDEX, onlyIfCached ? TRUE : FALSE);
        return this;
    }

    public RequestCacheControlStruct setMaxAge(long maxAge) {
        requestCacheControl.setIntField(REQ_CACHE_CONTROL_MAX_AGE_INDEX, maxAge);
        return this;
    }

    public RequestCacheControlStruct setMaxStale(long maxStale) {
        requestCacheControl.setIntField(REQ_CACHE_CONTROL_MAX_STALE_INDEX, maxStale);
        return this;
    }

    public RequestCacheControlStruct setMinFresh(long minFresh) {
        requestCacheControl.setIntField(REQ_CACHE_CONTROL_MIN_FRESH_INDEX, minFresh);
        return this;
    }

    public void populateStruct(String cacheControlHeaderVal) {
        Map<CacheControlDirective, String> controlDirectives = CacheControlParser.parse(cacheControlHeaderVal);

        controlDirectives.forEach((directive, value) -> {
            switch (directive) {
                case NO_CACHE:
                    requestCacheControl.setBooleanField(REQ_CACHE_CONTROL_NO_CACHE_INDEX, TRUE);
                    break;
                case NO_STORE:
                    requestCacheControl.setBooleanField(REQ_CACHE_CONTROL_NO_STORE_INDEX, TRUE);
                    break;
                case NO_TRANSFORM:
                    requestCacheControl.setBooleanField(REQ_CACHE_CONTROL_NO_TRANSFORM_INDEX, TRUE);
                    break;
                case ONLY_IF_CACHED:
                    requestCacheControl.setBooleanField(REQ_CACHE_CONTROL_ONLY_IF_CACHED_INDEX, TRUE);
                    break;
                case MAX_AGE:
                    requestCacheControl.setIntField(REQ_CACHE_CONTROL_MAX_AGE_INDEX, Long.parseLong(value));
                    break;
                case MAX_STALE:
                    requestCacheControl.setIntField(REQ_CACHE_CONTROL_MAX_STALE_INDEX, Long.parseLong(value));
                    break;
                case MIN_FRESH:
                    requestCacheControl.setIntField(REQ_CACHE_CONTROL_MIN_FRESH_INDEX, Long.parseLong(value));
                    break;
            }
        });
    }
}
