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
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.List;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;

/**
 * Check the http header existence.
 *
 * @since 0.966.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "hasHeader",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        args = {@Argument(name = "headerName", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true
)
public class HasHeader {

    public static boolean hasHeader(Strand strand, ObjectValue entityObj, String headerName) {
        if (entityObj.getNativeData(ENTITY_HEADERS) == null) {
            return false;
        }
        HttpHeaders httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        List<String> headerValueList = httpHeaders.getAll(headerName);
        if (headerValueList == null || headerValueList.isEmpty()) {
            return false;
        }
        return true;
    }
}
