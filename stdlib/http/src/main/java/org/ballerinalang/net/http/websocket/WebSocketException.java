/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.TypeConstants;
import io.ballerina.runtime.api.Types;
import io.ballerina.runtime.api.types.BErrorType;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * Exceptions that could occur in WebSocket.
 *
 * @since 0.995
 */
public class WebSocketException extends BError {
    private final String message;

    public WebSocketException(Throwable ex, String typeIdName) {
        this(WebSocketConstants.ErrorCode.WsGenericError.errorCode().substring(2) + ":" +
                     WebSocketUtil.getErrorMessage(ex), typeIdName);
    }

    public WebSocketException(String message, String typeIdName) {
        this(message, BValueCreator.createMapValue(Types.TYPE_ERROR_DETAIL), typeIdName);
    }

    public WebSocketException(String message, BError cause, String typeIdName) {
        this(message, cause, BValueCreator.createMapValue(Types.TYPE_ERROR_DETAIL), typeIdName);
    }

    public WebSocketException(String message, BMap<BString, Object> details, String typeIdName) {
        super(new BErrorType(TypeConstants.ERROR, Types.TYPE_ERROR.getPackage(), TypeChecker.getType(details)),
              BStringUtils.fromString(message), null, details, typeIdName, WebSocketConstants.PROTOCOL_HTTP_PKG_ID);
        this.message = message;
    }

    public WebSocketException(String message, BError cause, BMap<BString, Object> details, String typeIdName) {
        super(new BErrorType(TypeConstants.ERROR, Types.TYPE_ERROR.getPackage(), TypeChecker.getType(details)),
              BStringUtils.fromString(message), cause, details, typeIdName, WebSocketConstants.PROTOCOL_HTTP_PKG_ID);
        this.message = message;
    }

    public String detailMessage() {
        return message;
    }
}
