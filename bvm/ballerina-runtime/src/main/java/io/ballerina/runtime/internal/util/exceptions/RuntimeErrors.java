/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Error codes and Error keys to represent the runtime errors.
 */
public enum RuntimeErrors {

    INCOMPATIBLE_TYPE("incompatible.types", "RUNTIME_0005"),
    NOT_ENOUGH_FORMAT_ARGUMENTS("not.enough.format.arguments", "RUNTIME_0021"),
    INVALID_FORMAT_SPECIFIER("invalid.format.specifier", "RUNTIME_0022"),
    ILLEGAL_FORMAT_CONVERSION("illegal.format.conversion", "RUNTIME_0028"),
    INCOMPATIBLE_CONVERT_OPERATION("incompatible.convert.operation", "RUNTIME_0039"),
    INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION("incompatible.simple.type.convert.operation", "RUNTIME_0040"),
    XML_FUNC_TYPE_ERROR("unexpected.xml.type", "RUNTIME_0042"),
    JAVA_NULL_REFERENCE("java.null.reference", "RUNTIME_0043"),
    INVALID_READONLY_VALUE_UPDATE("invalid.update.on.readonly.value", "RUNTIME_0052");

    private String errorMsgKey;
    private String errorCode;

    RuntimeErrors(String errorMessageKey, String errorCode) {
        this.errorMsgKey = errorMessageKey;
        this.errorCode = errorCode;
    }

    public String getErrorMsgKey() {
        return errorMsgKey;
    }

    public void setErrorMsgKey(String errorMsgKey) {
        this.errorMsgKey = errorMsgKey;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
