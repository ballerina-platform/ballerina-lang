/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.services.testutils;

import org.ballerinalang.net.http.HttpConstants;

/**
 * Utility for properly splitting and parsing cookie header.
 */
public class CookieUtils {

    public static Cookie getCookie(String headerValue) {
        String[] headerSplits = headerValue.split(";");
        Cookie cookie = new Cookie();
        if (headerSplits.length > 0) {
            cookie.name = headerSplits[0].split("=")[0];
            cookie.value = headerSplits[0].split("=")[1];
            for (String headerSplit : headerSplits) {
                if (headerSplit.trim().equals(HttpConstants.HTTP_ONLY)) {
                    cookie.httpOnly = true;
                } else if (headerSplit.trim().equals(HttpConstants.SECURE)) {
                    cookie.secure = true;
                } else if (headerSplit.trim().startsWith(HttpConstants.PATH)) {
                    cookie.path = headerSplit.split("=")[1];
                }
            }
        }
        return cookie;
    }

    /**
     * Static Cookie class.
     */
    public static class Cookie {
        public String name;
        public String value;
        public String path;
        public boolean secure;
        public boolean httpOnly;

        @Override
        public String toString() {
            return "Cookie{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    ", path='" + path + '\'' +
                    ", secure=" + secure +
                    ", httpOnly=" + httpOnly +
                    '}';
        }
    }
}
