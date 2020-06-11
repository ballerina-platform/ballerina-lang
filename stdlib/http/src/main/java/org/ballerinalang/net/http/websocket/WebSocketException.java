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

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BErrorType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValueImpl;

/**
 * Exceptions that could occur in WebSocket.
 *
 * @since 0.995
 */
public class WebSocketException extends ErrorValue {
    private final String message;

    public WebSocketException(Throwable ex) {
        this(WebSocketConstants.ErrorCode.WsGenericError.errorCode().substring(2) + ":" +
                WebSocketUtil.getErrorMessage(ex));
    }

    public WebSocketException(String message) {
        this(message, new MapValueImpl<>(BTypes.typeErrorDetail));
       }

    public WebSocketException(String message, ErrorValue cause) {
        this(message, cause, new MapValueImpl<>(BTypes.typeErrorDetail));
    }

    public WebSocketException(String message, MapValueImpl<Object, Object> details) {
        super(new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage(), TypeChecker.getType(details)),
                StringUtils.fromString(message), null, details);
        this.message = message;
    }

    public WebSocketException(String message, ErrorValue cause, MapValueImpl<Object, Object> details) {
        super(new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage(), TypeChecker.getType(details)),
                StringUtils.fromString(message), cause, details);
        this.message = message;
    }

    public String detailMessage() {
        return message;
    }
}
