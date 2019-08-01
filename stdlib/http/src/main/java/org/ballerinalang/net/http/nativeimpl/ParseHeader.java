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
package org.ballerinalang.net.http.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpErrorType;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;

import java.util.Arrays;

import static org.ballerinalang.mime.util.MimeConstants.COMMA;
import static org.ballerinalang.mime.util.MimeConstants.PARSER_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.READING_HEADER_FAILED;
import static org.ballerinalang.mime.util.MimeConstants.SEMICOLON;

/**
 * Extern function to parse header value and get value with parameter map.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "parseHeader",
        returnType = {@ReturnType(type = TypeKind.STRING),
                @ReturnType(type = TypeKind.MAP, elementType = TypeKind.STRING),
                @ReturnType(type = TypeKind.RECORD, structType = "Error")},
        isPublic = true
)
public class ParseHeader {

    private static final org.ballerinalang.jvm.types.BTupleType parseHeaderTupleType = new org.ballerinalang.jvm
            .types.BTupleType(
            Arrays.asList(org.ballerinalang.jvm.types.BTypes.typeString, org.ballerinalang.jvm.types.BTypes.typeMap));

    public static Object parseHeader(Strand strand, String headerValue) {
        String errMsg;
        try {
            if (headerValue == null) {
                throw new BLangNullReferenceException(PARSER_ERROR + "header value cannot be null");
            }
            if (headerValue.contains(COMMA)) {
                headerValue = headerValue.substring(0, headerValue.indexOf(COMMA));
            }

            // Set value and param map
            String value = headerValue.trim();
            if (headerValue.contains(SEMICOLON)) {
                value = HeaderUtil.getHeaderValue(value);
            }
            ArrayValue contentTuple = new ArrayValue(parseHeaderTupleType);
            contentTuple.add(0, (Object) value);
            contentTuple.add(1, HeaderUtil.getParamMap(headerValue));
            return contentTuple;
        } catch (BLangNullReferenceException ex) {
            errMsg = ex.getMessage();
        } catch (BallerinaException ex) {
            errMsg = PARSER_ERROR + ex.getMessage();
        }

        // set parse error
        ErrorValue mimeError = MimeUtil.createError(READING_HEADER_FAILED, errMsg);
        String httpErrorMessage = "MimeError occurred while parsing the header";
        return HttpUtil.createHttpError(httpErrorMessage, HttpErrorType.GENERIC_CLIENT_ERROR, mimeError);
    }
}
