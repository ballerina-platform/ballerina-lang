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

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_BOOLEAN;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_B_TYPE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_CELL;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_DECIMAL;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_ERROR;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_FLOAT;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_FUNCTION;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_FUTURE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_HANDLE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_INT;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_LIST;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_MAPPING;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_NIL;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_OBJECT;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_STREAM;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_STRING;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_TABLE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_TYPEDESC;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_UNDEF;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_XML;

public final class SemTypeHelper {

    private SemTypeHelper() {
    }

    public static String stringRepr(SemType ty) {
        return "all[" + bitSetRepr(ty.all()) + "] some [" + bitSetRepr(ty.some()) + "]";
    }

    private static String bitSetRepr(int bits) {
        StringBuilder sb = new StringBuilder();
        appendBitSetRepr(sb, bits, CODE_NIL, "NIL");
        appendBitSetRepr(sb, bits, CODE_BOOLEAN, "BOOLEAN");
        appendBitSetRepr(sb, bits, CODE_INT, "INT");
        appendBitSetRepr(sb, bits, CODE_FLOAT, "FLOAT");
        appendBitSetRepr(sb, bits, CODE_DECIMAL, "DECIMAL");
        appendBitSetRepr(sb, bits, CODE_STRING, "STRING");
        appendBitSetRepr(sb, bits, CODE_ERROR, "ERROR");
        appendBitSetRepr(sb, bits, CODE_TYPEDESC, "TYPE_DESC");
        appendBitSetRepr(sb, bits, CODE_HANDLE, "HANDLE");
        appendBitSetRepr(sb, bits, CODE_FUNCTION, "FUNCTION");
        appendBitSetRepr(sb, bits, CODE_FUTURE, "FUTURE");
        appendBitSetRepr(sb, bits, CODE_STREAM, "STREAM");
        appendBitSetRepr(sb, bits, CODE_LIST, "LIST");
        appendBitSetRepr(sb, bits, CODE_MAPPING, "MAPPING");
        appendBitSetRepr(sb, bits, CODE_TABLE, "TABLE");
        appendBitSetRepr(sb, bits, CODE_XML, "XML");
        appendBitSetRepr(sb, bits, CODE_OBJECT, "OBJECT");
        appendBitSetRepr(sb, bits, CODE_CELL, "CELL");
        appendBitSetRepr(sb, bits, CODE_UNDEF, "UNDEF");
        appendBitSetRepr(sb, bits, CODE_B_TYPE, "B_TYPE");
        return sb.toString();
    }

    private static void appendBitSetRepr(StringBuilder sb, int bits, int index, String name) {
        int mask = 1 << index;
        if ((bits & mask) != 0) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(name).append(" ");
        }
    }
}
