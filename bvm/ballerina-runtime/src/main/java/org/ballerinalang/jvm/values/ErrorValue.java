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
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.BErrorType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.freeze.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent an error in ballerina.
 *
 * @since 0.995.0
 */
public class ErrorValue extends RuntimeException implements RefValue {

    private static final long serialVersionUID = 1L;
    private final BErrorType type;
    private final String reason;
    private final Object details;

    public ErrorValue(String reason, Object details) {
        super(reason);
        this.type = BTypes.typeError;
        this.reason = reason;
        this.details = details;
    }

    @Override
    public BErrorType getType() {
        return type;
    }

    @Override
    public void stamp(BType type) {

    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        // Error values are immutable and frozen, copy give same value.
        return this;
    }

    @Override
    public void attemptFreeze(Status freezeStatus) {
        // do nothing, since error types are always frozen
    }

    @Override
    public String toString() {
        return reason + " " + details.toString();
    }

    public String getReason() {
        return reason;
    }

    public Object getDetails() {
        if (details instanceof RefValue) {
            return ((RefValue) details).copy(new HashMap<>());
        }
        return details;
    }

    @Override
    public void printStackTrace() {
        ErrorHandlerUtils.printError("error: " + BallerinaErrors.getPrintableStackTrace(this));
    }
}
