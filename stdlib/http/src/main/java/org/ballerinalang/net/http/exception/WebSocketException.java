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

package org.ballerinalang.net.http.exception;

import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketUtil;

import static org.ballerinalang.net.http.WebSocketConstants.ErrorCode.WsGenericError;

/**
 * BallerinaException that could occur in WebSocket.
 *
 * @since 0.995
 */
public class WebSocketException extends ErrorValue {
    private static final long serialVersionUID = 381055783364464822L;
    private final String message;

    /**
     * Constructs a new {@link WebSocketException} with the specified detail message.
     *
     * @param ex the exception that caused this
     */
    public WebSocketException(Throwable ex) {
        this(WebSocketUtil.getErrorMessage(ex));
    }

    /**
     * Constructs a new {@link WebSocketException} with the specified detail message.
     *
     * @param message Error Message
     */
    public WebSocketException(String message) {
        this(WsGenericError, message);
    }

    /**
     * Constructs a new {@link WebSocketException} with the specified detail message and cause.
     *
     * @param errorCode   Cause
     * @param message Error message
     */
    public WebSocketException(WebSocketConstants.ErrorCode errorCode, String message) {
        super(errorCode.errorCode(), WebSocketUtil.createDetailRecord(message));
        this.message = message;
    }

    public String detailMessage() {
        return message;
    }
}
