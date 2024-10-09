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

package io.ballerina.runtime.api.types.semtype;

/**
 * Represent bit field that indicate which basic type a semType belongs to.
 *
 * @since 2201.10.0
 */
public final class BasicTypeCode {

    public static final int CODE_NIL = 0x00;
    public static final int CODE_BOOLEAN = 0x01;
    public static final int CODE_INT = 0x02;
    public static final int CODE_FLOAT = 0x03;
    public static final int CODE_DECIMAL = 0x04;
    public static final int CODE_STRING = 0x05;
    public static final int CODE_ERROR = 0x06;
    public static final int CODE_TYPEDESC = 0x07;
    public static final int CODE_HANDLE = 0x08;
    public static final int CODE_FUNCTION = 0x09;
    public static final int CODE_REGEXP = 0x0A;
    public static final int CODE_FUTURE = 0x0B;
    public static final int CODE_STREAM = 0x0C;
    public static final int CODE_LIST = 0x0D;
    public static final int CODE_MAPPING = 0x0E;
    public static final int CODE_TABLE = 0x0F;
    public static final int CODE_XML = 0x10;
    public static final int CODE_OBJECT = 0x11;
    public static final int CODE_CELL = 0x12;
    public static final int CODE_UNDEF = 0x13;

    // Inherently immutable
    public static final BasicTypeCode BT_NIL = get(CODE_NIL);
    public static final BasicTypeCode BT_BOOLEAN = get(CODE_BOOLEAN);
    public static final BasicTypeCode BT_INT = get(CODE_INT);
    public static final BasicTypeCode BT_FLOAT = get(CODE_FLOAT);
    public static final BasicTypeCode BT_DECIMAL = get(CODE_DECIMAL);
    public static final BasicTypeCode BT_STRING = get(CODE_STRING);
    public static final BasicTypeCode BT_ERROR = get(CODE_ERROR);
    public static final BasicTypeCode BT_TYPEDESC = get(CODE_TYPEDESC);
    public static final BasicTypeCode BT_HANDLE = get(CODE_HANDLE);
    public static final BasicTypeCode BT_FUNCTION = get(CODE_FUNCTION);
    public static final BasicTypeCode BT_REGEXP = get(CODE_REGEXP);

    // Inherently mutable
    public static final BasicTypeCode BT_FUTURE = get(CODE_FUTURE);
    public static final BasicTypeCode BT_STREAM = get(CODE_STREAM);

    // Selectively immutable
    public static final BasicTypeCode BT_LIST = get(CODE_LIST);
    public static final BasicTypeCode BT_MAPPING = get(CODE_MAPPING);
    public static final BasicTypeCode BT_TABLE = get(CODE_TABLE);
    public static final BasicTypeCode BT_XML = get(CODE_XML);
    public static final BasicTypeCode BT_OBJECT = get(CODE_OBJECT);

    // Non-val
    public static final BasicTypeCode BT_CELL = get(CODE_CELL);
    public static final BasicTypeCode BT_UNDEF = get(CODE_UNDEF);

    // Helper bit fields (does not represent basic type tag)
    static final int VT_COUNT = CODE_OBJECT + 1;
    public static final int BASIC_TYPE_MASK = (1 << (CODE_STRING + 1)) - 1;
    public static final int VT_MASK = (1 << VT_COUNT) - 1;

    static final int VT_COUNT_INHERENTLY_IMMUTABLE = CODE_FUTURE;
    public static final int VT_INHERENTLY_IMMUTABLE = (1 << VT_COUNT_INHERENTLY_IMMUTABLE) - 1;

    private final int code;

    private BasicTypeCode(int code) {
        this.code = code;
    }

    public static BasicTypeCode get(int code) {
        if (BasicTypeCodeCache.isCached(code)) {
            return BasicTypeCodeCache.cache[code];
        }
        return new BasicTypeCode(code);
    }

    public int code() {
        return code;
    }

    @Override
    public int hashCode() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BasicTypeCode other) {
            return code == other.code;
        }
        return false;
    }

    private static final class BasicTypeCodeCache {

        private static final BasicTypeCode[] cache;
        static {
            cache = new BasicTypeCode[CODE_UNDEF + 2];
            for (int i = CODE_NIL; i < CODE_UNDEF + 1; i++) {
                cache[i] = new BasicTypeCode(i);
            }
        }

        private static boolean isCached(int code) {
            return 0 < code && code < VT_COUNT;
        }

    }
}
