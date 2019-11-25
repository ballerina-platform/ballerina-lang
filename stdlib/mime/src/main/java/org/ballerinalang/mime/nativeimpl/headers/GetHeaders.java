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

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.MimeUtil;

import java.util.List;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.HEADER_NOT_FOUND;

/**
 * Get all the header values associated with the given header name.
 *
 * @since 0.966.0
 */
public class GetHeaders {

    public static ArrayValue getHeaders(ObjectValue entityObj, String headerName) {
        if (entityObj.getNativeData(ENTITY_HEADERS) == null) {
            throw MimeUtil.createError(HEADER_NOT_FOUND, "Http header does not exist");
        }
        HttpHeaders httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        List<String> headerValueList = httpHeaders.getAll(headerName);
        if (headerValueList == null) {
            throw MimeUtil.createError(HEADER_NOT_FOUND, "Http header does not exist");
        }
        int i = 0;
        ArrayValue stringArray = new ArrayValueImpl(new BArrayType(BTypes.typeHandle));
        for (String headerValue : headerValueList) {
            stringArray.add(i, new HandleValue(headerValue));
            i++;
        }
        return stringArray;
    }
}
