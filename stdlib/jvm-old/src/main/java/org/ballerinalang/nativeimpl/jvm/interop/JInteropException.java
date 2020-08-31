/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.jvm.interop;

/**
 * This error indicates Ballerina Java interoperability related error.
 *
 * @since 1.0.0
 */
class JInteropException extends RuntimeException {

    static final String CLASS_NOT_FOUND_REASON = "CLASS_NOT_FOUND";
    static final String METHOD_NOT_FOUND_REASON = "METHOD_NOT_FOUND";
    static final String CONSTRUCTOR_NOT_FOUND_REASON = "CONSTRUCTOR_NOT_FOUND";
    static final String FIELD_NOT_FOUND_REASON = "FIELD_NOT_FOUND";
    static final String OVERLOADED_METHODS_REASON = "OVERLOADED_METHODS";
    static final String UNSUPPORTED_PRIMITIVE_TYPE_REASON = "UNSUPPORTED_PRIMITIVE_TYPE";
    static final String METHOD_SIGNATURE_NOT_MATCH_REASON = "METHOD_SIGNATURE_DOES_NOT_MATCH";
    static final String CLASS_LOADER_INIT_FAILED_REASON = "CLASS_LOADER_INIT_FAILED";

    private String reason;

    JInteropException(String reason, String message) {
        super(message);
        this.reason = reason;
    }

    JInteropException(String reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    String getReason() {
        return this.reason;
    }
}
