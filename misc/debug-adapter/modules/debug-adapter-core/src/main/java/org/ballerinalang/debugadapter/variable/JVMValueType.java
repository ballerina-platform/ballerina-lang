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

    INT("int"),
    LONG("long"),
    BOOLEAN("boolean"),
    DOUBLE("double"),
    J_INT("java.lang.Int"),
    J_LONG("java.lang.Long"),
    J_BOOLEAN("java.lang.Boolean"),
    J_DOUBLE("java.lang.Double"),
    J_STRING("java.lang.String"),
    J_OBJECT("java.lang.Object"),
    BMPSTRING("org.ballerinalang.jvm.values.BmpStringValue"),
    NONBMPSTRING("org.ballerinalang.jvm.values.NonBmpStringValue"),
    DECIMAL("org.ballerinalang.jvm.values.DecimalValue"), // todo - parent var name
    OBJECT_VALUE("org.ballerinalang.jvm.values.ObjectValue"),
    ARRAY_VALUE("org.ballerinalang.jvm.values.ArrayValue"),
    TUPLE_VALUE("org.ballerinalang.jvm.values.TupleValue"),
    MAP_VALUE("org.ballerinalang.jvm.values.MapValue"),
    ERROR_VALUE("org.ballerinalang.jvm.values.ErrorValue"),
    TYPEDESC_VALUE("org.ballerinalang.jvm.values.TypedescValue"),
    FUTURE_VALUE("org.ballerinalang.jvm.values.FutureValue"),
    HANDLE_VALUE("org.ballerinalang.jvm.values.HandleValue"),
    STREAM_VALUE("org.ballerinalang.jvm.values.StreamValue"),
    TABLE_VALUE("org.ballerinalang.jvm.values.TableValue"),
    XML_COMMENT("org.ballerinalang.jvm.values.XMLComment"),
    XML_ITEM("org.ballerinalang.jvm.values.XMLItem"),
    XML_PI("org.ballerinalang.jvm.values.XMLPi"),
    XML_SEQUENCE("org.ballerinalang.jvm.values.XMLSequence"),
    XML_TEXT("org.ballerinalang.jvm.values.XMLText"),
    XML_ATTRIB_MAP("org.ballerinalang.jvm.values.AttributeMapValueImpl"),
    FP_VALUE("org.ballerinalang.jvm.values.FPValue"),
    ANON_SERVICE("anonService"),
    BTYPE_OBJECT("BObjectType"),
    BTYPE_RECORD("BRecordType"),
    BTYPE_JSON("BJSONType");

    private final String value;

    JVMValueType(String value) {
        this.value = value;
    }

    public String getString() {
        return this.value;
    }
}
