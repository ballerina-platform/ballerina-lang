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

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.api.BArray;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.net.http.HttpUtil;

import java.util.Arrays;

import static org.ballerinalang.mime.util.MimeConstants.COMMA;
import static org.ballerinalang.mime.util.MimeConstants.FAILED_TO_PARSE;
import static org.ballerinalang.mime.util.MimeConstants.SEMICOLON;
import static org.ballerinalang.net.http.HttpErrorType.GENERIC_CLIENT_ERROR;

/**
 * Extern function to parse header value and get value with parameter map.
 *
 * @since 0.96.1
 */
public class ParseHeader {

    private static final BTupleType parseHeaderTupleType = new BTupleType(
            Arrays.asList(BTypes.typeString, BTypes.typeMap));

    public static Object parseHeader(BString headerValue) {
        if (headerValue == null) {
            return HttpUtil.createHttpError(FAILED_TO_PARSE + "header value cannot be null",
                                            GENERIC_CLIENT_ERROR);
        }
        try {
            if (headerValue.getValue().contains(COMMA)) {
                headerValue = headerValue.substring(0, headerValue.getValue().indexOf(COMMA));
            }
            // Set value and param map
            String value = headerValue.getValue().trim();
            if (headerValue.getValue().contains(SEMICOLON)) {
                value = HeaderUtil.getHeaderValue(value);
            }
            BArray contentTuple = BValueCreator.createTupleValue(parseHeaderTupleType);
            contentTuple.add(0, StringUtils.fromString(value));
            contentTuple.add(1, HeaderUtil.getParamMap(headerValue.getValue()));
            return contentTuple;
        } catch (Exception ex) {
            String errMsg = ex instanceof ErrorValue ? ex.toString() : ex.getMessage();
            return HttpUtil.createHttpError(FAILED_TO_PARSE + errMsg, GENERIC_CLIENT_ERROR);
        }
    }
}
