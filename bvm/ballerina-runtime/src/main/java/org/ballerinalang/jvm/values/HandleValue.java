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
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;

import java.util.List;
import java.util.Map;

/**
 * Represent an opaque handle value in jBallerina.
 *
 * @since 1.0
 */
public class HandleValue implements RefValue {

    private Object value;

    public HandleValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String stringValue() {
        return value.toString();
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : null;
    }

    @Override
    public BType getType() {
        return BTypes.typeHandle;
    }

    @Override
    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {

    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {

        return this;
    }
}
