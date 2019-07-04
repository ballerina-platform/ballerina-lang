/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;

import java.util.List;
import java.util.Map;

/**
 * Ballerina runtime value representation of a *type*.
 *
 * {@code typedesc} is used to describe type of a value in Ballerina.
 * For example {@code typedesc} of number 5 is {@code int}, where as {@code typedesc} of a record value is the
 * record type that used to create this particular value instance.
 *
 * @since 0.995.0
 */
public class TypedescValue implements RefValue {

    final BType type;
    final BType describingType; // Type of the value describe by this typedesc.

    public TypedescValue(BType describingType) {
        this.type = BTypes.typeTypedesc;
        this.describingType = describingType;
    }

    public BType getDescribingType() {
        return describingType;
    }

    @Override
    public String stringValue() {
        return "typedesc " + describingType.toString();
    }

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {

    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public boolean isFrozen() {
        return true;
    }

    @Override
    public Object freeze() {
        return this;
    }
}
