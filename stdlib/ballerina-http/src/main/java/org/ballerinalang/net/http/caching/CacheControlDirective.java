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

import java.util.Locale;

/**
 * Enum for cache control directives.
 *
 * @since 0.965.0
 */
public enum CacheControlDirective {

    MUST_REVALIDATE("must-revalidate"),
    NO_CACHE("no-cache"),
    NO_STORE("no-store"),
    NO_TRANSFORM("no-transform"),
    PRIVATE("private"),
    PUBLIC("public"),
    PROXY_REVALIDATE("proxy-revalidate"),
    MAX_AGE("max-age"),
    S_MAXAGE("s-maxage"),
    INVALID("invalid"),
    ONLY_IF_CACHED("only-if-cached"),
    MAX_STALE("max-stale"),
    MIN_FRESH("min-fresh");

    private String directiveValue;

    CacheControlDirective(String directiveValue) {
        this.directiveValue = directiveValue;
    }

    public String value() {
        return directiveValue;
    }

    public static CacheControlDirective parseValue(String directive) {
        try {
            return CacheControlDirective.valueOf(directive.toUpperCase(Locale.ENGLISH).replace('-', '_'));
        } catch (Exception e) {
            // ignore exception
            return CacheControlDirective.INVALID;
        }
    }
}
