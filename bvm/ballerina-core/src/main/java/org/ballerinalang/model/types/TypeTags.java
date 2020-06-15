/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.types;

/**
 * This class contains tag values of each type in Ballerina.
 *
 * @since 0.87
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
    public static final int ARRAY_TAG = ENDPOINT_TAG + 1;
    public static final int UNION_TAG = ARRAY_TAG + 1;
    public static final int INTERSECTION_TAG = UNION_TAG + 1;
    public static final int PACKAGE_TAG = INTERSECTION_TAG + 1;
    public static final int NONE_TAG = PACKAGE_TAG + 1;
    public static final int VOID_TAG = NONE_TAG + 1;
    public static final int XMLNS_TAG = VOID_TAG + 1;
    public static final int ANNOTATION_TAG = XMLNS_TAG + 1;
    public static final int SEMANTIC_ERROR = ANNOTATION_TAG + 1;
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

    public static final int SERVICE_TAG = OBJECT_TYPE_TAG;
}
