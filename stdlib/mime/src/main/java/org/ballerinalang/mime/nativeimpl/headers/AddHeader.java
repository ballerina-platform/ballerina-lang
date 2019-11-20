/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.mime.nativeimpl.headers;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.values.ObjectValue;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;

/**
 * Add the given header value against the given header.
 *
 * @since 0.966.0
 */
public class AddHeader {

    public static void addHeader(ObjectValue entityObj, String headerName, String headerValue) {
        if (headerName == null || headerValue == null) {
            return;
        }
        HttpHeaders httpHeaders;
        if (entityObj.getNativeData(ENTITY_HEADERS) != null) {
            httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        } else {
            httpHeaders = new DefaultHttpHeaders();
            entityObj.addNativeData(ENTITY_HEADERS, httpHeaders);
        }
        httpHeaders.add(headerName, headerValue);
    }
}
