/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.variable;

/**
 * Ballerina JVM variable types.
 */
public enum JVMValueType {

    LONG("long"),
    BOOLEAN("boolean"),
    DOUBLE("double"),
    DECIMAL("org.ballerinalang.jvm.values.DecimalValue"), // todo - parent var name
    STRING("java.lang.String"),
    OBJECT("java.lang.Object"),
    OBJECT_TYPE("org.ballerinalang.jvm.types.BObjectType"),
    OBJECT_VALUE("org.ballerinalang.jvm.values.ObjectValue"),
    ARRAY_VALUE("org.ballerinalang.jvm.values.ArrayValue"),
    TUPLE_VALUE("org.ballerinalang.jvm.values.TupleValue"),
    MAP_VALUE("org.ballerinalang.jvm.values.MapValue"),
    ERROR_VALUE("org.ballerinalang.jvm.values.ErrorValue"),
    XML_ITEM("org.ballerinalang.jvm.values.XMLItem");

    private final String value;

    JVMValueType(String value) {
        this.value = value;
    }

    public String getString() {
        return this.value;
    }
}
