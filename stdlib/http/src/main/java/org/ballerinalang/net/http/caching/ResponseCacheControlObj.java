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

import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.runtime.Module;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.Map;
import java.util.StringJoiner;

import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_IS_PRIVATE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_MAX_AGE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_MUST_REVALIDATE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_NO_CACHE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_NO_CACHE_FIELDS_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_NO_STORE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_NO_TRANSFORM_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_PRIVATE_FIELDS_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_PROXY_REVALIDATE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_S_MAXAGE_FIELD;
import static org.ballerinalang.net.http.HttpUtil.FALSE;
import static org.ballerinalang.net.http.HttpUtil.TRUE;

/**
 * An abstraction for the ResponseCacheControl struct. This can be used for creating and populating
 * ResponseCacheControl structs based on the Cache-Control header.
 *
 * @since 0.965.0
 */
public class ResponseCacheControlObj {

    private BObject responseCacheControl;

    public ResponseCacheControlObj(Module bPackage, String objectTypeName) {
        responseCacheControl = BValueCreator.createObjectValue(bPackage, objectTypeName);
        init();
    }

    public ResponseCacheControlObj(BObject responseCacheControl) {
        this.responseCacheControl = responseCacheControl;
    }

        public BObject getObj() {
        return responseCacheControl;
    }

    public void setObj(BObject responseCacheControl) {
        this.responseCacheControl = responseCacheControl;
    }

    public void init() {
        // Initialize the struct fields to default values we use
        responseCacheControl.set(RES_CACHE_CONTROL_NO_TRANSFORM_FIELD, TRUE);
        responseCacheControl.set(RES_CACHE_CONTROL_MAX_AGE_FIELD, -1);
        responseCacheControl.set(RES_CACHE_CONTROL_S_MAXAGE_FIELD, -1);
    }

    public ResponseCacheControlObj setMustRevalidate(boolean mustRevalidate) {
        responseCacheControl.set(RES_CACHE_CONTROL_MUST_REVALIDATE_FIELD, mustRevalidate);
        return this;
    }

    public ResponseCacheControlObj setNoCache(boolean noCache) {
        responseCacheControl.set(RES_CACHE_CONTROL_NO_CACHE_FIELD, noCache);
        return this;
    }

    public ResponseCacheControlObj setNoCache(boolean noCache, String[] noCacheFields) {
        responseCacheControl.set(RES_CACHE_CONTROL_NO_CACHE_FIELD, noCache);
        responseCacheControl.set(RES_CACHE_CONTROL_NO_CACHE_FIELDS_FIELD, noCacheFields);
        return this;
    }

    public ResponseCacheControlObj setNoStore(boolean noStore) {
        responseCacheControl.set(RES_CACHE_CONTROL_NO_STORE_FIELD, noStore);
        return this;
    }

    public ResponseCacheControlObj setNoTransform(boolean noTransform) {
        responseCacheControl.set(RES_CACHE_CONTROL_NO_TRANSFORM_FIELD, noTransform);
        return this;
    }

    public ResponseCacheControlObj setPrivate(boolean isPrivate) {
        responseCacheControl.set(RES_CACHE_CONTROL_IS_PRIVATE_FIELD, isPrivate);
        return this;
    }

    public ResponseCacheControlObj setPrivate(boolean isPrivate, String[] privateFields) {
        responseCacheControl.set(RES_CACHE_CONTROL_IS_PRIVATE_FIELD, isPrivate);
        responseCacheControl.set(RES_CACHE_CONTROL_PRIVATE_FIELDS_FIELD, privateFields);
        return this;
    }

    public ResponseCacheControlObj setProxyRevalidate(boolean proxyRevalidate) {
        responseCacheControl.set(RES_CACHE_CONTROL_PROXY_REVALIDATE_FIELD, proxyRevalidate);
        return this;
    }

    public ResponseCacheControlObj setMaxAge(long maxAge) {
        responseCacheControl.set(RES_CACHE_CONTROL_MAX_AGE_FIELD, maxAge);
        return this;
    }

    public ResponseCacheControlObj setSMaxAge(long sMaxAge) {
        responseCacheControl.set(RES_CACHE_CONTROL_S_MAXAGE_FIELD, sMaxAge);
        return this;
    }

    public void populateStruct(String cacheControlHeaderVal) {
        Map<CacheControlDirective, String> controlDirectives = CacheControlParser.parse(cacheControlHeaderVal);

        controlDirectives.forEach((directive, value) -> {
            switch (directive) {
                case MUST_REVALIDATE:
                    responseCacheControl.set(RES_CACHE_CONTROL_MUST_REVALIDATE_FIELD, TRUE);
                    break;
                case NO_CACHE:
                    responseCacheControl.set(RES_CACHE_CONTROL_NO_CACHE_FIELD, TRUE);
                    if (value != null) {
                        value = value.replace("\"", "");
                        responseCacheControl.set(RES_CACHE_CONTROL_NO_CACHE_FIELDS_FIELD,
                                                         BValueCreator.createArrayValue(value.split(",")));
                    }
                    break;
                case NO_STORE:
                    responseCacheControl.set(RES_CACHE_CONTROL_NO_STORE_FIELD, TRUE);
                    break;
                case NO_TRANSFORM:
                    responseCacheControl.set(RES_CACHE_CONTROL_NO_TRANSFORM_FIELD, TRUE);
                    break;
                case PRIVATE:
                    responseCacheControl.set(RES_CACHE_CONTROL_IS_PRIVATE_FIELD, TRUE);
                    if (value != null) {
                        value = value.replace("\"", "");
                        responseCacheControl.set(RES_CACHE_CONTROL_PRIVATE_FIELDS_FIELD,
                                                         BValueCreator.createArrayValue(value.split(",")));
                    }
                    break;
                case PUBLIC:
                    responseCacheControl.set(RES_CACHE_CONTROL_IS_PRIVATE_FIELD, FALSE);
                    break;
                case PROXY_REVALIDATE:
                    responseCacheControl.set(RES_CACHE_CONTROL_PROXY_REVALIDATE_FIELD, TRUE);
                    break;
                case MAX_AGE:
                    try {
                        responseCacheControl.set(RES_CACHE_CONTROL_MAX_AGE_FIELD, Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        // Ignore the exception and set 0 as the max-age so that it will be treated as a stale response.
                        // Note that this won't change the value of the actual cache-control header
                        responseCacheControl.set(RES_CACHE_CONTROL_MAX_AGE_FIELD, 0);
                    }
                    break;
                case S_MAXAGE:
                    try {
                        responseCacheControl.set(RES_CACHE_CONTROL_S_MAXAGE_FIELD, Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        // Ignore the exception and set 0 as the s-maxage.
                        responseCacheControl.set(RES_CACHE_CONTROL_S_MAXAGE_FIELD, 0);
                    }
                    break;
                default:
                    break;
            }
        });
    }

    public String buildCacheControlDirectives() {
        StringJoiner directivesBuilder = new StringJoiner(",");

        if (getBooleanValue(responseCacheControl, RES_CACHE_CONTROL_MUST_REVALIDATE_FIELD) == TRUE) {
            directivesBuilder.add("must-revalidate");
        }

        if (getBooleanValue(responseCacheControl, RES_CACHE_CONTROL_NO_CACHE_FIELD) == TRUE) {
            directivesBuilder.add("no-cache" + appendFields(
                    (BArray) responseCacheControl.get(RES_CACHE_CONTROL_NO_CACHE_FIELDS_FIELD)));
        }

        if (getBooleanValue(responseCacheControl, RES_CACHE_CONTROL_NO_STORE_FIELD) == TRUE) {
            directivesBuilder.add("no-store");
        }

        if (getBooleanValue(responseCacheControl, RES_CACHE_CONTROL_NO_TRANSFORM_FIELD) == TRUE) {
            directivesBuilder.add("no-transform");
        }

        if (getBooleanValue(responseCacheControl, RES_CACHE_CONTROL_IS_PRIVATE_FIELD) == TRUE) {
            directivesBuilder.add("private" + appendFields(
                    (BArray) responseCacheControl.get(RES_CACHE_CONTROL_PRIVATE_FIELDS_FIELD)));
        } else {
            directivesBuilder.add("public");
        }

        if (getBooleanValue(responseCacheControl, RES_CACHE_CONTROL_PROXY_REVALIDATE_FIELD) == TRUE) {
            directivesBuilder.add("proxy-revalidate");
        }

        if (getIntValue(responseCacheControl, RES_CACHE_CONTROL_MAX_AGE_FIELD) >= 0) {
            directivesBuilder.add("max-age=" + responseCacheControl.get(RES_CACHE_CONTROL_MAX_AGE_FIELD));
        }

        if (getIntValue(responseCacheControl, RES_CACHE_CONTROL_S_MAXAGE_FIELD) >= 0) {
            directivesBuilder.add("s-maxage=" + responseCacheControl.get(RES_CACHE_CONTROL_S_MAXAGE_FIELD));
        }

        return directivesBuilder.toString();
    }

    private String appendFields(BArray values) {
        if (values.size() > 0) {
            StringJoiner joiner = new StringJoiner(",");

            for (int i = 0; i < values.size(); i++) {
                joiner.add(values.getString(i));
            }

            return "=\"" + joiner.toString() + "\"";
        }

        return "";
    }

    private boolean getBooleanValue(BObject responseCacheControl, BString fieldName) {
        return (Boolean) responseCacheControl.get(fieldName);
    }

    private long getIntValue(BObject responseCacheControl, BString fieldName) {
        return Long.parseLong(responseCacheControl.get(fieldName).toString());
    }
}
