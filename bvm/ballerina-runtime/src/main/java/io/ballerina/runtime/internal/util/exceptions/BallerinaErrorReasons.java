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

import static io.ballerina.runtime.api.constants.RuntimeConstants.FUTURE_LANG_LIB;
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


    public static final BString DIVISION_BY_ZERO_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("DivisionByZero"));
    public static final BString NUMBER_OVERFLOW = StringUtils.fromString(BALLERINA_PREFIX.concat("NumberOverflow"));
    public static final BString ARITHMETIC_OPERATION_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("ArithmeticOperationError"));
    public static final BString JAVA_NULL_REFERENCE_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("JavaNullReferenceError"));
    public static final String JAVA_CLASS_NOT_FOUND_ERROR = BALLERINA_PREFIX.concat("JavaClassNotFoundError");

    public static final BString BALLERINA_PREFIXED_CONVERSION_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("ConversionError"));

    public static final String NUMBER_PARSING_ERROR_IDENTIFIER = "NumberParsingError";
    public static final String INVALID_UPDATE_ERROR_IDENTIFIER = "InvalidUpdate";
    public static final String INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER = "IndexOutOfRange";
    public static final String INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER = "InherentTypeViolation";
    public static final String OPERATION_NOT_SUPPORTED_IDENTIFIER = "OperationNotSupported";
    public static final String INVALID_TYPE_TO_SORT = "SortOperationError";

    public static final BString OPERATION_NOT_SUPPORTED_ERROR =
            StringUtils.fromString(OPERATION_NOT_SUPPORTED_IDENTIFIER);

    public static final BString JSON_CONVERSION_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("JSONConversionError"));
    public static final BString STACK_OVERFLOW_ERROR =
            StringUtils.fromString(BALLERINA_PREFIX.concat("StackOverflow"));

    public static final BString VALUE_LANG_LIB_CONVERSION_ERROR = getModulePrefixedReason(VALUE_LANG_LIB,
                                                                                          "ConversionError");
    public static final BString MERGE_JSON_ERROR = getModulePrefixedReason(VALUE_LANG_LIB, "MergeJsonError");
    public static final BString FROM_BAL_STRING_ERROR = getModulePrefixedReason(VALUE_LANG_LIB, "FromBalStringError");
    public static final BString XML_OPERATION_ERROR = getModulePrefixedReason(XML_LANG_LIB, "XMLOperationError");
    public static final BString FUTURE_CANCELLED = getModulePrefixedReason(FUTURE_LANG_LIB, "FutureAlreadyCancelled");

    public static final BString ASYNC_CALL_INSIDE_LOCK =
            StringUtils.fromString(BALLERINA_PREFIX.concat("AsyncCallInsideLockError"));

    public static BString getModulePrefixedReason(String moduleName, String identifier) {
        return StringUtils.fromString(BALLERINA_ORG_PREFIX.concat(moduleName)
                                              .concat(CLOSING_CURLY_BRACE).concat(identifier));
    }
}
