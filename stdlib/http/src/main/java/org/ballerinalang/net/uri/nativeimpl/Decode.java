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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Extern function to decode URLs.
 * ballerina/http:decode
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "decode",
        args = {@Argument(name = "url", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING),
                      @ReturnType(type = TypeKind.RECORD, structType = "Error")},
        isPublic = true
)
public class Decode {
    public static Object decode(Strand strand, String url, String charset) {
        try {
            return URLDecoder.decode(url, charset);
        } catch (UnsupportedEncodingException e) {
            return HttpUtil.createHttpError("Error occurred while decoding the url. " + e.getMessage());
        }
    }
}
