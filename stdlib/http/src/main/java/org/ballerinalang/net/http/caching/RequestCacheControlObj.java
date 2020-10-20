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

import io.ballerina.runtime.api.values.BObject;

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
public class RequestCacheControlObj {

    private BObject requestCacheControl;

    public RequestCacheControlObj(BObject requestCacheControl) {
        this.requestCacheControl = requestCacheControl;

        // Initialize the struct fields to default values we use
        requestCacheControl.set(REQ_CACHE_CONTROL_NO_TRANSFORM_FIELD, TRUE);
        requestCacheControl.set(REQ_CACHE_CONTROL_MAX_AGE_FIELD, -1);
        requestCacheControl.set(REQ_CACHE_CONTROL_MAX_STALE_FIELD, -1);
        requestCacheControl.set(REQ_CACHE_CONTROL_MIN_FRESH_FIELD, -1);
    }

    public BObject getObj() {
        return requestCacheControl;
    }

    public RequestCacheControlObj setNoCache(boolean noCache) {
        requestCacheControl.set(REQ_CACHE_CONTROL_NO_CACHE_FIELD, noCache);
        return this;
    }

    public RequestCacheControlObj setNoStore(boolean noStore) {
        requestCacheControl.set(REQ_CACHE_CONTROL_NO_STORE_FIELD, noStore);
        return this;
    }

    public RequestCacheControlObj setNoTransform(boolean noTransform) {
        requestCacheControl.set(REQ_CACHE_CONTROL_NO_TRANSFORM_FIELD, noTransform);
        return this;
    }

    public RequestCacheControlObj setOnlyIfCached(boolean onlyIfCached) {
        requestCacheControl.set(REQ_CACHE_CONTROL_ONLY_IF_CACHED_FIELD, onlyIfCached);
        return this;
    }

    public RequestCacheControlObj setMaxAge(long maxAge) {
        requestCacheControl.set(REQ_CACHE_CONTROL_MAX_AGE_FIELD, maxAge);
        return this;
    }

    public RequestCacheControlObj setMaxStale(long maxStale) {
        requestCacheControl.set(REQ_CACHE_CONTROL_MAX_STALE_FIELD, maxStale);
        return this;
    }

    public RequestCacheControlObj setMinFresh(long minFresh) {
        requestCacheControl.set(REQ_CACHE_CONTROL_MIN_FRESH_FIELD, minFresh);
        return this;
    }

    public void populateStruct(String cacheControlHeaderVal) {
        Map<CacheControlDirective, String> controlDirectives = CacheControlParser.parse(cacheControlHeaderVal);

        controlDirectives.forEach((directive, value) -> {
            switch (directive) {
                case NO_CACHE:
                    requestCacheControl.set(REQ_CACHE_CONTROL_NO_CACHE_FIELD, TRUE);
                    break;
                case NO_STORE:
                    requestCacheControl.set(REQ_CACHE_CONTROL_NO_STORE_FIELD, TRUE);
                    break;
                case NO_TRANSFORM:
                    requestCacheControl.set(REQ_CACHE_CONTROL_NO_TRANSFORM_FIELD, TRUE);
                    break;
                case ONLY_IF_CACHED:
                    requestCacheControl.set(REQ_CACHE_CONTROL_ONLY_IF_CACHED_FIELD, TRUE);
                    break;
                case MAX_AGE:
                    try {
                        requestCacheControl.set(REQ_CACHE_CONTROL_MAX_AGE_FIELD, Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        // Ignore the exception and set max-age to 0.
                        requestCacheControl.set(REQ_CACHE_CONTROL_MAX_AGE_FIELD, 0);
                    }
                    break;
                case MAX_STALE:
                    try {
                        requestCacheControl.set(REQ_CACHE_CONTROL_MAX_STALE_FIELD, Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        // Ignore the exception and set max-stale to 0.
                        requestCacheControl.set(REQ_CACHE_CONTROL_MAX_STALE_FIELD, 0);
                    }
                    break;
                case MIN_FRESH:
                    try {
                        requestCacheControl.set(REQ_CACHE_CONTROL_MIN_FRESH_FIELD, Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        // Ignore the exception and set min-fresh to 0.
                        requestCacheControl.set(REQ_CACHE_CONTROL_MIN_FRESH_FIELD, 0);
                    }
                    break;
                default:
                    break;
            }
        });
    }
}
