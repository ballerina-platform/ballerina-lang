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
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.StructureTypeInfo;

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
public class ResponseCacheControlStruct {

    private BMap<String, BValue> responseCacheControl;

    public ResponseCacheControlStruct(StructureTypeInfo structInfo) {
        responseCacheControl = BLangVMStructs.createBStruct(structInfo);
        init();
    }

    public ResponseCacheControlStruct(BMap<String, BValue> responseCacheControl) {
        this.responseCacheControl = responseCacheControl;
    }

    public BMap<String, BValue> getStruct() {
        return responseCacheControl;
    }

    public void setStruct(BMap<String, BValue> responseCacheControl) {
        this.responseCacheControl = responseCacheControl;
    }

    public void init() {
        // Initialize the struct fields to default values we use
        responseCacheControl.put(RES_CACHE_CONTROL_NO_TRANSFORM_FIELD, new BBoolean(TRUE));
        responseCacheControl.put(RES_CACHE_CONTROL_MAX_AGE_FIELD, new BInteger(-1));
        responseCacheControl.put(RES_CACHE_CONTROL_S_MAXAGE_FIELD, new BInteger(-1));
    }

    public ResponseCacheControlStruct setMustRevalidate(boolean mustRevalidate) {
        responseCacheControl.put(RES_CACHE_CONTROL_MUST_REVALIDATE_FIELD, new BBoolean(mustRevalidate));
        return this;
    }

    public ResponseCacheControlStruct setNoCache(boolean noCache) {
        responseCacheControl.put(RES_CACHE_CONTROL_NO_CACHE_FIELD, new BBoolean(noCache));
        return this;
    }

    public ResponseCacheControlStruct setNoCache(boolean noCache, String[] noCacheFields) {
        responseCacheControl.put(RES_CACHE_CONTROL_NO_CACHE_FIELD, new BBoolean(noCache));
        responseCacheControl.put(RES_CACHE_CONTROL_NO_CACHE_FIELDS_FIELD, new BStringArray(noCacheFields));
        return this;
    }

    public ResponseCacheControlStruct setNoStore(boolean noStore) {
        responseCacheControl.put(RES_CACHE_CONTROL_NO_STORE_FIELD, new BBoolean(noStore));
        return this;
    }

    public ResponseCacheControlStruct setNoTransform(boolean noTransform) {
        responseCacheControl.put(RES_CACHE_CONTROL_NO_TRANSFORM_FIELD, new BBoolean(noTransform));
        return this;
    }

    public ResponseCacheControlStruct setPrivate(boolean isPrivate) {
        responseCacheControl.put(RES_CACHE_CONTROL_IS_PRIVATE_FIELD, new BBoolean(isPrivate));
        return this;
    }

    public ResponseCacheControlStruct setPrivate(boolean isPrivate, String[] privateFields) {
        responseCacheControl.put(RES_CACHE_CONTROL_IS_PRIVATE_FIELD, new BBoolean(isPrivate));
        responseCacheControl.put(RES_CACHE_CONTROL_PRIVATE_FIELDS_FIELD, new BStringArray(privateFields));
        return this;
    }

    public ResponseCacheControlStruct setProxyRevalidate(boolean proxyRevalidate) {
        responseCacheControl.put(RES_CACHE_CONTROL_PROXY_REVALIDATE_FIELD, new BBoolean(proxyRevalidate));
        return this;
    }

    public ResponseCacheControlStruct setMaxAge(long maxAge) {
        responseCacheControl.put(RES_CACHE_CONTROL_MAX_AGE_FIELD, new BInteger(maxAge));
        return this;
    }

    public ResponseCacheControlStruct setSMaxAge(long sMaxAge) {
        responseCacheControl.put(RES_CACHE_CONTROL_S_MAXAGE_FIELD, new BInteger(sMaxAge));
        return this;
    }

    public void populateStruct(String cacheControlHeaderVal) {
        Map<CacheControlDirective, String> controlDirectives = CacheControlParser.parse(cacheControlHeaderVal);

        controlDirectives.forEach((directive, value) -> {
            switch (directive) {
                case MUST_REVALIDATE:
                    responseCacheControl.put(RES_CACHE_CONTROL_MUST_REVALIDATE_FIELD, new BBoolean(TRUE));
                    break;
                case NO_CACHE:
                    responseCacheControl.put(RES_CACHE_CONTROL_NO_CACHE_FIELD, new BBoolean(TRUE));
                    if (value != null) {
                        value = value.replace("\"", "");
                        responseCacheControl.put(RES_CACHE_CONTROL_NO_CACHE_FIELDS_FIELD,
                                                         new BStringArray(value.split(",")));
                    }
                    break;
                case NO_STORE:
                    responseCacheControl.put(RES_CACHE_CONTROL_NO_STORE_FIELD, new BBoolean(TRUE));
                    break;
                case NO_TRANSFORM:
                    responseCacheControl.put(RES_CACHE_CONTROL_NO_TRANSFORM_FIELD, new BBoolean(TRUE));
                    break;
                case PRIVATE:
                    responseCacheControl.put(RES_CACHE_CONTROL_IS_PRIVATE_FIELD, new BBoolean(TRUE));
                    if (value != null) {
                        value = value.replace("\"", "");
                        responseCacheControl.put(RES_CACHE_CONTROL_PRIVATE_FIELDS_FIELD,
                                                         new BStringArray(value.split(",")));
                    }
                    break;
                case PUBLIC:
                    responseCacheControl.put(RES_CACHE_CONTROL_IS_PRIVATE_FIELD, new BBoolean(FALSE));
                    break;
                case PROXY_REVALIDATE:
                    responseCacheControl.put(RES_CACHE_CONTROL_PROXY_REVALIDATE_FIELD, new BBoolean(TRUE));
                    break;
                case MAX_AGE:
                    responseCacheControl.put(RES_CACHE_CONTROL_MAX_AGE_FIELD, new BInteger(Long.parseLong(value)));
                    break;
                case S_MAXAGE:
                    responseCacheControl.put(RES_CACHE_CONTROL_S_MAXAGE_FIELD, new BInteger(Long.parseLong(value)));
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
                    (BStringArray) responseCacheControl.get(RES_CACHE_CONTROL_NO_CACHE_FIELDS_FIELD)));
        }

        if (getBooleanValue(responseCacheControl, RES_CACHE_CONTROL_NO_STORE_FIELD) == TRUE) {
            directivesBuilder.add("no-store");
        }

        if (getBooleanValue(responseCacheControl, RES_CACHE_CONTROL_NO_TRANSFORM_FIELD) == TRUE) {
            directivesBuilder.add("no-transform");
        }

        if (getBooleanValue(responseCacheControl, RES_CACHE_CONTROL_IS_PRIVATE_FIELD) == TRUE) {
            directivesBuilder.add("private" + appendFields(
                    (BStringArray) responseCacheControl.get(RES_CACHE_CONTROL_PRIVATE_FIELDS_FIELD)));
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

    private String appendFields(BStringArray values) {
        if (values.size() > 0) {
            StringJoiner joiner = new StringJoiner(",");

            for (int i = 0; i < values.size(); i++) {
                joiner.add(values.get(i));
            }

            return "=\"" + joiner.toString() + "\"";
        }

        return "";
    }

    private boolean getBooleanValue(BMap<String, BValue> responseCacheControl, String fieldName) {
        return ((BBoolean) responseCacheControl.get(fieldName)).booleanValue();
    }

    private long getIntValue(BMap<String, BValue> responseCacheControl, String fieldName) {
        return ((BInteger) responseCacheControl.get(fieldName)).intValue();
    }
}
