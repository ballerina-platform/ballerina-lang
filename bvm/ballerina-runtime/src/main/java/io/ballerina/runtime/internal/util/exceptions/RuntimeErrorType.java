/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.util.exceptions;

/**
 * Error codes and Error message keys to represent the runtime errors.
 */

public enum RuntimeErrorType {

    // TypeCastError
    TYPE_CAST_ERROR("TypeCastError", "incompatible.types.cannot.cast", "RT-0001"),

    // IncompatibleTypeError
    INCOMPATIBLE_TYPE_ERROR("IncompatibleType", "incompatible.types", "RT-0002"),

    // IndexOutOfRangeError
    INDEX_NUMBER_TOO_LARGE("IndexNumberTooLarge", "index.number.too.large", "RT-0003"),
    ARRAY_INDEX_OUT_OF_RANGE("ArrayIndexOutOfRange", "array.index.out.of.range", "RT-0004"),
    TUPLE_INDEX_OUT_OF_RANGE("TupleIndexOutOfRange", "tuple.index.out.of.range", "RT-0005"),
    STRING_INDEX_OUT_OF_RANGE("StringIndexOutOfRange", "string.index.out.of.range", "RT-0006"),
    SUBSTRING_INDEX_OUT_OF_RANGE("StringIndexOutOfRange", "substring.index.out.of.range", "RT-0006"),
    XML_SEQUENCE_INDEX_OUT_OF_RANGE("XmlSequenceIndexOutOfRange", "xml.index.out.of.range", "RT-0007"),

    // StringOperationError
    INVALID_SUBSTRING_RANGE("InvalidSubstringRange", "invalid.substring.range", "RT-0008"),


    // ConversionError
    INCOMPATIBLE_NUMBER_CONVERSION_ERROR("NumberConversionError", "incompatible.convert.operation", "RT-0035"),
    SIMPLE_TYPE_NUMBER_CONVERSION_ERROR("NumberConversionError",
            "incompatible.simple.type.convert.operation", "RT-0035");


    private String errorName;
    private String errorMsgKey;
    private String errorCode;


    RuntimeErrorType(String errorName, String errorMsgKey, String errorCode) {
        this.errorName = errorName;
        this.errorMsgKey = errorMsgKey;
        this.errorCode = errorCode;
    }



    public String getErrorName() {
        return errorName;
    }

    public String getErrorMsgKey() {
        return errorMsgKey;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
