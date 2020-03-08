/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.types;

/**
 * This class contains all the supported type names as string.
 *
 * @since 0.995.0
 */
public class TypeConstants {

    public static final String INT_TNAME = "int";
    public static final String BYTE_TNAME = "byte";
    public static final String FLOAT_TNAME = "float";
    public static final String DECIMAL_TNAME = "decimal";
    public static final String STRING_TNAME = "string";
    public static final String BOOLEAN_TNAME = "boolean";
    public static final String ARRAY_TNAME = "[]";
    public static final String MAP_TNAME = "map";
    public static final String FUTURE_TNAME = "future";
    public static final String XML_TNAME = "xml";
    public static final String JSON_TNAME = "json";
    public static final String ITERATOR_TNAME = "iterator";
    public static final String TABLE_TNAME = "table";
    public static final String STREAM_TNAME = "stream";
    public static final String ANY_TNAME = "any";
    public static final String ANYDATA_TNAME = "anydata";
    public static final String TYPEDESC_TNAME = "typedesc";
    public static final String NULL_TNAME = "()";
    public static final String XML_ATTRIBUTES_TNAME = "xml-attributes";
    public static final String CHANNEL = "channel";
    public static final String ERROR = "error";
    public static final String SERVICE = "service";
    public static final String HANDLE_TNAME = "handle";
    public static final String FINITE_TNAME = "finite";
    public static final String FUNCTION_TNAME = "function";

    // SubTypes
    public static final String SIGNED32 = "Signed32";
    public static final String SIGNED16 = "Signed16";
    public static final String SIGNED8 = "Signed8";
    public static final String UNSIGNED32 = "Unsigned32";
    public static final String UNSIGNED16 = "Unsigned16";
    public static final String UNSIGNED8 = "Unsigned8";

    // Special Types and Type fields.
    public static final String DETAIL_TYPE = "detail";
    public static final String DETAIL_MESSAGE = "message";
    public static final String DETAIL_CAUSE = "cause";

    // Return type of the next function in iterators
    public static final String ITERATOR_NEXT_RETURN_TYPE = "$$returnType$$";
}
