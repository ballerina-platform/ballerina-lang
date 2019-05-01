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
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TypedescValue;

/**
 * Runtime impl to create typedesc values out of Ballerina values.
 *
 * @since 0.995.0
 */
public class TypedescProvider {
    public static TypedescValue typeof(Object value) {
        if (value instanceof RefValue) {
            RefValue v = (RefValue) value;
            org.ballerinalang.jvm.types.BType type = v.getType();
            return new TypedescValue(type);
        } else if (value instanceof Double) {
            return new TypedescValue(BTypes.typeFloat);
        } else if (value instanceof Long) {
            return new TypedescValue(BTypes.typeInt);
        } else if (value instanceof String) {
            return new TypedescValue(BTypes.typeString);
        } else if (value instanceof Byte) {
            return new TypedescValue(BTypes.typeByte);
        } else if (value instanceof Boolean) {
            return new TypedescValue(BTypes.typeBoolean);
        }
        return null;
    }
}
