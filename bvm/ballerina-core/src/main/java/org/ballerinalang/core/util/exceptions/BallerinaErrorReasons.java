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
package org.ballerinalang.core.util.exceptions;

/**
 * This is a temporary class for reasons for Ballerina errors from the VM either returned or causing panic.
 *
 * @since 0.990.0
 */
public class BallerinaErrorReasons {

    private static final String BALLERINA_PREFIX = "{ballerina}";

    public static final String CLONE_ERROR = BALLERINA_PREFIX.concat("CloneError");
    public static final String FREEZE_ERROR = BALLERINA_PREFIX.concat("FreezeError");
    public static final String STAMP_ERROR = BALLERINA_PREFIX.concat("StampError");
    public static final String CYCLIC_VALUE_REFERENCE_ERROR = BALLERINA_PREFIX.concat("CyclicValueReferenceError");
    public static final String CONVERSION_ERROR = BALLERINA_PREFIX.concat("ConversionError");

    public static final String TYPE_CAST_ERROR = BALLERINA_PREFIX.concat("TypeCastError");
    public static final String NUMBER_CONVERSION_ERROR = BALLERINA_PREFIX.concat("NumberConversionError");
    public static final String TRANSACTION_ERROR = BALLERINA_PREFIX.concat("TransactionError");

    public static final String JSON_OPERATION_ERROR = BALLERINA_PREFIX.concat("JSONOperationError");
    public static final String JSON_CONVERSION_ERROR = BALLERINA_PREFIX.concat("JSONConversionError");

    public static final String STRING_OPERATION_ERROR = BALLERINA_PREFIX.concat("StringOperationError");

    public static final String TABLE_OPERATION_ERROR = BALLERINA_PREFIX.concat("TableOperationError");
    public static final String TABLE_CLOSED_ERROR = BALLERINA_PREFIX.concat("TableClosedError");

    public static final String XML_CREATION_ERROR = BALLERINA_PREFIX.concat("XMLCreationError");
    public static final String XML_OPERATION_ERROR = BALLERINA_PREFIX.concat("XMLOperationError");

    public static final String CONCURRENT_MODIFICATION_ERROR = BALLERINA_PREFIX.concat("ConcurrentModification");
    public static final String INVALID_UPDATE_ERROR = BALLERINA_PREFIX.concat("InvalidUpdate");
    public static final String INDEX_OUT_OF_RANGE_ERROR = BALLERINA_PREFIX.concat("IndexOutOfRange");
    public static final String INHERENT_TYPE_VIOLATION_ERROR = BALLERINA_PREFIX.concat("InherentTypeViolation");
    public static final String KEY_NOT_FOUND_ERROR = BALLERINA_PREFIX.concat("KeyNotFound");

    public static final String DIVISION_BY_ZERO_ERROR = BALLERINA_PREFIX.concat("DivisionByZero");

    public static final String STACK_OVERFLOW_ERROR = BALLERINA_PREFIX.concat("StackOverflow");
}
