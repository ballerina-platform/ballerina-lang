/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.uri.nativeimpl;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.net.http.HttpUtil;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Resolve a given path against a given URI.
 *
 * @since 0.975.0
 */
public class Resolve {
    public static Object resolve(BString url, BString path) {
        URI uri;
        try {
            uri = new URI(path.getValue());
            if (!uri.isAbsolute()) {
                // Url is not absolute, we need to resolve it.
                URI baseUri = new URI(url.getValue());
                uri = baseUri.resolve(uri.normalize());

            }
            return StringUtils.fromString(uri.toString());
        } catch (URISyntaxException e) {
            return HttpUtil.createHttpError("error occurred while resolving URI. " + e.getMessage());
        }
    }
}
