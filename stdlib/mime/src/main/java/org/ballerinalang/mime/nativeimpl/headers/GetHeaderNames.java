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
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Set;
import java.util.TreeSet;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;

/**
 * Get all header names.
 *
 * @since 0.966.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getHeaderNames",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.ARRAY)},
        isPublic = true
)
public class GetHeaderNames {

    public static ArrayValue getHeaderNames(Strand strand, ObjectValue entityObj) {
        ArrayValue stringArray = new ArrayValueImpl(new BArrayType(BTypes.typeString));
        if (entityObj.getNativeData(ENTITY_HEADERS) == null) {
            return stringArray;
        }
        HttpHeaders httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            int i = 0;
            Set<String> distinctNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            distinctNames.addAll(httpHeaders.names());
            for (String headerName : distinctNames) {
                stringArray.add(i, headerName);
                i++;
            }
        }
        return stringArray;
    }
}
