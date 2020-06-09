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
 * This class contains tag values of each type in Ballerina.
 *
 * @since 0.995.0
 */
public class TypeTags {
    public static final int INT_TAG = 1;
    public static final int BYTE_TAG = INT_TAG + 1;
    public static final int FLOAT_TAG = BYTE_TAG + 1;
    public static final int DECIMAL_TAG = FLOAT_TAG + 1;
    public static final int STRING_TAG = DECIMAL_TAG + 1;
    public static final int BOOLEAN_TAG = STRING_TAG + 1;
    public static final int JSON_TAG = BOOLEAN_TAG + 1;
    public static final int XML_TAG = JSON_TAG + 1;
    public static final int TABLE_TAG = XML_TAG + 1;
    public static final int NULL_TAG = TABLE_TAG + 1;
    public static final int ANYDATA_TAG = NULL_TAG + 1;
    public static final int RECORD_TYPE_TAG = ANYDATA_TAG + 1;
    public static final int TYPEDESC_TAG = RECORD_TYPE_TAG + 1;
    public static final int STREAM_TAG = TYPEDESC_TAG + 1;
    public static final int MAP_TAG = STREAM_TAG + 1;
    public static final int INVOKABLE_TAG = MAP_TAG + 1;
    public static final int ANY_TAG = INVOKABLE_TAG + 1;
    public static final int ENDPOINT_TAG = ANY_TAG + 1;
    public static final int SERVICE_TAG = ENDPOINT_TAG + 1;
    public static final int ARRAY_TAG = SERVICE_TAG + 1;
    public static final int UNION_TAG = ARRAY_TAG + 1;
    public static final int INTERSECTION_TAG = UNION_TAG + 1;
    public static final int PACKAGE_TAG = INTERSECTION_TAG + 1;
    public static final int NONE_TAG = PACKAGE_TAG + 1;
    public static final int VOID_TAG = NONE_TAG + 1;
    public static final int XMLNS_TAG = VOID_TAG + 1;
    public static final int ANNOTATION_TAG = XMLNS_TAG + 1;
    public static final int XML_ATTRIBUTES_TAG = ANNOTATION_TAG + 1;
    public static final int SEMANTIC_ERROR = XML_ATTRIBUTES_TAG + 1;
    public static final int ERROR_TAG = SEMANTIC_ERROR + 1;
    public static final int ITERATOR_TAG = ERROR_TAG + 1;
    public static final int TUPLE_TAG = ITERATOR_TAG + 1;
    public static final int FUTURE_TAG = TUPLE_TAG + 1;
    public static final int FINITE_TYPE_TAG = FUTURE_TAG + 1;
    public static final int OBJECT_TYPE_TAG = FINITE_TYPE_TAG + 1;
    public static final int BYTE_ARRAY_TAG = OBJECT_TYPE_TAG + 1;
    public static final int FUNCTION_POINTER_TAG = BYTE_ARRAY_TAG + 1;
    public static final int CHANNEL_TAG = FUNCTION_POINTER_TAG + 1;
    public static final int HANDLE_TAG = FUNCTION_POINTER_TAG + 1;
    public static final int READONLY_TAG = HANDLE_TAG + 1;

    // Subtypes
    public static final int SIGNED32_INT_TAG = READONLY_TAG + 1;
    public static final int SIGNED16_INT_TAG = SIGNED32_INT_TAG + 1;
    public static final int SIGNED8_INT_TAG = SIGNED16_INT_TAG + 1;
    public static final int UNSIGNED32_INT_TAG = SIGNED8_INT_TAG + 1;
    public static final int UNSIGNED16_INT_TAG = UNSIGNED32_INT_TAG + 1;
    public static final int UNSIGNED8_INT_TAG = UNSIGNED16_INT_TAG + 1;
    public static final int CHAR_STRING_TAG = UNSIGNED8_INT_TAG + 1;
    public static final int XML_ELEMENT_TAG = CHAR_STRING_TAG + 1;
    public static final int XML_PI_TAG = XML_ELEMENT_TAG + 1;
    public static final int XML_COMMENT_TAG = XML_PI_TAG + 1;
    public static final int XML_TEXT_TAG = XML_COMMENT_TAG + 1;
    public static final int NEVER_TAG = XML_TEXT_TAG + 1;

    public static boolean isIntegerTypeTag(int tag) {

        // TODO : Fix byte type. Ideally, byte belongs to here. But we have modeled it differently.
        switch (tag) {
            case INT_TAG:
            case SIGNED32_INT_TAG:
            case SIGNED16_INT_TAG:
            case SIGNED8_INT_TAG:
            case UNSIGNED32_INT_TAG:
            case UNSIGNED16_INT_TAG:
            case UNSIGNED8_INT_TAG:
                return true;
        }
        return false;
    }

    public static boolean isXMLTypeTag(int tag) {

        switch (tag) {
            case XML_TAG:
            case XML_ELEMENT_TAG:
            case XML_COMMENT_TAG:
            case XML_PI_TAG:
            case XML_TEXT_TAG:
                return true;
        }
        return false;
    }

    public static boolean isStringTypeTag(int tag) {

        switch (tag) {
            case STRING_TAG:
            case CHAR_STRING_TAG:
                return true;
        }
        return false;
    }
}
