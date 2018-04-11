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
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.StructInfo;

import java.util.Map;
import java.util.StringJoiner;

import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_IS_PRIVATE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_MAX_AGE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_MUST_REVALIDATE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_NO_CACHE_FIELDS_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_NO_CACHE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_NO_STORE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_NO_TRANSFORM_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_PRIVATE_FIELDS_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_PROXY_REVALIDATE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RES_CACHE_CONTROL_S_MAXAGE_INDEX;
import static org.ballerinalang.net.http.HttpUtil.FALSE;
import static org.ballerinalang.net.http.HttpUtil.TRUE;

/**
 * An abstraction for the ResponseCacheControl struct. This can be used for creating and populating
 * ResponseCacheControl structs based on the Cache-Control header.
 *
 * @since 0.965.0
 */
public class ResponseCacheControlStruct {

    private BStruct responseCacheControl;

    public ResponseCacheControlStruct(StructInfo structInfo) {
        responseCacheControl = BLangVMStructs.createBStruct(structInfo);

        // Initialize the struct fields to default values we use
        responseCacheControl.setBooleanField(RES_CACHE_CONTROL_NO_TRANSFORM_INDEX, 1);
        responseCacheControl.setIntField(RES_CACHE_CONTROL_MAX_AGE_INDEX, -1);
        responseCacheControl.setIntField(RES_CACHE_CONTROL_S_MAXAGE_INDEX, -1);
    }

    public ResponseCacheControlStruct(BStruct responseCacheControl, boolean initToDefaults) {
        this.responseCacheControl = responseCacheControl;

        if (initToDefaults) {
            // Initialize the struct fields to default values we use
            responseCacheControl.setBooleanField(RES_CACHE_CONTROL_NO_TRANSFORM_INDEX, 1);
            responseCacheControl.setIntField(RES_CACHE_CONTROL_MAX_AGE_INDEX, -1);
            responseCacheControl.setIntField(RES_CACHE_CONTROL_S_MAXAGE_INDEX, -1);
        }
    }

    public BStruct getStruct() {
        return responseCacheControl;
    }

    public void setStruct(BStruct responseCacheControl) {
        this.responseCacheControl = responseCacheControl;
    }

    public ResponseCacheControlStruct setMustRevalidate(boolean mustRevalidate) {
        responseCacheControl.setBooleanField(RES_CACHE_CONTROL_MUST_REVALIDATE_INDEX, mustRevalidate ? TRUE : FALSE);
        return this;
    }

    public ResponseCacheControlStruct setNoCache(boolean noCache) {
        responseCacheControl.setBooleanField(RES_CACHE_CONTROL_NO_CACHE_INDEX, noCache ? TRUE : FALSE);
        return this;
    }

    public ResponseCacheControlStruct setNoCache(boolean noCache, String[] noCacheFields) {
        responseCacheControl.setBooleanField(RES_CACHE_CONTROL_NO_CACHE_INDEX, noCache ? TRUE : FALSE);
        responseCacheControl.setRefField(RES_CACHE_CONTROL_NO_CACHE_FIELDS_INDEX, new BStringArray(noCacheFields));
        return this;
    }

    public ResponseCacheControlStruct setNoStore(boolean noStore) {
        responseCacheControl.setBooleanField(RES_CACHE_CONTROL_NO_STORE_INDEX, noStore ? TRUE : FALSE);
        return this;
    }

    public ResponseCacheControlStruct setNoTransform(boolean noTransform) {
        responseCacheControl.setBooleanField(RES_CACHE_CONTROL_NO_TRANSFORM_INDEX, noTransform ? TRUE : FALSE);
        return this;
    }

    public ResponseCacheControlStruct setPrivate(boolean isPrivate) {
        responseCacheControl.setBooleanField(RES_CACHE_CONTROL_IS_PRIVATE_INDEX, isPrivate ? TRUE : FALSE);
        return this;
    }

    public ResponseCacheControlStruct setPrivate(boolean isPrivate, String[] privateFields) {
        responseCacheControl.setBooleanField(RES_CACHE_CONTROL_IS_PRIVATE_INDEX, isPrivate ? TRUE : FALSE);
        responseCacheControl.setRefField(RES_CACHE_CONTROL_PRIVATE_FIELDS_INDEX, new BStringArray(privateFields));
        return this;
    }

    public ResponseCacheControlStruct setProxyRevalidate(boolean proxyRevalidate) {
        responseCacheControl.setBooleanField(RES_CACHE_CONTROL_PROXY_REVALIDATE_INDEX, proxyRevalidate ? TRUE : FALSE);
        return this;
    }

    public ResponseCacheControlStruct setMaxAge(long maxAge) {
        responseCacheControl.setIntField(RES_CACHE_CONTROL_MAX_AGE_INDEX, maxAge);
        return this;
    }

    public ResponseCacheControlStruct setSMaxAge(long sMaxAge) {
        responseCacheControl.setIntField(RES_CACHE_CONTROL_S_MAXAGE_INDEX, sMaxAge);
        return this;
    }

    public void populateStruct(String cacheControlHeaderVal) {
        Map<CacheControlDirective, String> controlDirectives = CacheControlParser.parse(cacheControlHeaderVal);

        controlDirectives.forEach((directive, value) -> {
            switch (directive) {
                case MUST_REVALIDATE:
                    responseCacheControl.setBooleanField(RES_CACHE_CONTROL_MUST_REVALIDATE_INDEX, TRUE);
                    break;
                case NO_CACHE:
                    responseCacheControl.setBooleanField(RES_CACHE_CONTROL_NO_CACHE_INDEX, TRUE);
                    if (value != null) {
                        value = value.replace("\"", "");
                        responseCacheControl.setRefField(RES_CACHE_CONTROL_NO_CACHE_FIELDS_INDEX,
                                                         new BStringArray(value.split(",")));
                    }
                    break;
                case NO_STORE:
                    responseCacheControl.setBooleanField(RES_CACHE_CONTROL_NO_STORE_INDEX, TRUE);
                    break;
                case NO_TRANSFORM:
                    responseCacheControl.setBooleanField(RES_CACHE_CONTROL_NO_TRANSFORM_INDEX, TRUE);
                    break;
                case PRIVATE:
                    responseCacheControl.setBooleanField(RES_CACHE_CONTROL_IS_PRIVATE_INDEX, TRUE);
                    if (value != null) {
                        value = value.replace("\"", "");
                        responseCacheControl.setRefField(RES_CACHE_CONTROL_PRIVATE_FIELDS_INDEX,
                                                         new BStringArray(value.split(",")));
                    }
                    break;
                case PUBLIC:
                    responseCacheControl.setBooleanField(RES_CACHE_CONTROL_IS_PRIVATE_INDEX, FALSE);
                    break;
                case PROXY_REVALIDATE:
                    responseCacheControl.setBooleanField(RES_CACHE_CONTROL_PROXY_REVALIDATE_INDEX, TRUE);
                    break;
                case MAX_AGE:
                    responseCacheControl.setIntField(RES_CACHE_CONTROL_MAX_AGE_INDEX, Long.parseLong(value));
                    break;
                case S_MAXAGE:
                    responseCacheControl.setIntField(RES_CACHE_CONTROL_S_MAXAGE_INDEX, Long.parseLong(value));
                    break;
                default:
                    break;
            }
        });
    }

    public String buildCacheControlDirectives() {
        StringJoiner directivesBuilder = new StringJoiner(",");

        if (responseCacheControl.getBooleanField(RES_CACHE_CONTROL_MUST_REVALIDATE_INDEX) == TRUE) {
            directivesBuilder.add("must-revalidate");
        }

        if (responseCacheControl.getBooleanField(RES_CACHE_CONTROL_NO_CACHE_INDEX) == TRUE) {
            directivesBuilder.add("no-cache" + appendFields(
                    (BStringArray) responseCacheControl.getRefField(RES_CACHE_CONTROL_NO_CACHE_FIELDS_INDEX)));
        }

        if (responseCacheControl.getBooleanField(RES_CACHE_CONTROL_NO_STORE_INDEX) == TRUE) {
            directivesBuilder.add("no-store");
        }

        if (responseCacheControl.getBooleanField(RES_CACHE_CONTROL_NO_TRANSFORM_INDEX) == TRUE) {
            directivesBuilder.add("no-transform");
        }

        if (responseCacheControl.getBooleanField(RES_CACHE_CONTROL_IS_PRIVATE_INDEX) == TRUE) {
            directivesBuilder.add("private" + appendFields(
                    (BStringArray) responseCacheControl.getRefField(RES_CACHE_CONTROL_PRIVATE_FIELDS_INDEX)));
        } else {
            directivesBuilder.add("public");
        }

        if (responseCacheControl.getBooleanField(RES_CACHE_CONTROL_PROXY_REVALIDATE_INDEX) == TRUE) {
            directivesBuilder.add("proxy-revalidate");
        }

        if (responseCacheControl.getIntField(RES_CACHE_CONTROL_MAX_AGE_INDEX) >= 0) {
            directivesBuilder.add("max-age=" + responseCacheControl.getIntField(RES_CACHE_CONTROL_MAX_AGE_INDEX));
        }

        if (responseCacheControl.getIntField(RES_CACHE_CONTROL_S_MAXAGE_INDEX) >= 0) {
            directivesBuilder.add("s-maxage=" + responseCacheControl.getIntField(RES_CACHE_CONTROL_S_MAXAGE_INDEX));
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
}
