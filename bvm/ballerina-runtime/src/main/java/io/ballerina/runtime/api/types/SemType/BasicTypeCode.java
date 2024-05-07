/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.api.types.SemType;

import java.util.HashMap;
import java.util.Map;

public final class BasicTypeCode {

    private final static Map<Integer, BasicTypeCode> cache = new HashMap<>();

    static final int CODE_NIL = 0x00;
    static final int CODE_BOOLEAN = 0x01;
    static final int CODE_INT = 0x02;
    static final int CODE_FLOAT = 0x03;
    static final int CODE_DECIMAL = 0x04;
    static final int CODE_STRING = 0x05;
    static final int CODE_ERROR = 0x06;
    static final int CODE_TYPEDESC = 0x07;
    static final int CODE_HANDLE = 0x08;
    static final int CODE_FUNCTION = 0x09;
    static final int CODE_FUTURE = 0x0A;
    static final int CODE_STREAM = 0x0B;
    static final int CODE_LIST = 0x0C;
    static final int CODE_MAPPING = 0x0D;
    static final int CODE_TABLE = 0x0E;
    static final int CODE_XML = 0x0F;
    static final int CODE_OBJECT = 0x10;
    static final int CODE_CELL = 0x11;
    static final int CODE_UNDEF = 0x12;
    static final int CODE_B_TYPE = 0x13;

    // Inherently immutable
    public static final BasicTypeCode BT_NIL = from(CODE_NIL);
    public static final BasicTypeCode BT_BOOLEAN = from(CODE_BOOLEAN);
    public static final BasicTypeCode BT_INT = from(CODE_INT);
    public static final BasicTypeCode BT_FLOAT = from(CODE_FLOAT);
    public static final BasicTypeCode BT_DECIMAL = from(CODE_DECIMAL);
    public static final BasicTypeCode BT_STRING = from(CODE_STRING);
    public static final BasicTypeCode BT_ERROR = from(CODE_ERROR);
    public static final BasicTypeCode BT_TYPEDESC = from(CODE_TYPEDESC);
    public static final BasicTypeCode BT_HANDLE = from(CODE_HANDLE);
    public static final BasicTypeCode BT_FUNCTION = from(CODE_FUNCTION);

    // Inherently mutable
    public static final BasicTypeCode BT_FUTURE = from(CODE_FUTURE);
    public static final BasicTypeCode BT_STREAM = from(CODE_STREAM);

    // Selectively immutable
    public static final BasicTypeCode BT_LIST = from(CODE_LIST);
    public static final BasicTypeCode BT_MAPPING = from(CODE_MAPPING);
    public static final BasicTypeCode BT_TABLE = from(CODE_TABLE);
    public static final BasicTypeCode BT_XML = from(CODE_XML);
    public static final BasicTypeCode BT_OBJECT = from(CODE_OBJECT);

    // Non-val
    public static final BasicTypeCode BT_CELL = from(CODE_CELL);
    public static final BasicTypeCode BT_UNDEF = from(CODE_UNDEF);
    public static final BasicTypeCode BT_B_TYPE = from(CODE_B_TYPE);

    // Helper bit fields (does not represent basic type tag)
    static final int VT_COUNT = BT_OBJECT.code + 1;
    static final int VT_MASK = (1 << VT_COUNT) - 1;

    static final int VT_COUNT_INHERENTLY_IMMUTABLE = 0x0A;
    public static final int VT_INHERENTLY_IMMUTABLE = (1 << VT_COUNT_INHERENTLY_IMMUTABLE) - 1;

    private int code;

    private BasicTypeCode(int code) {
        this.code = code;
    }

    public static BasicTypeCode from(int code) {
        return cache.computeIfAbsent(code, BasicTypeCode::new);
    }

    public int code() {
        return code;
    }
}
