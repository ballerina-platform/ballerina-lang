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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.api.BTypedesc;

import java.util.Map;

/**
 * <p>
 * Ballerina runtime value representation of a *type*.
 *
 * {@code typedesc} is used to describe type of a value in Ballerina.
 * For example {@code typedesc} of number 5 is {@code int}, where as {@code typedesc} of a record value is the
 * record type that used to create this particular value instance.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *  
 * @since 0.995.0
 */
public class TypedescValue implements RefValue, BTypedesc {

    final BType type;
    final BType describingType; // Type of the value describe by this typedesc.

    @Deprecated
    public TypedescValue(BType describingType) {
        this.type = BTypes.typeTypedesc;
        this.describingType = describingType;
    }

    /**
     * Returns the {@code BType} of the value describe by this type descriptor.
     * @return describing type
     */
    public BType getDescribingType() {
        return describingType;
    }

    @Override
    public String stringValue(Strand strand) {
        return "typedesc " + describingType.toString();
    }

    @Override
    public String toString() {
        return stringValue();
    }

    @Override
    public BType getType() {
        return type;
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
