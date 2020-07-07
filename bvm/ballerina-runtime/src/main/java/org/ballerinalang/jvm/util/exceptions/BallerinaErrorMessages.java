/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.jvm.util.exceptions;


/**
 * This is a temporary class for reasons for Ballerina errors from the VM either returned or causing panic.
 *
 * @since 0.990.0
 */
public class BallerinaErrorMessages {
    public static final String TYPE_CAST_ERROR = "Type cast error";
    public static final String NUMBER_CONVERSION_ERROR = "Number conversion error";
    public static final String JSON_OPERATION_ERROR = "JSON operation error";

    public static final String DIVISION_BY_ZERO_ERROR = "Division by zero";
    public static final String NUMBER_OVERFLOW = "Number overflow";
    public static final String ARITHMETIC_OPERATION_ERROR = "Arithmetic operation error";
    public static final String JAVA_NULL_REFERENCE_ERROR = "Java null reference error";
    public static final String JAVA_CLASS_NOT_FOUND_ERROR = "Java class not found error";

    // TODO: 8/28/19 Errors we should be able to remove once all migration is done.
    public static final String BALLERINA_PREFIXED_CONVERSION_ERROR = "Conversion error";
    public static final String ITERATOR_MUTABILITY_ERROR = "Iterator mutability error";

    public static final String NUMBER_PARSING_ERROR_IDENTIFIER = "Number parsing error";
    public static final String BOOLEAN_PARSING_ERROR_IDENTIFIER = "Boolean parsing error";
    public static final String INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER = "Index out of range";
    public static final String INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER = "Inherent type violation";
    public static final String OPERATION_NOT_SUPPORTED_IDENTIFIER = "Operation not supported";
    public static final String KEY_NOT_FOUND_ERROR_IDENTIFIER = "Key not found";

    public static final String JSON_CONVERSION_ERROR = "JSON conversion error";
    public static final String STACK_OVERFLOW_ERROR = "Stack overflow";

    public static final String CONSTRUCT_FROM_CONVERSION_ERROR = "Conversion error";
    public static final String CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR = "Cyclic value reference error";
    public static final String MERGE_JSON_ERROR = "Merge json error";
    public static final String STRING_OPERATION_ERROR = "String operation error";
    public static final String XML_OPERATION_ERROR = "XML operation error";
    public static final String MAP_KEY_NOT_FOUND_ERROR = KEY_NOT_FOUND_ERROR_IDENTIFIER;
    public static final String TABLE_KEY_NOT_FOUND_ERROR = KEY_NOT_FOUND_ERROR_IDENTIFIER;
    public static final String TABLE_KEY_CYCLIC_VALUE_REFERENCE_ERROR = "Cyclic value reference error";
    public static final String TABLE_HAS_A_VALUE_FOR_KEY_ERROR = "Key constraint violation";
    public static final String ILLEGAL_LIST_INSERTION_ERROR = "Illegal list insertion";
    public static final String FUTURE_CANCELLED = "Future already cancelled";

    public static final String ASYNC_CALL_INSIDE_LOCK = "Async call inside lock error";
}
