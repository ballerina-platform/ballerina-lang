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
package io.ballerina.runtime.internal.util.exceptions;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.DECIMAL_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.FUTURE_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.MAP_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.REGEXP_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.STRING_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.TABLE_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.VALUE_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.XML_LANG_LIB;

/**
 * This is a temporary class for reasons for Ballerina errors from the VM either returned or causing panic.
 *
 * @since 0.990.0
 */
public class BallerinaErrorReasons {

    private static final String BALLERINA_PREFIX = "{ballerina}";
    private static final String BALLERINA_ORG_PREFIX = "{ballerina/";
    private static final String CLOSING_CURLY_BRACE = "}";

    public static final BString TYPE_CAST_ERROR = StringUtils.fromString(BALLERINA_PREFIX.concat("TypeCastError"));
    public static final BString NUMBER_CONVERSION_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("NumberConversionError"));
    public static final BString JSON_OPERATION_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("JSONOperationError"));

    public static final BString DIVISION_BY_ZERO_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("DivisionByZero"));
    public static final String NUMBER_OVERFLOW_ERROR_IDENTIFIER = "NumberOverflow";
    public static final BString NUMBER_OVERFLOW =
            StringUtils.fromString(BALLERINA_PREFIX.concat(NUMBER_OVERFLOW_ERROR_IDENTIFIER));
    public static final BString LARGE_EXPONENT_ERROR = StringUtils.fromString(BALLERINA_PREFIX.concat(
            "DecimalExponentError"));
    public static final BString ARITHMETIC_OPERATION_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("ArithmeticOperationError"));
    public static final BString QUANTIZE_ERROR = getModulePrefixedReason(DECIMAL_LANG_LIB, "QuantizeError");
    public static final BString JAVA_NULL_REFERENCE_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("JavaNullReferenceError"));
    public static final String JAVA_CLASS_NOT_FOUND_ERROR = BALLERINA_PREFIX.concat("JavaClassNotFoundError");
    public static final BString JAVA_OUT_OF_MEMORY_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("OutOfMemoryError"));

    public static final BString BALLERINA_PREFIXED_CONVERSION_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("ConversionError"));
    public static final BString ITERATOR_MUTABILITY_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("IteratorMutabilityError"));

    public static final String NUMBER_PARSING_ERROR_IDENTIFIER = "NumberParsingError";
    public static final String BOOLEAN_PARSING_ERROR_IDENTIFIER = "BooleanParsingError";
    public static final String INVALID_UPDATE_ERROR_IDENTIFIER = "InvalidUpdate";
    public static final String INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER = "IndexOutOfRange";
    public static final String INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER = "InherentTypeViolation";
    public static final String INCOMPATIBLE_ARGUMENTS = "IncompatibleArguments";
    public static final String OPERATION_NOT_SUPPORTED_IDENTIFIER = "OperationNotSupported";
    public static final String KEY_NOT_FOUND_ERROR_IDENTIFIER = "KeyNotFound";
    public static final String INVALID_TYPE_TO_SORT = "SortOperationError";
    public static final String UNORDERED_TYPES = "UnorderedTypesError";
    public static final String LENGTH_GREATER_THAT_2147483647_NOT_YET_SUPPORTED =
            "length greater that '2147483647' not yet supported";

    public static final BString INDEX_OUT_OF_RANGE_ERROR = StringUtils
            .fromString(INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER);
    public static final BString OPERATION_NOT_SUPPORTED_ERROR =
            StringUtils.fromString(OPERATION_NOT_SUPPORTED_IDENTIFIER);

    public static final BString JSON_CONVERSION_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("JSONConversionError"));
    public static final BString STACK_OVERFLOW_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("StackOverflow"));

    public static final BString VALUE_LANG_LIB_CONVERSION_ERROR = getModulePrefixedReason(VALUE_LANG_LIB,
                                                                                          "ConversionError");
    public static final BString VALUE_LANG_LIB_CYCLIC_VALUE_REFERENCE_ERROR =
            getModulePrefixedReason(VALUE_LANG_LIB, "CyclicValueReferenceError");
    public static final BString MERGE_JSON_ERROR = getModulePrefixedReason(VALUE_LANG_LIB, "MergeJsonError");
    public static final BString FROM_BAL_STRING_ERROR = getModulePrefixedReason(VALUE_LANG_LIB, "FromBalStringError");
    public static final BString STRING_OPERATION_ERROR = getModulePrefixedReason(STRING_LANG_LIB,
                                                                                 "StringOperationError");
    public static final BString XML_OPERATION_ERROR = getModulePrefixedReason(XML_LANG_LIB, "XMLOperationError");
    public static final BString MAP_KEY_NOT_FOUND_ERROR = getModulePrefixedReason(MAP_LANG_LIB,
                                                                                  KEY_NOT_FOUND_ERROR_IDENTIFIER);
    public static final BString TABLE_KEY_NOT_FOUND_ERROR = getModulePrefixedReason(TABLE_LANG_LIB,
                                                                                    KEY_NOT_FOUND_ERROR_IDENTIFIER);
    public static final BString TABLE_KEY_CYCLIC_VALUE_REFERENCE_ERROR =
            getModulePrefixedReason(TABLE_LANG_LIB, "CyclicValueReferenceError");
    public static final BString TABLE_HAS_A_VALUE_FOR_KEY_ERROR = getModulePrefixedReason(TABLE_LANG_LIB,
                                                                                          "KeyConstraintViolation");
    public static final BString ILLEGAL_LIST_INSERTION_ERROR = getModulePrefixedReason(ARRAY_LANG_LIB,
                                                                                       "IllegalListInsertion");
    public static final BString FUTURE_CANCELLED = getModulePrefixedReason(FUTURE_LANG_LIB, "FutureAlreadyCancelled");

    public static final BString ASYNC_CALL_INSIDE_LOCK =
            StringUtils.fromString(BALLERINA_PREFIX.concat("AsyncCallInsideLockError"));
    public static final BString UNORDERED_TYPES_ERROR = StringUtils.fromString(UNORDERED_TYPES);
    public static final BString UNSUPPORTED_DECIMAL_ERROR = StringUtils.fromString(BALLERINA_PREFIX.concat(
            "UnsupportedDecimalError"));

    public static final String INVALID_FRACTION_DIGITS_ERROR = "InvalidFractionDigits";
    public static final BString FAILED_TO_DECODE_BYTES = StringUtils.fromString("FailedToDecodeBytes");

    public static final String REG_EXP_PARSING_ERROR_IDENTIFIER = "RegularExpressionParsingError";
    public static final BString REG_EXP_PARSING_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat(REG_EXP_PARSING_ERROR_IDENTIFIER));

    public static final BString REGEXP_OPERATION_ERROR = getModulePrefixedReason(REGEXP_LANG_LIB,
            "RegularExpressionOperationError");

    public static BString getModulePrefixedReason(String moduleName, String identifier) {
        return StringUtils.fromString(BALLERINA_ORG_PREFIX.concat(moduleName)
                                              .concat(CLOSING_CURLY_BRACE).concat(identifier));
    }
}
