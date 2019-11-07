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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.api.BHandle;

import java.util.Map;

/**
 * <p>
 * Represent an opaque handle value in jBallerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *  
 * @since 1.0
 */
public class HandleValue implements BHandle, RefValue {

    private Object value;

    @Deprecated
    public HandleValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the internal value of the handle.
     * @return {@code Object} value
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String stringValue(Strand strand) {
        return String.valueOf(value);
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
    public Object copy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {

        return this;
    }

    public static HandleValue valueOfJ(Object value) {
        return new HandleValue(value);
    }
}
