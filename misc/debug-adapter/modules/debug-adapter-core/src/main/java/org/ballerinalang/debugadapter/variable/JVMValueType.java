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

    BOOLEAN("boolean"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    BYTE("byte"),
    J_INT("java.lang.Int"),
    J_LONG("java.lang.Long"),
    J_BOOLEAN("java.lang.Boolean"),
    J_DOUBLE("java.lang.Double"),
    J_STRING("java.lang.String"),
    J_OBJECT("java.lang.Object"),
    BMPSTRING("io.ballerina.runtime.internal.values.BmpStringValue"),
    NONBMPSTRING("io.ballerina.runtime.internal.values.NonBmpStringValue"),
    DECIMAL("io.ballerina.runtime.internal.values.DecimalValue"),
    OBJECT_VALUE("io.ballerina.runtime.internal.values.ObjectValue"),
    ARRAY_VALUE("io.ballerina.runtime.internal.values.ArrayValue"),
    TUPLE_VALUE("io.ballerina.runtime.internal.values.TupleValue"),
    MAP_VALUE("io.ballerina.runtime.internal.values.MapValue"),
    ERROR_VALUE("io.ballerina.runtime.internal.values.ErrorValue"),
    TYPEDESC_VALUE("io.ballerina.runtime.internal.values.TypedescValue"),
    FUTURE_VALUE("io.ballerina.runtime.internal.values.FutureValue"),
    HANDLE_VALUE("io.ballerina.runtime.internal.values.HandleValue"),
    STREAM_VALUE("io.ballerina.runtime.internal.values.StreamValue"),
    TABLE_VALUE("io.ballerina.runtime.internal.values.TableValue"),
    XML_COMMENT("io.ballerina.runtime.internal.values.XmlComment"),
    XML_ITEM("io.ballerina.runtime.internal.values.XmlItem"),
    XML_PI("io.ballerina.runtime.internal.values.XmlPi"),
    XML_SEQUENCE("io.ballerina.runtime.internal.values.XmlSequence"),
    XML_TEXT("io.ballerina.runtime.internal.values.XmlText"),
    XML_ATTRIB_MAP("io.ballerina.runtime.internal.values.AttributeMapValueImpl"),
    STRAND_WAIT_RESULT("io.ballerina.runtime.internal.scheduling.Strand$WaitResult"),
    FP_VALUE("io.ballerina.runtime.internal.values.FPValue"),
    BTYPE_OBJECT("BObjectType"),
    BTYPE_RECORD("BRecordType"),
    BTYPE_JSON("BJsonType"),
    BTYPE_SERVICE("BServiceType");

    private final String value;

    JVMValueType(String value) {
        this.value = value;
    }

    public String getString() {
        return this.value;
    }
}
