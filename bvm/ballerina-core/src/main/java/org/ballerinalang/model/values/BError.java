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
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BType;

/**
 * BError value.
 *
 * @since 0.983.0
 */
public class BError implements BRefType {

    BType type;
    public String reason;
    public BRefType details;
    public BMap<String, BValue> stackElement;

    public BError(BType type, String reason, BRefType details) {
        this.type = type;
        this.reason = reason;
        this.details = details;
    }

    @Override
    public String stringValue() {
        return reason + " " + details.stringValue();
    }

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public void seal(BType type) {

    }

    @Override
    public BValue copy() {
        // Error values are immutable and frozen, copy give same value.
        return this;
    }

    public String getReason() {
        return reason;
    }

    public BRefType getDetails() {
        // TODO: Make details frozen.
        return (BRefType) details.copy();
    }

    @Override
    public Object value() {
        return null;
    }
}
