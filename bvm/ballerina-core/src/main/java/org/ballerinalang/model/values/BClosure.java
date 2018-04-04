/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BType;

/**
 * The {@code BClosure} holds closure of any BValue in Ballerina.
 *
 * @since 0.97
 */
public class BClosure implements BValue {

    private BValue value;

    public BClosure(BValue value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return value.stringValue();
    }

    @Override
    public BType getType() {
        return value.getType();
    }

    public BValue value() {
        return value;
    }

    @Override
    public BValue copy() {
        return new BClosure(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BClosure)) {
            return false;
        }

        BClosure other = (BClosure) obj;
        return this.value.getType() == other.value.getType() && this.value.equals(other.value);
    }
}
