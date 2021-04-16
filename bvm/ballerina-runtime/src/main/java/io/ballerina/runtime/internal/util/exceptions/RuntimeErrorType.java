/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.internal.util.exceptions;

/**
 * Error types, Error keys and Error codes to represent the runtime errors.
 *
 * @since 2.0.0
 */

public enum RuntimeErrorType {
    TYPE_CAST_ERROR("TypeCastError", "incompatible.types.cannot.cast", "RT-0001");

    private final String errorName;
    private final String errorMsgKey;
    private final String errorCode;

    RuntimeErrorType(String errorName, String errorMessageKey, String errorCode) {
        this.errorName = errorName;
        this.errorMsgKey = errorMessageKey;
        this.errorCode = errorCode;
    }

    public String getErrorName() {
        return errorName;
    }
    public String getErrorMsgKey() {
        return errorMsgKey;
    }
    public String getErrorCode() {
        return errorCode;
    }
}
