/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANYDATA_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_ENV;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BIG_DECIMAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BYTE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_ARRAY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_INITIAL_VALUE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_LIST_INITIAL_VALUE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_MAPPING_INITIAL_VALUE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_XML_QNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CHANNEL_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.COLLECTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DOUBLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FLOAT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_PARAMETER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTEGER_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTERSECTABLE_REFERENCE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTERSECTION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_THREAD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JSON_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_STREAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.METHOD_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.METHOD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NEVER_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NULL_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OPERAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PATH;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.READONLY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REF_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_ASSERTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_ATOM_QUANTIFIER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_CAPTURING_GROUP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_CHAR_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_CHAR_ESCAPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_CHAR_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_CHAR_SET_RANGE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_DISJUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_FLAG_EXPR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_FLAG_ON_OFF;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_QUANTIFIER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_SEQUENCE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REG_EXP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RESOURCE_METHOD_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_ERRORS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_REGISTRY_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SERVICE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STACK;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_BUILDER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.THROWABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_ID_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_REF_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_CREATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VARIABLE_KEY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WD_CHANNELS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WORKER_DATA_CHANNEL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_SEQUENCE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;

/**
 * JVM bytecode generation related constants.
 *
 * @since 1.2.0
 */
public class JvmSignatures {

    public static final String INIT_ERROR_WITH_TYPE =
            "(L" + TYPE + ";L" + B_STRING_VALUE + ";L" + BERROR + ";L" + OBJECT + ";)V";
    public static final String INIT_XML_QNAME =
            "(L" + B_STRING_VALUE + ";L" + B_STRING_VALUE + ";L" + B_STRING_VALUE + ";)V";
    public static final String ADD_COLLECTION = "(L" + COLLECTION + ";)Z";
    public static final String ADD_MODULE_CONFIG_DATA = "(L" + MAP + ";L" + MODULE + ";[L" + VARIABLE_KEY + ";)V";
    public static final String ADD_SHUTDOWN_HOOK = "(L" + JAVA_THREAD + ";)V";
    public static final String ADD_TYPE_ID = "(L" + MODULE + ";L" + STRING_VALUE + ";Z)V";
    public static final String ADD_VALUE_CREATOR = "(L" + STRING_VALUE + ";L" + STRING_VALUE + ";L" + STRING_VALUE +
            ";ZL" + VALUE_CREATOR + ";)V";
    public static final String ANNOTATION_GET_STRAND =
            "(L" + FUNCTION_POINTER + ";L" + STRING_VALUE + ";)L" + STRING_VALUE + ";";
    public static final String ANY_TO_BYTE = "(L" + OBJECT + ";)I";
    public static final String ANY_TO_DECIMAL = "(L" + OBJECT + ";)L" + DECIMAL_VALUE + ";";
    public static final String ANY_TO_JBOOLEAN = "(L" + OBJECT + ";)Z";
    public static final String ANY_TO_JBYTE = "(L" + OBJECT + ";)B";
    public static final String ANY_TO_JCHAR = "(L" + OBJECT + ";)C";
    public static final String ANY_TO_JDOUBLE = "(L" + OBJECT + ";)D";
    public static final String ANY_TO_JFLOAT = "(L" + OBJECT + ";)F";
    public static final String ANY_TO_JLONG = "(L" + OBJECT + ";)J";
    public static final String ANY_TO_JSTRING = "(L" + OBJECT + ";)S";
    public static final String ARRAY_ADD_BSTRING = "(JL" + JvmConstants.B_STRING_VALUE + ";)V";
    public static final String ARRAY_ADD_OBJECT = "(JL" + OBJECT + ";)V";
    public static final String BAL_ENV_PARAM = "(L" + BAL_ENV + ";";
    public static final String BBB = "(L" + STRING_VALUE + ";)L" + OBJECT + ";";
    public static final String BOBJECT_CALL =
            "(L" + STRAND_CLASS + ";L" + STRING_VALUE + ";[L" + OBJECT + ";)L" + OBJECT + ";";
    public static final String BOBJECT_GET = "(L" + B_STRING_VALUE + ";)L" + OBJECT + ";";
    public static final String BOOLEAN_TO_STRING = "(Z)L" + STRING_VALUE + ";";
    public static final String BOOLEAN_VALUE_OF_METHOD = "(Z)L" + BOOLEAN_VALUE + ";";
    public static final String BSTRING_CONCAT = "(L" + B_STRING_VALUE + ";)L" + B_STRING_VALUE + ";";
    public static final String CAST_B_MAPPING_INITIAL_VALUE_ENTRY = "[L" + B_MAPPING_INITIAL_VALUE_ENTRY + ";";
    public static final String CHECK_CAST = "(L" + OBJECT + ";L" + TYPE + ";)L" + OBJECT + ";";
    public static final String CHECK_FIELD_UPDATE = "(L" + STRING_VALUE + ";L" + OBJECT + ";)V";
    public static final String CHECK_IS_TYPE = "(L" + OBJECT + ";L" + TYPE + ";)Z";
    public static final String CHECKPOINT_CALL = "(L" + BAL_ENV + ";L" + B_STRING_VALUE + ";L" + B_STRING_VALUE + ";" +
            "JJ)V";
    public static final String COLLECTION_OP = "(L" + COLLECTION + ";)V";
    public static final String COMPARE_DECIMALS = "(L" + DECIMAL_VALUE + ";L" + DECIMAL_VALUE + ";)Z";
    public static final String COMPARE_OBJECTS = "(L" + OBJECT + ";L" + OBJECT + ";)Z";
    public static final String CONTAINS_KEY = "(L" + STRING_VALUE + ";L" + OBJECT + ";)Z";
    public static final String CREATE_CANCELLED_FUTURE_ERROR = "()L" + ERROR_VALUE + ";";
    public static final String CREATE_ERROR =
            "(L" + STRING_VALUE + ";L" + B_STRING_VALUE + ";L" + BERROR + ";L" + OBJECT + ";)L" + BERROR + ";";
    public static final String CREATE_ERROR_FROM_THROWABLE = "(L" + THROWABLE + ";)L" + ERROR_VALUE + ";";
    public static final String CREATE_OBJECT =
            "(L" + STRING_VALUE + ";L" + SCHEDULER + ";L" + STRAND_CLASS + ";L" + MAP + ";[L" + OBJECT + ";)L" +
                    B_OBJECT + ";";
    public static final String CREATE_RECORD = "(L" + STRING_VALUE + ";)L" + MAP_VALUE + ";";
    public static final String CREATE_RECORD_WITH_MAP =
            "(L" + STRING_VALUE + ";)L" + MAP_VALUE + "<L" + STRING_VALUE + ";L" + OBJECT + ";>;";
    public static final String CREATE_XML_COMMENT = "(L" + B_STRING_VALUE + ";Z)L" + XML_VALUE + ";";
    public static final String CREATE_XML_ELEMENT =
            "(L" + B_XML_QNAME + ";L" + B_STRING_VALUE + ";Z)L" + XML_VALUE + ";";
    public static final String CREATE_XML_PI = "(L" + B_STRING_VALUE + ";L" + B_STRING_VALUE + ";Z)L" + XML_VALUE + ";";
    public static final String CREATE_XML_TEXT = "(L" + B_STRING_VALUE + ";)L" + XML_VALUE + ";";
    public static final String CRETAE_XML_SEQUENCE = "()L" + XML_SEQUENCE + ";";
    public static final String DECIMAL_NEGATE = "()L" + DECIMAL_VALUE + ";";
    public static final String DECIMAL_TO_HANDLE = "(L" + OBJECT + ";)L" + HANDLE_VALUE + ";";
    public static final String DECIMAL_VALUE_OF_BOOLEAN = "(B)L" + DECIMAL_VALUE + ";";
    public static final String DECIMAL_VALUE_OF_CHAR = "(C)L" + DECIMAL_VALUE + ";";
    public static final String DECIMAL_VALUE_OF_DOUBLE = "(D)L" + DECIMAL_VALUE + ";";
    public static final String DECIMAL_VALUE_OF_FLOAT = "(F)L" + DECIMAL_VALUE + ";";
    public static final String DECIMAL_VALUE_OF_INT = "(I)L" + DECIMAL_VALUE + ";";
    public static final String DECIMAL_VALUE_OF_LONG = "(J)L" + DECIMAL_VALUE + ";";
    public static final String DECIMAL_VALUE_OF_SHORT = "(S)L" + DECIMAL_VALUE + ";";
    public static final String INT_TO_STRING = "(I)L" + STRING_VALUE + ";";
    public static final String DOUBLE_TO_STRING = "(D)L" + STRING_VALUE + ";";
    public static final String DOUBLE_VALUE_OF_METHOD = "(D)L" + DOUBLE_VALUE + ";";
    public static final String ERROR_CALL = "(L" + BAL_ENV + ";L" + ERROR_VALUE + ";)V";
    public static final String ERROR_INIT = "(L" + TYPE + ";L" + B_STRING_VALUE + ";L" + BERROR + ";L" + OBJECT + ";)V";
    public static final String FP_INIT = "(L" + FUNCTION + ";L" + TYPE + ";L" + STRING_VALUE + ";Z)V";
    public static final String FUNCTION_CALL =
            "(L" + STRAND_CLASS + ";L" + STRING_VALUE + ";[L" + OBJECT + ";)L" + OBJECT + ";";
    public static final String FROM_STRING = "(L" + STRING_VALUE + ";)L" + B_STRING_VALUE + ";";
    public static final String GET_ANNOTATION_VALUE =
            "(L" + TYPEDESC_VALUE + ";L" + B_STRING_VALUE + ";)L" + OBJECT + ";";
    public static final String GET_ANON_TYPE = "(IL" + STRING_VALUE + ";)L" + TYPE + ";";
    public static final String GET_ARRAY_TYPE_IMPL = "L" + ARRAY_TYPE_IMPL + ";";
    public static final String GET_ARRAY_VALUE = "L" + ARRAY_VALUE + ";";
    public static final String GET_ATTRAIBUTE_MAP = "()L" + MAP_VALUE + ";";
    public static final String GET_BDECIMAL = "L" + DECIMAL_VALUE + ";";
    public static final String GET_BERROR = "L" + BERROR + ";";
    public static final String GET_BOBJECT = "L" + B_OBJECT + ";";
    public static final String GET_BSTRING = "L" + B_STRING_VALUE + ";";
    public static final String GET_REGEXP = "L" + REG_EXP_VALUE + ";";
    public static final String GET_BSTRING_FOR_ARRAY_INDEX = "(J)L" + JvmConstants.B_STRING_VALUE + ";";
    public static final String GET_ERROR_TYPE = "L" + ERROR_TYPE + ";";
    public static final String GET_ERROR_VALUE = "L" + ERROR_VALUE + ";";
    public static final String GET_FUNCTION = "()L" + FUNCTION + ";";
    public static final String GET_FUNCTION_POINTER = "L" + FUNCTION_POINTER + ";";
    public static final String GET_FUTURE_VALUE = "L" + FUTURE_VALUE + ";";
    public static final String GET_HANDLE_VALUE = "L" + HANDLE_VALUE + ";";
    public static final String GET_JSTRING = "()L" + STRING_VALUE + ";";
    public static final String GET_RUNTIME_REGISTRY = "L" + RUNTIME_REGISTRY_CLASS + ";";
    public static final String GET_RUNTIME_REGISTRY_CLASS = "()L" + RUNTIME_REGISTRY_CLASS + ";";
    public static final String GET_LOCK_FROM_MAP = "(L" + STRING_VALUE + ";)L" + LOCK_VALUE + ";";
    public static final String GET_LOCK_MAP = "(L" + STRING_VALUE + ";)L" + LOCK_VALUE + ";";
    public static final String GET_MAIN_ARGS = "()[L" + OBJECT + ";";
    public static final String GET_MAP_ARRAY = "[L" + MAP_VALUE + ";";
    public static final String GET_MAP_VALUE = "L" + MAP_VALUE + ";";
    public static final String GET_MODULE = "L" + MODULE + ";";
    public static final String GET_OBJECT = "L" + OBJECT + ";";
    public static final String GET_OBJECT_FOR_STRING = "(L" + STRING_VALUE + ";)L" + OBJECT + ";";
    public static final String GET_RUNTIME = "()L" + JvmConstants.JAVA_RUNTIME + ";";
    public static final String GET_RUNTIME_ERROR = "L" + RUNTIME_ERRORS + ";";
    public static final String GET_RUNTIME_EXCEPTION =
            "(L" + STRING_VALUE + ";L" + RUNTIME_ERRORS + ";[L" + OBJECT + ";)L" + ERROR_VALUE + ";";
    public static final String GET_SCHEDULER = "L" + SCHEDULER + ";";
    public static final String GET_STRAND = "L" + STRAND_CLASS + ";";
    public static final String GET_STRAND_METADATA = "L" + STRAND_METADATA + ";";
    public static final String GET_STREAM_VALUE = "L" + STREAM_VALUE + ";";
    public static final String GET_STRING = "L" + STRING_VALUE + ";";
    public static final String GET_STRING_AT = "(L" + B_STRING_VALUE + ";J)L" + B_STRING_VALUE + ";";
    public static final String GET_STRING_FROM_ARRAY = "(J)L" + OBJECT + ";";
    public static final String GET_TABLE_VALUE_IMPL = "L" + TABLE_VALUE_IMPL + ";";
    public static final String GET_THROWABLE = "L" + THROWABLE + ";";
    public static final String GET_TUPLE_TYPE_IMPL = "L" + TUPLE_TYPE_IMPL + ";";
    public static final String GET_TYPE = "L" + TYPE + ";";
    public static final String GET_TYPEDESC = "L" + TYPEDESC_VALUE + ";";
    public static final String GET_TYPEDESC_OF_OBJECT = "(L" + OBJECT + ";)L" + TYPEDESC_VALUE + ";";
    public static final String GET_UNION_TYPE_IMPL = "L" + UNION_TYPE_IMPL + ";";
    public static final String GET_ERROR_TYPE_IMPL = "L" + ERROR_TYPE_IMPL + ";";
    public static final String GET_TYPE_REF_TYPE_IMPL = "L" + TYPE_REF_TYPE_IMPL + ";";
    public static final String GET_WD_CHANNELS = "L" + WD_CHANNELS + ";";
    public static final String GET_WORKER_DATA_CHANNEL = "(L" + STRING_VALUE + ";)L" + WORKER_DATA_CHANNEL + ";";
    public static final String GET_XML = "L" + XML_VALUE + ";";
    public static final String HANDLE_CHANNEL_ERROR = "([L" + CHANNEL_DETAILS + ";L" + ERROR_VALUE + ";)V";
    public static final String HANDLE_ERROR_RETURN = "(L" + OBJECT + ";)V";
    public static final String HANDLE_FLUSH = "([L" + CHANNEL_DETAILS + ";)L" + ERROR_VALUE + ";";
    public static final String HANDLE_MAP_STORE = "(L" + MAP_VALUE + ";L" + B_STRING_VALUE + ";L" + OBJECT + ";)V";
    public static final String HANDLE_STOP_PANIC = "(L" + THROWABLE + ";)V";
    public static final String HANDLE_TABLE_STORE = "(L" + TABLE_VALUE + ";L" + OBJECT + ";L" + OBJECT + ";)V";
    public static final String HANDLE_THROWABLE = "(L" + JvmConstants.THROWABLE + ";)V";
    public static final String HANDLE_WAIT_ANY = "(L" + LIST + ";)L" + STRAND_CLASS + "$WaitResult;";
    public static final String HANDLE_WAIT_MULTIPLE = "(L" + MAP + ";L" + MAP_VALUE + ";)V";
    public static final String HANDLE_WORKER_ERROR =
            "(L" + REF_VALUE + ";L" + STRAND_CLASS + ";[L" + CHANNEL_DETAILS + ";)V";
    public static final String INIT_ARRAY = "(L" + TYPE + ";[L" + B_LIST_INITIAL_VALUE_ENTRY + ";)V";
    public static final String INIT_ARRAY_TYPE_IMPL = "(L" + TYPE + ";IZI)V";
    public static final String INIT_ARRAY_WITH_INITIAL_VALUES =
            "(L" + TYPE + ";[L" + B_LIST_INITIAL_VALUE_ENTRY + ";L" + TYPEDESC_VALUE + ";)V";
    public static final String INIT_BAL_ENV = "(L" + STRAND_CLASS + ";L" + MODULE + ";)V";
    public static final String INIT_CHANNEL_DETAILS = "(L" + STRING_VALUE + ";ZZ)V";
    public static final String INIT_CLI_SPEC = "(L" + OPTION + ";[L" + OPERAND + ";[L" + STRING_VALUE + ";)V";
    public static final String INIT_CONFIG = "([L" + STRING_VALUE + ";[L" + PATH + ";L" + STRING_VALUE + ";)V";
    public static final String INIT_CONFIGURABLES =
            "(L" + MODULE + ";L" + MAP + ";[L" + STRING_VALUE + ";[L" + PATH + ";L" + STRING_VALUE + ";)V";
    public static final String INIT_DECIMAL = "(L" + BIG_DECIMAL + ";)V";
    public static final String INIT_ERROR = "(L" + B_STRING_VALUE + ";)V";
    public static final String INIT_ERROR_TYPE_IMPL = "(L" + STRING_VALUE + ";L" + MODULE + ";)V";
    public static final String INIT_FIELD_IMPL = "(L" + TYPE + ";L" + STRING_VALUE + ";J)V";
    public static final String INIT_FINITE_TYPE_IMPL = "(L" + STRING_VALUE + ";L" + STRING_VALUE + ";L" + SET + ";I)V";
    public static final String INIT_FUNCTION_PARAM = "(L" + STRING_VALUE + ";ZL" + STRING_VALUE + ";L" + TYPE + ";)V";
    public static final String INIT_FUNCTION_TYPE_IMPL = "(L" + MODULE + ";J)V";
    public static final String INIT_FUNCTION_TYPE_IMPL_WITH_PARAMS = "(L" + MODULE + ";[L" + FUNCTION_PARAMETER + ";" +
            "L" + TYPE + ";L" + TYPE + ";" + "JL" + STRING_VALUE + ";)V";
    public static final String INIT_INTERSECTION_TYPE_WITH_REFERENCE_TYPE =
            "(L" + MODULE + ";[L" + TYPE + ";L" + INTERSECTABLE_REFERENCE_TYPE + ";IZ)V";
    public static final String INIT_INTERSECTION_TYPE_WITH_TYPE = "(L" + MODULE + ";[L" + TYPE + ";L" + TYPE + ";IZ)V";
    public static final String INIT_LIST_INITIAL_EXPRESSION_ENTRY = "(L" + OBJECT + ";)V";
    public static final String INIT_LIST_INITIAL_SPREAD_ENTRY = "(L" + B_ARRAY + ";)V";
    public static final String INIT_RUNTIME_REGISTRY = "(L" + RUNTIME_REGISTRY_CLASS + ";)V";
    public static final String INIT_MAPPING_INITIAL_SPREAD_FIELD_ENTRY = "(L" + B_MAP + ";)V";
    public static final String INIT_MODULE = "(L" + STRING_VALUE + ";L" + STRING_VALUE + ";L" + STRING_VALUE + ";Z)V";
    public static final String INIT_NON_BMP_STRING_VALUE = "(L" + STRING_VALUE + ";[I)V";
    public static final String INIT_OBJECT = "(L" + STRING_VALUE + ";L" + MODULE + ";J)V";
    public static final String INIT_OPERAND = "(ZL" + STRING_VALUE + ";L" + TYPE + ";)V";
    public static final String INIT_OPTION = "(L" + TYPE + ";I)V";
    public static final String INIT_PARAMETERIZED_TYPE_IMPL = "(L" + TYPE + ";I)V";
    public static final String INIT_STRAND =
            "(L" + STRING_VALUE + ";L" + STRAND_METADATA + ";L" + SCHEDULER + ";L" + STRAND_CLASS + ";L" + MAP + ";)V";
    public static final String INIT_STRAND_METADATA =
            "(L" + STRING_VALUE + ";L" + STRING_VALUE + ";L" + STRING_VALUE + ";L" + STRING_VALUE + ";L" +
                    STRING_VALUE + ";)V";
    public static final String INIT_STREAM_TYPE_IMPL = "(L" + TYPE + ";L" + TYPE + ";)V";
    public static final String INIT_TABLE_TYPE_IMPL = "(L" + TYPE + ";L" + TYPE + ";Z)V";
    public static final String INIT_TABLE_TYPE_WITH_FIELD_NAME_LIST = "(L" + TYPE + ";[L" + STRING_VALUE + ";Z)V";
    public static final String INIT_TABLE_VALUE_IMPL = "(L" + TYPE + ";L" + ARRAY_VALUE + ";L" + ARRAY_VALUE +
            ";)V";
    public static final String INIT_TUPLE = "(L" + TYPE + ";[L" + B_LIST_INITIAL_VALUE_ENTRY + ";)V";
    public static final String INIT_TUPLE_TYPE_IMPL = "(L" + STRING_VALUE + ";L" + MODULE + ";IZZ)V";
    public static final String INIT_TYPEDESC = "(L" + TYPEDESC_VALUE + ";)V";
    public static final String INIT_UNION_TYPE_IMPL = "(L" + STRING_VALUE + ";L" + MODULE + ";IZJ)V";
    public static final String INIT_WITH_BOOLEAN = "(L" + TYPE + ";Z)V";
    public static final String INIT_WITH_STRING = "(L" + STRING_VALUE + ";)V";
    public static final String INITIAL_METHOD_DESC = "(L" + STRAND_CLASS + ";";
    public static final String INIT_TYPE_REF = "(L" + STRING_VALUE + ";L" + MODULE + ";IZ)V";
    public static final String INSTANTIATE = "(L" + STRAND_CLASS + ";[L" + B_INITIAL_VALUE_ENTRY + ";)L" + OBJECT + ";";
    public static final String INT_VALUE_OF_METHOD = "(I)L" + INT_VALUE + ";";
    public static final String INTI_VARIABLE_KEY =
            "(L" + MODULE + ";L" + STRING_VALUE + ";L" + TYPE + ";L" + STRING_VALUE + ";Z)V";
    public static final String IS_CONCURRENT = "(L" + FUNCTION_POINTER + ";)Z";
    public static final String JSON_GET_ELEMENT = "(L" + OBJECT + ";L" + B_STRING_VALUE + ";)L" + OBJECT + ";";
    public static final String JSON_SET_ELEMENT = "(L" + OBJECT + ";L" + STRING_VALUE + ";L" + OBJECT + ";)V";
    public static final String LAMBDA_MAIN = "([L" + OBJECT + ";)L" + OBJECT + ";";
    public static final String LAMBDA_STOP_DYNAMIC = "([L" + OBJECT + ";)L" + OBJECT + ";";
    public static final String LINKED_HASH_SET_OP = "(L" + LINKED_HASH_SET + ";)V";
    public static final String LOAD_ANY_TYPE = "L" + ANY_TYPE + ";";
    public static final String LOAD_ANYDATA_TYPE = "L" + ANYDATA_TYPE + ";";
    public static final String LOAD_BOOLEAN_TYPE = "L" + BOOLEAN_TYPE + ";";
    public static final String LOAD_BYTE_TYPE = "L" + BYTE_TYPE + ";";
    public static final String LOAD_DECIMAL_TYPE = "L" + DECIMAL_TYPE + ";";
    public static final String LOAD_FLOAT_TYPE = "L" + FLOAT_TYPE + ";";
    public static final String LOAD_HANDLE_TYPE = "L" + HANDLE_TYPE + ";";
    public static final String LOAD_INTEGER_TYPE = "L" + INTEGER_TYPE + ";";
    public static final String LOAD_JSON_TYPE = "L" + JSON_TYPE + ";";
    public static final String LOAD_NEVER_TYPE = "L" + NEVER_TYPE + ";";
    public static final String LOAD_NULL_TYPE = "L" + NULL_TYPE + ";";
    public static final String LOAD_OBJECT_TYPE = "L" + OBJECT_TYPE + ";";
    public static final String LOAD_READONLY_TYPE = "L" + READONLY_TYPE + ";";
    public static final String LOAD_SERVICE_TYPE = "L" + SERVICE_TYPE + ";";
    public static final String LOAD_STRING_TYPE = "L" + STRING_TYPE + ";";
    public static final String LOAD_TYPE = "L" + TYPE + ";";
    public static final String LOAD_UNION_TYPE = "L" + UNION_TYPE + ";";
    public static final String LOAD_XML_TYPE = "L" + XML_TYPE + ";";
    public static final String LOCK = "(L" + STRAND_CLASS + ";)Z";
    public static final String LONG_STREAM_RANGE_CLOSED = "(JJ)L" + LONG_STREAM + ";";
    public static final String LONG_TO_STRING = "(J)L" + STRING_VALUE + ";";
    public static final String LONG_VALUE_OF = "(J)L" + LONG_VALUE + ";";
    public static final String MAP_PUT = "(L" + OBJECT + ";L" + OBJECT + ";)L" + OBJECT + ";";
    public static final String MAP_VALUES = "()L" + COLLECTION + ";";
    public static final String MAP_VALUES_WITH_COLLECTION = "()L" + COLLECTION + "<TV;>;";
    public static final String METHOD_STRING_PARAM = "(L" + JvmConstants.STRING_VALUE + ";)V";
    public static final String METHOD_TYPE_IMPL_ARRAY_PARAM = "([L" + METHOD_TYPE_IMPL + ";)V";
    public static final String METHOD_TYPE_IMPL_INIT =
            "(L" + STRING_VALUE + ";L" + MODULE + ";L" + OBJECT_TYPE_IMPL + ";L" + FUNCTION_TYPE_IMPL + ";J)V";
    public static final String METHOD_TYPE_IMPL_PARAM = "(L" + METHOD_TYPE_IMPL + ";)V";
    public static final String MODULE_START = "(L" + STRAND_CLASS + ";)L" + OBJECT + ";";
    public static final String OBJECT_SET = "(L" + STRING_VALUE + ";L" + B_STRING_VALUE + ";L" + OBJECT + ";)V";
    public static final String OBJECT_TYPE_DUPLICATE = "()L" + OBJECT_TYPE_IMPL + ";";
    public static final String OBJECT_TYPE_IMPL_INIT = "(L" + TYPE + ";)V";
    public static final String PANIC_IF_IN_LOCK = "(L" + STRAND_CLASS + ";)V";
    public static final String PASS_BSTRING_RETURN_OBJECT = "(L" + B_STRING_VALUE + ";)L" + OBJECT + ";";
    public static final String PASS_OBJECT_RETURN_OBJECT = "(L" + OBJECT + ";)L" + OBJECT + ";";
    public static final String PASS_OBJECT_RETURN_SAME_TYPE = "(L" + OBJECT + ";)TV;";
    public static final String POPULATE_ATTACHED_FUNCTION = "([L" + METHOD_TYPE_IMPL + ";)V";
    public static final String POPULATE_CONFIG_DATA = "()[L" + VARIABLE_KEY + ";";
    public static final String POPULATE_INITIAL_VALUES = "([L" + B_MAPPING_INITIAL_VALUE_ENTRY + ";)V";
    public static final String PROCESS_ANNOTATIONS = "(L" + MAP_VALUE + ";L" + TYPE + ";)V";
    public static final String PROCESS_FP_ANNOTATIONS =
            "(L" + FUNCTION_POINTER + ";L" + MAP_VALUE + ";L" + STRING_VALUE + ";)V";
    public static final String PROCESS_OBJ_CTR_ANNOTATIONS =
            "(L" + OBJECT_TYPE_IMPL + ";L" + MAP_VALUE + ";L" + STRAND_CLASS + ";)V";
    public static final String STACK_FRAMES = "L" + STACK + ";";
    public static final String RECORD_GET = "(L" + STRING_VALUE + ";L" + OBJECT + ";)L" + OBJECT + ";";
    public static final String RECORD_GET_KEYS = "()[L" + OBJECT + ";";
    public static final String RECORD_INIT_WRAPPER = "(L" + STRAND_CLASS + ";L" + MAP_VALUE + ";)V";
    public static final String RECORD_PUT = "(L" + STRING_VALUE + ";L" + OBJECT + ";L" + OBJECT + ";)L" + OBJECT + ";";
    public static final String RECORD_REMOVE = "(L" + STRING_VALUE + ";L" + OBJECT + ";)L" + OBJECT + ";";
    public static final String RECORD_SET = "()L" + SET + ";";
    public static final String RECORD_SET_MAP_ENTRY = "()L" + SET + "<L" + MAP_ENTRY + "<TK;TV;>;>;";
    public static final String RECORD_TYPE_IMPL_INIT =
            "(L" + STRING_VALUE + ";L" + STRING_VALUE + ";L" + MODULE + ";JZI)V";
    public static final String RECORD_VALUE_CLASS = "<K:L" + OBJECT + ";V:L" + OBJECT + ";>L" + MAP_VALUE_IMPL +
            "<TK;TV;>;L" + MAP_VALUE + "<TK;TV;>;";
    public static final String RESOURCE_METHOD_TYPE_ARRAY_PARAM = "([L" + RESOURCE_METHOD_TYPE + ";)V";
    public static final String RESOURCE_METHOD_TYPE_IMPL_INIT =
            "(L" + STRING_VALUE + ";L" + MODULE + ";L" + OBJECT_TYPE_IMPL + ";L" + FUNCTION_TYPE_IMPL + ";[L" +
                    TYPE + ";JL" + STRING_VALUE + ";[L" + STRING_VALUE + ";)V";
    public static final String RETURN_ARRAY_VALUE = ")L" + ARRAY_VALUE + ";";
    public static final String RETURN_B_OBJECT = ")L" + B_OBJECT + ";";
    public static final String RETURN_B_STRING_VALUE = ")L" + B_STRING_VALUE + ";";
    public static final String RETURN_REGEX_VALUE = ")L" + REG_EXP_VALUE + ";";
    public static final String RETURN_DECIMAL_VALUE = ")L" + DECIMAL_VALUE + ";";
    public static final String RETURN_ERROR_VALUE = ")L" + ERROR_VALUE + ";";
    public static final String RETURN_FUNCTION_POINTER = ")L" + FUNCTION_POINTER + ";";
    public static final String RETURN_FUTURE_VALUE = ")L" + FUTURE_VALUE + ";";
    public static final String RETURN_HANDLE_VALUE = ")L" + HANDLE_VALUE + ";";
    public static final String RETURN_JOBJECT = ")L" + OBJECT + ";";
    public static final String RETURN_MAP_VALUE = ")L" + MAP_VALUE + ";";
    public static final String RETURN_OBJECT = "()L" + OBJECT + ";";
    public static final String RETURN_STREAM_VALUE = ")L" + STREAM_VALUE + ";";
    public static final String RETURN_TABLE_VALUE_IMPL = ")L" + TABLE_VALUE_IMPL + ";";
    public static final String RETURN_TYPEDESC_VALUE = ")L" + TYPEDESC_VALUE + ";";
    public static final String RETURN_XML_VALUE = ")L" + XML_VALUE + ";";
    public static final String SCHEDULE_LOCAL = "([L" + OBJECT + ";L" + B_FUNCTION_POINTER + ";L" +
            STRAND_CLASS + ";" + "L" + TYPE + ";L" + STRING_VALUE + ";L" + STRAND_METADATA + ";)L" + FUTURE_VALUE + ";";
    public static final String SCHEDULE_TRANSACTIONAL_FUNCTION =
            "([L" + OBJECT + ";L" + B_FUNCTION_POINTER + ";L" + STRAND_CLASS + ";L" + TYPE + ";L" +
                    STRING_VALUE + ";" + "L" + STRAND_METADATA + ";)L" + FUTURE_VALUE + ";";
    public static final String SEND_DATA = "(L" + OBJECT + ";L" + STRAND_CLASS + ";)V";
    public static final String SET_ARRAY_ELEMENT = "(L" + TYPE + ";IZ)V";
    public static final String SET_DECIMAL_RETURN_DECIMAL = "(L" + DECIMAL_VALUE + ";)L" + DECIMAL_VALUE + ";";
    public static final String SET_IMMUTABLE_TYPE = "(L" + INTERSECTION_TYPE + ";)V";
    public static final String SET_LINKED_HASH_MAP = "(L" + LINKED_HASH_MAP + ";)V";
    public static final String SET_MAP = "(L" + MAP + ";)V";
    public static final String SET_METHODS = "([L" + METHOD_TYPE + ";)V";
    public static final String SET_ON_INIT = "(L" + B_STRING_VALUE + ";L" + OBJECT + ";)V";
    public static final String SET_RESOURCE_METHOD_TYPE_ARRAY = "([L" + RESOURCE_METHOD_TYPE + ";)V";
    public static final String SET_STRAND = "(L" + STRAND_CLASS + ";)V";
    public static final String SET_TYPE_ARRAY = "([L" + TYPE + ";)V";
    public static final String SET_TYPE_ID_SET = "(L" + TYPE_ID_SET + ";)V";
    public static final String SET_VALUE = "(L" + B_STRING_VALUE + ";L" + OBJECT + ";)V";
    public static final String START_CALLABLE_OBSERVATION =
            "(L" + BAL_ENV + ";L" + B_STRING_VALUE + ";L" + B_STRING_VALUE + ";JJL" + B_OBJECT + ";L" +
                    B_STRING_VALUE + ";ZZZ)V";
    public static final String START_RESOURCE_OBSERVATION =
            "(L" + BAL_ENV + ";L" + B_STRING_VALUE + ";L" + B_STRING_VALUE + ";JJL" + B_STRING_VALUE + ";L" +
                    B_STRING_VALUE + ";L" + B_STRING_VALUE + ";ZZ)V";
    public static final String STOP_OBSERVATION = "(L" + BAL_ENV + ";)V";
    public static final String STRING_BUILDER_APPEND = "(L" + STRING_VALUE + ";)L" + STRING_BUILDER + ";";
    public static final String SYNC_SEND_DATA = "(L" + OBJECT + ";L" + STRAND_CLASS + ";)L" + OBJECT + ";";
    public static final String TO_ARRAY = "([L" + OBJECT + ";)[L" + OBJECT + ";";
    public static final String TO_CHAR = "(L" + OBJECT + ";)L" + B_STRING_VALUE + ";";
    public static final String TO_STRING_RETURN = "()L" + STRING_VALUE + ";";
    public static final String TRY_TAKE_DATA = "(L" + STRAND_CLASS + ";)L" + OBJECT + ";";
    public static final String TUPLE_SET_MEMBERS_METHOD = "(L" + LIST + ";L" + TYPE + ";)V";
    public static final String TWO_OBJECTS_ARGS = "(L" + OBJECT + ";L" + OBJECT + ";)V";
    public static final String TYPE_DESC_CONSTRUCTOR = "(L" + TYPE + ";[L" + MAP_VALUE + ";)V";
    public static final String TYPE_DESC_CONSTRUCTOR_WITH_ANNOTATIONS =
                                                             "(L" + TYPE + ";[L" + MAP_VALUE + ";L" + MAP_VALUE + ";)V";
    public static final String TYPE_PARAMETER = "(L" + TYPE + ";)V";
    public static final String UPDATE_CHANNEL_DETAILS = "([L" + CHANNEL_DETAILS + ";)V";
    public static final String VALUE_CLASS_INIT = "(L" + STRAND_CLASS + ";L" + MAP_VALUE + ";)L" + OBJECT + ";";
    public static final String VALUE_OF_DECIMAL = "(D)L" + BIG_DECIMAL + ";";
    public static final String VALUE_OF_JSTRING = "(L" + OBJECT + ";)L" + STRING_VALUE + ";";
    public static final String WAIT_RESULT = STRAND_CLASS + "$WaitResult";
    public static final String XML_ADD_CHILDREN = "(L" + XML_VALUE + ";)V";
    public static final String XML_CHILDREN = "()L" + XML_VALUE + ";";
    public static final String XML_CHILDREN_FROM_STRING = "(L" + STRING_VALUE + ";)L" + XML_VALUE + ";";
    public static final String XML_CONCAT = "(L" + XML_VALUE + ";L" + XML_VALUE + ";)L" + XML_VALUE + ";";
    public static final String XML_GET_ATTRIBUTE = "(L" + B_XML_QNAME + ";)L" + STRING_VALUE + ";";
    public static final String XML_GET_ITEM = "(I)L" + XML_VALUE + ";";
    public static final String XML_SET_ATTRIBUTE = "(L" + B_XML_QNAME + ";L" + B_STRING_VALUE + ";)V";
    public static final String HANDLE_DESCRIPTOR_FOR_STRING_CONCAT = "(Ljava/lang/invoke/MethodHandles$Lookup;" +
            GET_STRING + "Ljava/lang/invoke/MethodType;" + GET_STRING + "[" + GET_OBJECT + ")" +
            "Ljava/lang/invoke/CallSite;";
    public static final String CREATE_REGEXP = "(L" + REG_EXP_DISJUNCTION + ";)L" + REG_EXP_VALUE + ";";
    public static final String CREATE_RE_DISJUNCTION = "(L" + ARRAY_VALUE + ";)L" + REG_EXP_DISJUNCTION + ";";
    public static final String CREATE_RE_SEQUENCE = "(L" + ARRAY_VALUE + ";)L" + REG_EXP_SEQUENCE + ";";
    public static final String CREATE_RE_ASSERTION = "(L" + B_STRING_VALUE + ";)L" + REG_EXP_ASSERTION + ";";
    public static final String CREATE_RE_ATOM_QUANTIFIER = "(L" + OBJECT + ";L" + REG_EXP_QUANTIFIER + ";)L"
            + REG_EXP_ATOM_QUANTIFIER + ";";
    public static final String CREATE_RE_LITERAL_CHAR = "(L" + B_STRING_VALUE + ";)L" + REG_EXP_CHAR_ESCAPE + ";";
    public static final String CREATE_RE_CHAR_CLASS = "(L" + B_STRING_VALUE + ";L" + B_STRING_VALUE + ";L"
                    + REG_EXP_CHAR_SET + ";L" + B_STRING_VALUE + ";)L" + REG_EXP_CHAR_CLASS + ";";
    public static final String CREATE_RE_CHAR_SET = "(L" + ARRAY_VALUE + ";)L" + REG_EXP_CHAR_SET + ";";
    public static final String CREATE_RE_CHAR_SET_RANGE = "(L" + B_STRING_VALUE + ";L" + B_STRING_VALUE + ";L" +
             B_STRING_VALUE + ";)L" + REG_EXP_CHAR_SET_RANGE + ";";
    public static final String CREATE_RE_CAPTURING_GROUP = "(L" + B_STRING_VALUE + ";L" + OBJECT +
            ";L" + REG_EXP_DISJUNCTION + ";L" + B_STRING_VALUE + ";)L" + REG_EXP_CAPTURING_GROUP + ";";
    public static final String CREATE_RE_FLAG_EXPR = "(L" + B_STRING_VALUE + ";L" + REG_EXP_FLAG_ON_OFF +
            ";L" + B_STRING_VALUE + ";)L" + REG_EXP_FLAG_EXPR + ";";
    public static final String CREATE_RE_FLAG_ON_OFF = "(L" + B_STRING_VALUE + ";)L" + REG_EXP_FLAG_ON_OFF + ";";
    public static final String CREATE_RE_QUANTIFIER = "(L" + B_STRING_VALUE + ";L" + B_STRING_VALUE + ";)L"
            + REG_EXP_QUANTIFIER + ";";

    private JvmSignatures() {
    }
}
