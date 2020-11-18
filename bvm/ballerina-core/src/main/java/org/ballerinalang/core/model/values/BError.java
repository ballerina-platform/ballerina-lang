/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.ballerinalang.core.model.values;

import org.ballerinalang.core.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BError value.
 *
 * @since 0.983.0
 */
public class BError implements BRefType {

    BType type;
    private String message;
    private BRefType details;
    public List<BMap<String, BValue>> callStack;
    private BError cause;

    public BError(BType type, String message, BError cause, BRefType details) {
        this.type = type;
        this.message = message;
        this.cause = cause;
        this.details = details;
        callStack = new ArrayList<>();
    }

    @Override
    public String stringValue() {
        return message + " " + details.stringValue();
    }

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        // Error values are immutable and frozen, copy give same value.
        return this;
    }

    @Deprecated
    public String getReason() {
        return message;
    }

    public String getMessage() {
        return message;
    }

    public BRefType getDetails() {
        return details;
    }

    @Override
    public Object value() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFrozen() {
        return true;
    }

    public BError getCause() {
        return cause;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof  BError) {
            BError that = (BError) obj;

            boolean isCauseSame = false;
            if (this.cause != null) {
                isCauseSame = this.cause.equals(that.cause);
            } else if (that.cause == null) {
                isCauseSame = true;
            }
            return this.message.equals(that.message) && this.details.equals(that.details) && isCauseSame;
        }
        return false;
    }
}
