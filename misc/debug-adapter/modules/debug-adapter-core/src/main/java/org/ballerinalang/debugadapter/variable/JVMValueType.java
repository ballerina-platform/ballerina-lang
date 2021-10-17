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

import static org.ballerinalang.debugadapter.variable.VariableUtils.INTERNAL_VALUE_PREFIX;

/**
 * Ballerina JVM variable types.
 */
public enum JVMValueType {

    INT("int"),
    BYTE("byte"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    J_INT("java.lang.Int"),
    J_LONG("java.lang.Long"),
    J_DOUBLE("java.lang.Double"),
    J_STRING("java.lang.String"),
    J_OBJECT("java.lang.Object"),
    J_BOOLEAN("java.lang.Boolean"),
    BTYPE_JSON("BJsonType"),
    BTYPE_OBJECT("BObjectType"),
    BTYPE_RECORD("BRecordType"),
    BTYPE_SERVICE("BServiceType"),
    BMP_STRING(INTERNAL_VALUE_PREFIX + "BmpStringValue"),
    NON_BMP_STRING(INTERNAL_VALUE_PREFIX + "NonBmpStringValue"),
    DECIMAL(INTERNAL_VALUE_PREFIX + "DecimalValue"),
    OBJECT_VALUE(INTERNAL_VALUE_PREFIX + "ObjectValue"),
    ARRAY_VALUE(INTERNAL_VALUE_PREFIX + "ArrayValue"),
    TUPLE_VALUE(INTERNAL_VALUE_PREFIX + "TupleValue"),
    MAP_VALUE(INTERNAL_VALUE_PREFIX + "MapValue"),
    ERROR_VALUE(INTERNAL_VALUE_PREFIX + "ErrorValue"),
    TYPEDESC_VALUE(INTERNAL_VALUE_PREFIX + "TypedescValue"),
    FUTURE_VALUE(INTERNAL_VALUE_PREFIX + "FutureValue"),
    HANDLE_VALUE(INTERNAL_VALUE_PREFIX + "HandleValue"),
    STREAM_VALUE(INTERNAL_VALUE_PREFIX + "StreamValue"),
    TABLE_VALUE(INTERNAL_VALUE_PREFIX + "TableValue"),
    XML_COMMENT(INTERNAL_VALUE_PREFIX + "XmlComment"),
    FP_VALUE(INTERNAL_VALUE_PREFIX + "FPValue"),
    XML_PI(INTERNAL_VALUE_PREFIX + "XmlPi"),
    XML_TEXT(INTERNAL_VALUE_PREFIX + "XmlText"),
    XML_ITEM(INTERNAL_VALUE_PREFIX + "XmlItem"),
    XML_SEQUENCE(INTERNAL_VALUE_PREFIX + "XmlSequence"),
    XML_ATTRIB_MAP(INTERNAL_VALUE_PREFIX + "AttributeMapValueImpl");

    private final String value;

    JVMValueType(String value) {
        this.value = value;
    }

    public String getString() {
        return this.value;
    }
}
