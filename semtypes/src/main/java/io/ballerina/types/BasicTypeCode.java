/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent bit field that indicate which basic type a semType belongs to.
 *
 * @since 2201.10.0
 */
public class BasicTypeCode {
    // Inherently immutable
    public static final BasicTypeCode BT_NIL = from(0x00);
    public static final BasicTypeCode BT_BOOLEAN = from(0x01);
    public static final BasicTypeCode BT_INT = from(0x02);
    public static final BasicTypeCode BT_FLOAT = from(0x03);
    public static final BasicTypeCode BT_DECIMAL = from(0x04);
    public static final BasicTypeCode BT_STRING = from(0x05);
    public static final BasicTypeCode BT_ERROR = from(0x06);
    public static final BasicTypeCode BT_TYPEDESC = from(0x07);
    public static final BasicTypeCode BT_HANDLE = from(0x08);
    public static final BasicTypeCode BT_FUNCTION = from(0x09);

    // Inherently mutable
    public static final BasicTypeCode BT_FUTURE = from(0x0A);
    public static final BasicTypeCode BT_STREAM = from(0x0B);

    // Selectively immutable
    public static final BasicTypeCode BT_LIST = from(0x0C);
    public static final BasicTypeCode BT_MAPPING = from(0x0D);
    public static final BasicTypeCode BT_TABLE = from(0x0E);
    public static final BasicTypeCode BT_XML = from(0x0F);
    public static final BasicTypeCode BT_OBJECT = from(0x10);

    // Non-val
    public static final BasicTypeCode BT_CELL = from(0x11);
    public static final BasicTypeCode BT_UNDEF = from(0x12);

    // Helper bit fields (does not represent basic type tag)
    static final int VT_COUNT = BT_OBJECT.code + 1;
    static final int VT_MASK = (1 << VT_COUNT) - 1;

    static final int VT_COUNT_INHERENTLY_IMMUTABLE = 0x0A;
    public static final int VT_INHERENTLY_IMMUTABLE = (1 << VT_COUNT_INHERENTLY_IMMUTABLE) - 1;

    public final int code;

    // There is an integer for each basic type.
    private BasicTypeCode(int code) {
        this.code = code;
    }

    public static BasicTypeCode from(int code) {
        // todo: Add validation
        return new BasicTypeCode(code);
    }

    // Only used for .toString() method to aid debugging.
    private static Map<Integer, String> fieldNames = new HashMap<>();
    static {
        for (Field field : BasicTypeCode.class.getDeclaredFields()) {
            if (field.getType() == BasicTypeCode.class) {
                try {
                    BasicTypeCode o = (BasicTypeCode) field.get(null);
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
