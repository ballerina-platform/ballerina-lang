/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.types;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent bit field that indicate which uniform type a semType belongs to.
 * Regular types are divided longo mutable part and immutable part and these parts are called an uniform type.
 * 5th bit indicate mutability; 0 immutable, 1 mutable.
 *
 * @since 2.0.0
 */
public class UniformTypeCode {
    // Inherently immutable
    public static final UniformTypeCode UT_NIL = from(0x00);
    public static final UniformTypeCode UT_BOOLEAN = from(0x01);

    // Selectively immutable; immutable half
    public static final UniformTypeCode UT_LIST_RO = from(0x02);
    public static final UniformTypeCode UT_MAPPING_RO = from(0x03);
    public static final UniformTypeCode UT_TABLE_RO = from(0x04);
    public static final UniformTypeCode UT_XML_RO = from(0x05);
    public static final UniformTypeCode UT_OBJECT_RO = from(0x06);

    // Rest of inherently immutable
    public static final UniformTypeCode UT_INT = from(0x07);
    public static final UniformTypeCode UT_FLOAT = from(0x08);
    public static final UniformTypeCode UT_DECIMAL = from(0x09);
    public static final UniformTypeCode UT_STRING = from(0x0A);
    public static final UniformTypeCode UT_ERROR = from(0x0B);
    public static final UniformTypeCode UT_FUNCTION = from(0x0C);
    public static final UniformTypeCode UT_TYPEDESC = from(0x0D);
    public static final UniformTypeCode UT_HANDLE = from(0x0E);

    // Inherently mutable
    public static final UniformTypeCode UT_FUTURE = from(0x10);
    public static final UniformTypeCode UT_STREAM = from(0x11);

    // Selectively immutable; mutable half
    public static final UniformTypeCode UT_LIST_RW = from(0x12);
    public static final UniformTypeCode UT_MAPPING_RW = from(0x13);
    public static final UniformTypeCode UT_TABLE_RW = from(0x14);
    public static final UniformTypeCode UT_XML_RW = from(0x15);
    public static final UniformTypeCode UT_OBJECT_RW = from(0x16);

    // Helper bit fields (does not represent uniform type tag)
    static final int UT_COUNT = UT_OBJECT_RW.code + 1;
    static final int UT_MASK = (1 << UT_COUNT) - 1;

    static final int UT_COUNT_RO = 0x10;
    static final int UT_READONLY = (1 << UT_COUNT_RO) - 1;

    static final int UT_RW_MASK = UT_MASK ^ ~UT_READONLY;
    public final int code;

    private UniformTypeCode(int code) {
        this.code = code;
    }

    public static UniformTypeCode from(int code) {
        // todo: Add validation
        return new UniformTypeCode(code);
    }

    // Only used for .toString() method to aid debugging.
    private static Map<Integer, String> fieldNames = new HashMap<>();
    static {
        for (Field field : UniformTypeCode.class.getDeclaredFields()) {
            if (field.getType() == UniformTypeCode.class) {
                try {
                    UniformTypeCode o = (UniformTypeCode) field.get(null);
                    fieldNames.put(o.code, field.getName());
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException();
                }
            }
        }
    }

    @Override
    public String toString() {
        return fieldNames.get(this.code);
    }
}
