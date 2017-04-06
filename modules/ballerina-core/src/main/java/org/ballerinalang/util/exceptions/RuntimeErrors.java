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

package org.ballerinalang.util.exceptions;

/**
 * Error codes and Error keys to represent the runtime errors.
 */
public enum RuntimeErrors {

    CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE("casting.any.to.wrong.value.type", "RUNTIME_0001"),
    CASTING_ANY_TYPE_WITHOUT_INIT("casting.any.without.init", "RUNTIME_0002"),
    INDEX_NUMBER_TOO_LARGE("index.number.too.large", "RUNTIME_0003"),
    ARRAY_INDEX_OUT_OF_RANGE("array.index.out.of.range", "RUNTIME_0004");


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
