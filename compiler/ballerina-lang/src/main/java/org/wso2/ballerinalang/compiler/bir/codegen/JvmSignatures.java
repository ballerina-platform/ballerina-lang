/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_ENV;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BIG_DECIMAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BYTE_TYPE;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LISTENER_REGISTRY_CLASS;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RESOURCE_METHOD_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_ERRORS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SERVICE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_BUILDER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.THROWABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_ID_SET;
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

    public static final String INIT_ERROR_WITH_TYPE = String.format("(L%s;L%s;L%s;L%s;)V", TYPE, B_STRING_VALUE,
            BERROR, OBJECT);
    public static final String INIT_XML_QNAME = String.format("(L%s;L%s;L%s;)V", B_STRING_VALUE, B_STRING_VALUE,
            B_STRING_VALUE);
    public static final String ADD_COLLECTION = String.format("(L%s;)Z", COLLECTION);
    public static final String ADD_SHUTDOWN_HOOK = String.format("(L%s;)V", JAVA_THREAD);
    public static final String ADD_TYPE_ID = String.format("(L%s;L%s;Z)V", MODULE, STRING_VALUE);
    public static final String ADD_VALUE_CREATOR = String.format("(L%s;L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE,
            STRING_VALUE, VALUE_CREATOR);
    public static final String ANNOTATION_GET_STRAND = String.format("(L%s;L%s;)L%s;", FUNCTION_POINTER, STRING_VALUE
            , STRING_VALUE);
    public static final String ANY_TO_BYTE = String.format("(L%s;)I", OBJECT);
    public static final String ANY_TO_DECIMAL = String.format("(L%s;)L%s;", OBJECT, DECIMAL_VALUE);
    public static final String ANY_TO_JBOOLEAN = String.format("(L%s;)Z", OBJECT);
    public static final String ANY_TO_JBYTE = String.format("(L%s;)B", OBJECT);
    public static final String ANY_TO_JCHAR = String.format("(L%s;)C", OBJECT);
    public static final String ANY_TO_JDOUBLE = String.format("(L%s;)D", OBJECT);
    public static final String ANY_TO_JFLOAT = String.format("(L%s;)F", OBJECT);
    public static final String ANY_TO_JLONG = String.format("(L%s;)J", OBJECT);
    public static final String ANY_TO_JSTRING = String.format("(L%s;)S", OBJECT);
    public static final String ARRAY_ADD_BSTRING = String.format("(JL%s;)V", JvmConstants.B_STRING_VALUE);
    public static final String ARRAY_ADD_OBJECT = String.format("(JL%s;)V", OBJECT);
    public static final String BAL_ENV_PARAM = String.format("(L%s;", BAL_ENV);
    public static final String BBB = String.format("(L%s;)L%s;", STRING_VALUE, OBJECT);
    public static final String BOBJECT_CALL = String.format("(L%s;L%s;[L%s;)L%s;", STRAND_CLASS, STRING_VALUE, OBJECT
            , OBJECT);
    public static final String BOBJECT_GET = String.format("(L%s;)L%s;", B_STRING_VALUE, OBJECT);
    public static final String BOOLEAN_TO_STRING = String.format("(Z)L%s;", STRING_VALUE);
    public static final String BOOLEAN_VALUE_OF_METHOD = String.format("(Z)L%s;", BOOLEAN_VALUE);
    public static final String BSTRING_CONCAT = String.format("(L%s;)L%s;", B_STRING_VALUE, B_STRING_VALUE);
    public static final String CAST_B_MAPPING_INITIAL_VALUE_ENTRY = String.format("[L%s;",
            B_MAPPING_INITIAL_VALUE_ENTRY);
    public static final String CHECK_CAST = String.format("(L%s;L%s;)L%s;", OBJECT, TYPE, OBJECT);
    public static final String CHECK_FIELD_UPDATE = String.format("(L%s;L%s;)V", STRING_VALUE, OBJECT);
    public static final String CHECK_IS_TYPE = String.format("(L%s;L%s;)Z", OBJECT, TYPE);
    public static final String CHECKPOINT_CALL = String.format("(L%s;L%s;L%s;JJ)V", BAL_ENV, B_STRING_VALUE,
            B_STRING_VALUE);
    public static final String COLLECTION_OP = String.format("(L%s;)V", COLLECTION);
    public static final String COMPARE_DECIMALS = String.format("(L%s;L%s;)Z", DECIMAL_VALUE, DECIMAL_VALUE);
    public static final String COMPARE_OBJECTS = String.format("(L%s;L%s;)Z", OBJECT, OBJECT);
    public static final String CONTAINS_KEY = String.format("(L%s;L%s;)Z", STRING_VALUE, OBJECT);
    public static final String CREATE_CANCELLED_FUTURE_ERROR = String.format("()L%s;", ERROR_VALUE);
    public static final String CREATE_ERROR = String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE,
            BERROR, OBJECT, BERROR);
    public static final String CREATE_ERROR_FROM_THROWABLE = String.format("(L%s;)L%s;", THROWABLE, ERROR_VALUE);
    public static final String CREATE_OBJECT = String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;", STRING_VALUE, SCHEDULER,
            STRAND_CLASS, MAP, OBJECT, B_OBJECT);
    public static final String CREATE_RECORD = String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE);
    public static final String CREATE_RECORD_WITH_MAP = String.format("(L%s;)L%s<L%s;L%s;>;", STRING_VALUE, MAP_VALUE
            , STRING_VALUE, OBJECT);
    public static final String CREATE_XML_COMMENT = String.format("(L%s;Z)L%s;", B_STRING_VALUE, XML_VALUE);
    public static final String CREATE_XML_ELEMENT = String.format("(L%s;L%s;Z)L%s;", B_XML_QNAME, B_STRING_VALUE,
            XML_VALUE);
    public static final String CREATE_XML_PI = String.format("(L%s;L%s;Z)L%s;", B_STRING_VALUE, B_STRING_VALUE,
            XML_VALUE);
    public static final String CREATE_XML_TEXT = String.format("(L%s;)L%s;", B_STRING_VALUE, XML_VALUE);
    public static final String CRETAE_XML_SEQUENCE = String.format("()L%s;", XML_SEQUENCE);
    public static final String DECIMAL_NEGATE = String.format("()L%s;", DECIMAL_VALUE);
    public static final String DECIMAL_TO_HANDLE = String.format("(L%s;)L%s;", OBJECT, HANDLE_VALUE);
    public static final String DECIMAL_VALUE_OF_BOOLEAN = String.format("(B)L%s;", DECIMAL_VALUE);
    public static final String DECIMAL_VALUE_OF_CHAR = String.format("(C)L%s;", DECIMAL_VALUE);
    public static final String DECIMAL_VALUE_OF_DOUBLE = String.format("(D)L%s;", DECIMAL_VALUE);
    public static final String DECIMAL_VALUE_OF_FLOAT = String.format("(F)L%s;", DECIMAL_VALUE);
    public static final String DECIMAL_VALUE_OF_INT = String.format("(I)L%s;", DECIMAL_VALUE);
    public static final String DECIMAL_VALUE_OF_LONG = String.format("(J)L%s;", DECIMAL_VALUE);
    public static final String DECIMAL_VALUE_OF_SHORT = String.format("(S)L%s;", DECIMAL_VALUE);
    public static final String DOUBLE_TO_STRING = String.format("(D)L%s;", STRING_VALUE);
    public static final String DOUBLE_VALUE_OF_METHOD = String.format("(D)L%s;", DOUBLE_VALUE);
    public static final String ERROR_CALL = String.format("(L%s;L%s;)V", BAL_ENV, ERROR_VALUE);
    public static final String ERROR_INIT = String.format("(L%s;L%s;L%s;L%s;)V", TYPE, B_STRING_VALUE, BERROR, OBJECT);
    public static final String FP_INIT = String.format("(L%s;L%s;L%s;Z)V", FUNCTION, TYPE, STRING_VALUE);
    public static final String FROM_STRING = String.format("(L%s;)L%s;", STRING_VALUE, B_STRING_VALUE);
    public static final String GET_ANNOTATION_VALUE = String.format("(L%s;L%s;)L%s;", TYPEDESC_VALUE, B_STRING_VALUE,
            OBJECT);
    public static final String GET_ANON_TYPE = String.format("(IL%s;)L%s;", STRING_VALUE, TYPE);
    public static final String GET_ARRAY_VALUE = String.format("L%s;", ARRAY_VALUE);
    public static final String GET_ATTRAIBUTE_MAP = String.format("()L%s;", MAP_VALUE);
    public static final String GET_BDECIMAL = String.format("L%s;", DECIMAL_VALUE);
    public static final String GET_BERROR = String.format("L%s;", BERROR);
    public static final String GET_BOBJECT = String.format("L%s;", B_OBJECT);
    public static final String GET_BSTRING = String.format("L%s;", B_STRING_VALUE);
    public static final String GET_BSTRING_FOR_ARRAY_INDEX = String.format("(J)L%s;", JvmConstants.B_STRING_VALUE);
    public static final String GET_ERROR_TYPE = String.format("L%s;", ERROR_TYPE);
    public static final String GET_ERROR_VALUE = String.format("L%s;", ERROR_VALUE);
    public static final String GET_FUNCTION = String.format("()L%s;", FUNCTION);
    public static final String GET_FUNCTION_POINTER = String.format("L%s;", FUNCTION_POINTER);
    public static final String GET_FUTURE_VALUE = String.format("L%s;", FUTURE_VALUE);
    public static final String GET_HANDLE_VALUE = String.format("L%s;", HANDLE_VALUE);
    public static final String GET_JSTRING = String.format("()L%s;", STRING_VALUE);
    public static final String GET_LISTENER_REGISTRY = String.format("L%s;", JvmConstants.LISTENER_REGISTRY_CLASS);
    public static final String GET_LISTENER_REGISTRY_CLASS = String.format("()L%s;", LISTENER_REGISTRY_CLASS);
    public static final String GET_LOCK_FROM_MAP = String.format("(L%s;)L%s;", STRING_VALUE, LOCK_VALUE);
    public static final String GET_LOCK_MAP = String.format("(L%s;)L%s;", STRING_VALUE, LOCK_VALUE);
    public static final String GET_MAIN_ARGS = String.format("()[L%s;", OBJECT);
    public static final String GET_MAP_ARRAY = String.format("[L%s;", MAP_VALUE);
    public static final String GET_MAP_VALUE = String.format("L%s;", MAP_VALUE);
    public static final String GET_MODULE = String.format("L%s;", MODULE);
    public static final String GET_OBJECT = String.format("L%s;", OBJECT);
    public static final String GET_OBJECT_FOR_STRING = String.format("(L%s;)L%s;", STRING_VALUE, OBJECT);
    public static final String GET_RUNTIME = String.format("()L%s;", JvmConstants.JAVA_RUNTIME);
    public static final String GET_RUNTIME_ERROR = String.format("L%s;", RUNTIME_ERRORS);
    public static final String GET_RUNTIME_EXCEPTION = String.format("(L%s;L%s;[L%s;)L%s;", STRING_VALUE,
            RUNTIME_ERRORS, OBJECT, ERROR_VALUE);
    public static final String GET_SCHEDULER = String.format("L%s;", SCHEDULER);
    public static final String GET_STRAND = String.format("L%s;", STRAND_CLASS);
    public static final String GET_STRAND_METADATA = String.format("L%s;", STRAND_METADATA);
    public static final String GET_STREAM_VALUE = String.format("L%s;", STREAM_VALUE);
    public static final String GET_STRING = String.format("L%s;", STRING_VALUE);
    public static final String GET_STRING_AT = String.format("(L%s;J)L%s;", B_STRING_VALUE, B_STRING_VALUE);
    public static final String GET_STRING_FROM_ARRAY = String.format("(J)L%s;", OBJECT);
    public static final String GET_TABLE_VALUE_IMPL = String.format("L%s;", TABLE_VALUE_IMPL);
    public static final String GET_THROWABLE = String.format("L%s;", THROWABLE);
    public static final String GET_TUPLE_TYPE_IMPL = String.format("L%s;", TUPLE_TYPE_IMPL);
    public static final String GET_TYPE = String.format("L%s;", TYPE);
    public static final String GET_TYPEDESC = String.format("L%s;", TYPEDESC_VALUE);
    public static final String GET_TYPEDESC_OF_OBJECT = String.format("(L%s;)L%s;", OBJECT, TYPEDESC_VALUE);
    public static final String GET_UNION_TYPE_IMPL = String.format("L%s;", UNION_TYPE_IMPL);
    public static final String GET_WD_CHANNELS = String.format("L%s;", WD_CHANNELS);
    public static final String GET_WORKER_DATA_CHANNEL = String.format("(L%s;)L%s;", STRING_VALUE, WORKER_DATA_CHANNEL);
    public static final String GET_XML = String.format("L%s;", XML_VALUE);
    public static final String HANDLE_CHANNEL_ERROR = String.format("([L%s;L%s;)V", CHANNEL_DETAILS, ERROR_VALUE);
    public static final String HANDLE_ERROR_RETURN = String.format("(L%s;)V", OBJECT);
    public static final String HANDLE_FLUSH = String.format("([L%s;)L%s;", CHANNEL_DETAILS, ERROR_VALUE);
    public static final String HANDLE_MAP_STORE = String.format("(L%s;L%s;L%s;)V", MAP_VALUE, B_STRING_VALUE, OBJECT);
    public static final String HANDLE_STOP_PANIC = String.format("(L%s;)V", THROWABLE);
    public static final String HANDLE_TABLE_STORE = String.format("(L%s;L%s;L%s;)V", TABLE_VALUE, OBJECT, OBJECT);
    public static final String HANDLE_THROWABLE = String.format("(L%s;)V", JvmConstants.THROWABLE);
    public static final String HANDLE_WAIT_ANY = String.format("(L%s;)L%s$WaitResult;", LIST, STRAND_CLASS);
    public static final String HANDLE_WAIT_MULTIPLE = String.format("(L%s;L%s;)V", MAP, MAP_VALUE);
    public static final String HANDLE_WORKER_ERROR = String.format("(L%s;L%s;[L%s;)V", REF_VALUE, STRAND_CLASS,
            CHANNEL_DETAILS);
    public static final String INIT_ARRAY = String.format("(L%s;J[L%s;)V", ARRAY_TYPE, B_LIST_INITIAL_VALUE_ENTRY);
    public static final String INIT_ARRAY_TYPE_IMPL = String.format("(L%s;IZ)V", TYPE);
    public static final String INIT_ARRAY_WITH_INITIAL_VALUES = String.format("(L%s;J[L%s;L%s;)V", ARRAY_TYPE,
            B_LIST_INITIAL_VALUE_ENTRY, TYPEDESC_VALUE);
    public static final String INIT_BAL_ENV = String.format("(L%s;L%s;)V", STRAND_CLASS, MODULE);
    public static final String INIT_CHANNEL_DETAILS = String.format("(L%s;ZZ)V", STRING_VALUE);
    public static final String INIT_CLI_SPEC = String.format("(L%s;[L%s;[L%s;)V", OPTION, OPERAND, STRING_VALUE);
    public static final String INIT_CONFIG = String.format("([L%s;[L%s;L%s;)V", STRING_VALUE, PATH, STRING_VALUE);
    public static final String INIT_CONFIGURABLES = String.format("(L%s;L%s;[L%s;[L%s;L%s;)V", MODULE, MAP,
            STRING_VALUE, PATH, STRING_VALUE);
    public static final String INIT_DECIMAL = String.format("(L%s;)V", BIG_DECIMAL);
    public static final String INIT_ERROR = String.format("(L%s;)V", B_STRING_VALUE);
    public static final String INIT_ERROR_TYPE_IMPL = String.format("(L%s;L%s;)V", STRING_VALUE, MODULE);
    public static final String INIT_FIELD_IMPL = String.format("(L%s;L%s;J)V", TYPE, STRING_VALUE);
    public static final String INIT_FINITE_TYPE_IMPL = String.format("(L%s;L%s;I)V", STRING_VALUE, SET);
    public static final String INIT_FUCNTION_PARAM = String.format("(L%s;L%s;L%s;)V", STRING_VALUE, BOOLEAN_VALUE,
            TYPE);
    public static final String INIT_FUNCTION_TYPE_IMPL = String.format("([L%s;L%s;L%s;J)V", FUNCTION_PARAMETER, TYPE,
            TYPE);
    public static final String INIT_INTERSECTION_TYPE_WITH_REFERENCE_TYPE = String.format("(L%s;[L%s;L%s;IZ)V",
            MODULE, TYPE, INTERSECTABLE_REFERENCE_TYPE);
    public static final String INIT_INTERSECTION_TYPE_WITH_TYPE = String.format("(L%s;[L%s;L%s;IZ)V", MODULE, TYPE,
            TYPE);
    public static final String INIT_LIST_INITIAL_EXPRESSION_ENTRY = String.format("(L%s;)V", OBJECT);
    public static final String INIT_LISTENER_REGISTRY = String.format("(L%s;)V", JvmConstants.LISTENER_REGISTRY_CLASS);
    public static final String INIT_MAPPING_INITIAL_SPREAD_FIELD_ENTRY = String.format("(L%s;)V", B_MAP);
    public static final String INIT_MODULE = String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE);
    public static final String INIT_NON_BMP_STRING_VALUE = String.format("(L%s;[I)V", STRING_VALUE);
    public static final String INIT_OBJECT = String.format("(L%s;L%s;J)V", STRING_VALUE, MODULE);
    public static final String INIT_OPERAND = String.format("(ZL%s;L%s;)V", STRING_VALUE, TYPE);
    public static final String INIT_OPTION = String.format("(L%s;I)V", TYPE);
    public static final String INIT_PARAMETERIZED_TYPE_IMPL = String.format("(L%s;I)V", TYPE);
    public static final String INIT_STRAND = String.format("(L%s;L%s;L%s;L%s;L%s;)V", STRING_VALUE, STRAND_METADATA,
            SCHEDULER, STRAND_CLASS, MAP);
    public static final String INIT_STRAND_METADATA = String.format("(L%s;L%s;L%s;L%s;L%s;)V", STRING_VALUE,
            STRING_VALUE, STRING_VALUE, STRING_VALUE, STRING_VALUE);
    public static final String INIT_STREAM_TYPE_IMPL = String.format("(L%s;L%s;)V", TYPE, TYPE);
    public static final String INIT_TABLE_TYPE_IMPL = String.format("(L%s;L%s;Z)V", TYPE, TYPE);
    public static final String INIT_TABLE_TYPE_WITH_FIELD_NAME_LIST = String.format("(L%s;[L%s;Z)V", TYPE,
            STRING_VALUE);
    public static final String INIT_TABLE_VALUE_IMPL = String.format("(L%s;L%s;L%s;)V", TABLE_TYPE, ARRAY_VALUE,
            ARRAY_VALUE);
    public static final String INIT_TUPLE = String.format("(L%s;J[L%s;)V", TUPLE_TYPE, B_LIST_INITIAL_VALUE_ENTRY);
    public static final String INIT_TUPLE_TYPE_IMPL = String.format("(L%s;L%s;IZZ)V", STRING_VALUE, MODULE);
    public static final String INIT_TYPEDESC = String.format("(L%s;)V", TYPEDESC_VALUE);
    public static final String INIT_UNION_TYPE_IMPL = String.format("(L%s;L%s;IZJ)V", STRING_VALUE, MODULE);
    public static final String INIT_WITH_BOOLEAN = String.format("(L%s;Z)V", TYPE);
    public static final String INIT_WITH_STRING = String.format("(L%s;)V", STRING_VALUE);
    public static final String INITIAL_METHOD_DESC = String.format("(L%s;", STRAND_CLASS);
    public static final String INSTANTIATE = String.format("(L%s;[L%s;)L%s;", STRAND_CLASS, B_INITIAL_VALUE_ENTRY,
            OBJECT);
    public static final String INT_VALUE_OF_METHOD = String.format("(I)L%s;", INT_VALUE);
    public static final String INTI_VARIABLE_KEY = String.format("(L%s;L%s;L%s;L%s;Z)V", MODULE, STRING_VALUE, TYPE,
            STRING_VALUE);
    public static final String IS_CONCURRENT = String.format("(L%s;)Z", FUNCTION_POINTER);
    public static final String JSON_GET_ELEMENT = String.format("(L%s;L%s;)L%s;", OBJECT, B_STRING_VALUE, OBJECT);
    public static final String JSON_SET_ELEMENT = String.format("(L%s;L%s;L%s;)V", OBJECT, STRING_VALUE, OBJECT);
    public static final String LAMBDA_MAIN = String.format("([L%s;)L%s;", OBJECT, OBJECT);
    public static final String LAMBDA_STOP_DYNAMIC = String.format("([L%s;)L%s;", OBJECT, OBJECT);
    public static final String LINKED_HASH_SET_OP = String.format("(L%s;)V", LINKED_HASH_SET);
    public static final String LOAD_ANY_TYPE = String.format("L%s;", ANY_TYPE);
    public static final String LOAD_ANYDATA_TYPE = String.format("L%s;", ANYDATA_TYPE);
    public static final String LOAD_BOOLEAN_TYPE = String.format("L%s;", BOOLEAN_TYPE);
    public static final String LOAD_BYTE_TYPE = String.format("L%s;", BYTE_TYPE);
    public static final String LOAD_DECIMAL_TYPE = String.format("L%s;", DECIMAL_TYPE);
    public static final String LOAD_FLOAT_TYPE = String.format("L%s;", FLOAT_TYPE);
    public static final String LOAD_HANDLE_TYPE = String.format("L%s;", HANDLE_TYPE);
    public static final String LOAD_INTEGER_TYPE = String.format("L%s;", INTEGER_TYPE);
    public static final String LOAD_JSON_TYPE = String.format("L%s;", JSON_TYPE);
    public static final String LOAD_NEVER_TYPE = String.format("L%s;", NEVER_TYPE);
    public static final String LOAD_NULL_TYPE = String.format("L%s;", NULL_TYPE);
    public static final String LOAD_OBJECT_TYPE = String.format("L%s;", OBJECT_TYPE);
    public static final String LOAD_READONLY_TYPE = String.format("L%s;", READONLY_TYPE);
    public static final String LOAD_SERVICE_TYPE = String.format("L%s;", SERVICE_TYPE);
    public static final String LOAD_STRING_TYPE = String.format("L%s;", STRING_TYPE);
    public static final String LOAD_TYPE = String.format("L%s;", TYPE);
    public static final String LOAD_UNION_TYPE = String.format("L%s;", UNION_TYPE);
    public static final String LOAD_XML_TYPE = String.format("L%s;", XML_TYPE);
    public static final String LOCK = String.format("(L%s;)Z", STRAND_CLASS);
    public static final String LONG_STREAM_RANGE_CLOSED = String.format("(JJ)L%s;", LONG_STREAM);
    public static final String LONG_TO_STRING = String.format("(J)L%s;", STRING_VALUE);
    public static final String LONG_VALUE_OF = String.format("(J)L%s;", LONG_VALUE);
    public static final String MAP_PUT = String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT);
    public static final String MAP_VALUES = String.format("()L%s;", COLLECTION);
    public static final String MAP_VALUES_WITH_COLLECTION = String.format("()L%s<TV;>;", COLLECTION);
    public static final String METHOD_STRING_PARAM = String.format("(L%s;)V", JvmConstants.STRING_VALUE);
    public static final String METHOD_TYPE_IMPL_ARRAY_PARAM = String.format("([L%s;)V", METHOD_TYPE_IMPL);
    public static final String METHOD_TYPE_IMPL_INIT = String.format("(L%s;L%s;L%s;J)V", STRING_VALUE,
            OBJECT_TYPE_IMPL, FUNCTION_TYPE_IMPL);
    public static final String METHOD_TYPE_IMPL_PARAM = String.format("(L%s;)V", METHOD_TYPE_IMPL);
    public static final String MODULE_START = String.format("(L%s;)L%s;", STRAND_CLASS, OBJECT);
    public static final String OBJECT_SET = String.format("(L%s;L%s;L%s;)V", STRING_VALUE, B_STRING_VALUE, OBJECT);
    public static final String OBJECT_TYPE_DUPLICATE = String.format("()L%s;", OBJECT_TYPE_IMPL);
    public static final String OBJECT_TYPE_IMPL_INIT = String.format("(L%s;)V", OBJECT_TYPE_IMPL);
    public static final String PANIC_IF_UNLOCK = String.format("(L%s;L%s;)V", STRING_VALUE, STRAND_CLASS);
    public static final String PASS_BSTRING_RETURN_OBJECT = String.format("(L%s;)L%s;", B_STRING_VALUE, OBJECT);
    public static final String PASS_OBJECT_RETURN_OBJECT = String.format("(L%s;)L%s;", OBJECT, OBJECT);
    public static final String PASS_OBJECT_RETURN_SAME_TYPE = String.format("(L%s;)TV;", OBJECT);
    public static final String POPULATE_ATTACHED_FUNCTION = String.format("([L%s;)V", METHOD_TYPE_IMPL);
    public static final String POPULATE_CONFIG_DATA = String.format("()[L%s;", VARIABLE_KEY);
    public static final String POPULATE_INITIAL_VALUES = String.format("([L%s;)V", B_MAPPING_INITIAL_VALUE_ENTRY);
    public static final String PROCESS_ANNOTATIONS = String.format("(L%s;L%s;)V", MAP_VALUE, TYPE);
    public static final String PROCESS_FP_ANNOTATIONS = String.format("(L%s;L%s;L%s;)V", FUNCTION_POINTER, MAP_VALUE,
            STRING_VALUE);
    public static final String PROCESS_OBJ_CTR_ANNOTATIONS = String.format("(L%s;L%s;L%s;)V", OBJECT_TYPE_IMPL,
            MAP_VALUE, STRAND_CLASS);
    public static final String PUT_FRAMES = String.format("[L%s;", OBJECT);
    public static final String RECORD_GET = String.format("(L%s;L%s;)L%s;", STRING_VALUE, OBJECT, OBJECT);
    public static final String RECORD_GET_KEYS = String.format("()[L%s;", OBJECT);
    public static final String RECORD_INIT = String.format("(L%s;)V", TYPE);
    public static final String RECORD_INIT_WRAPPER = String.format("(L%s;L%s;)V", STRAND_CLASS, MAP_VALUE);
    public static final String RECORD_PUT = String.format("(L%s;L%s;L%s;)L%s;", STRING_VALUE, OBJECT, OBJECT, OBJECT);
    public static final String RECORD_REMOVE = String.format("(L%s;L%s;)L%s;", STRING_VALUE, OBJECT, OBJECT);
    public static final String RECORD_SET = String.format("()L%s;", SET);
    public static final String RECORD_SET_MAP_ENTRY = String.format("()L%s<L%s<TK;TV;>;>;", SET, MAP_ENTRY);
    public static final String RECORD_TYPE_IMPL_INIT = String.format("(L%s;L%s;JZI)V", STRING_VALUE, MODULE);
    public static final String RECORD_VALUE_CLASS = String.format("<K:L%s;V:L%s;>L%s<TK;TV;>;L%s<TK;TV;>;", OBJECT,
            OBJECT, MAP_VALUE_IMPL, MAP_VALUE);
    public static final String RESOURCE_METHOD_TYPE_ARRAY_PARAM = String.format("([L%s;)V", RESOURCE_METHOD_TYPE);
    public static final String RESOURCE_METHOD_TYPE_IMPL_INIT = String.format("(L%s;L%s;L%s;JL%s;[L%s;)V",
            STRING_VALUE, OBJECT_TYPE_IMPL, FUNCTION_TYPE_IMPL, STRING_VALUE, STRING_VALUE);
    public static final String RETURN_ARRAY_VALUE = String.format(")L%s;", ARRAY_VALUE);
    public static final String RETURN_B_OBJECT = String.format(")L%s;", B_OBJECT);
    public static final String RETURN_B_STRING_VALUE = String.format(")L%s;", B_STRING_VALUE);
    public static final String RETURN_DECIMAL_VALUE = String.format(")L%s;", DECIMAL_VALUE);
    public static final String RETURN_ERROR_VALUE = String.format(")L%s;", ERROR_VALUE);
    public static final String RETURN_FUNCTION_POINTER = String.format(")L%s;", FUNCTION_POINTER);
    public static final String RETURN_FUTURE_VALUE = String.format(")L%s;", FUTURE_VALUE);
    public static final String RETURN_HANDLE_VALUE = String.format(")L%s;", HANDLE_VALUE);
    public static final String RETURN_JOBJECT = String.format(")L%s;", OBJECT);
    public static final String RETURN_MAP_VALUE = String.format(")L%s;", MAP_VALUE);
    public static final String RETURN_OBJECT = String.format("()L%s;", OBJECT);
    public static final String RETURN_STREAM_VALUE = String.format(")L%s;", STREAM_VALUE);
    public static final String RETURN_TABLE_VALUE_IMPL = String.format(")L%s;", TABLE_VALUE_IMPL);
    public static final String RETURN_TYPEDESC_VALUE = String.format(")L%s;", TYPEDESC_VALUE);
    public static final String RETURN_XML_VALUE = String.format(")L%s;", XML_VALUE);
    public static final String SCHEDULE_LOCAL = String.format("([L%s;L%s;L%s;L%s;L%s;L%s;)L%s;", OBJECT,
            B_FUNCTION_POINTER, STRAND_CLASS, TYPE, STRING_VALUE, STRAND_METADATA, FUTURE_VALUE);
    public static final String SCHEDULE_TRANSACTIONAL_FUNCTION = String.format("([L%s;L%s;L%s;L%s;L%s;L%s;)L%s;",
            OBJECT, B_FUNCTION_POINTER, STRAND_CLASS, TYPE, STRING_VALUE, STRAND_METADATA, FUTURE_VALUE);
    public static final String SEND_DATA = String.format("(L%s;L%s;)V", OBJECT, STRAND_CLASS);
    public static final String SET_DECIMAL_RETURN_DECIMAL = String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE);
    public static final String SET_IMMUTABLE_TYPE = String.format("(L%s;)V", INTERSECTION_TYPE);
    public static final String SET_LINKED_HASH_MAP = String.format("(L%s;)V", LINKED_HASH_MAP);
    public static final String SET_MAP = String.format("(L%s;)V", MAP);
    public static final String SET_METHODS = String.format("([L%s;)V", METHOD_TYPE);
    public static final String SET_ON_INIT = String.format("(L%s;L%s;)V", B_STRING_VALUE, OBJECT);
    public static final String SET_RESOURCE_METHOD_TYPE_ARRAY = String.format("([L%s;)V", RESOURCE_METHOD_TYPE);
    public static final String SET_STRAND = String.format("(L%s;)V", STRAND_CLASS);
    public static final String SET_TYPE_ARRAY = String.format("([L%s;)V", TYPE);
    public static final String SET_TYPE_ID_SET = String.format("(L%s;)V", TYPE_ID_SET);
    public static final String SET_VALUE = String.format("(L%s;L%s;)V", B_STRING_VALUE, OBJECT);
    public static final String START_CALLABLE_OBSERVATION = String.format("(L%s;L%s;L%s;JJL%s;L%s;ZZZ)V", BAL_ENV,
            B_STRING_VALUE, B_STRING_VALUE, B_OBJECT, B_STRING_VALUE);
    public static final String START_RESOURCE_OBSERVATION = String.format("(L%s;L%s;L%s;JJL%s;L%s;L%s;ZZ)V", BAL_ENV,
            B_STRING_VALUE, B_STRING_VALUE, B_STRING_VALUE, B_STRING_VALUE, B_STRING_VALUE);
    public static final String STOP_OBSERVATION = String.format("(L%s;)V", BAL_ENV);
    public static final String STRING_BUILDER_APPEND = String.format("(L%s;)L%s;", STRING_VALUE, STRING_BUILDER);
    public static final String SYNC_SEND_DATA = String.format("(L%s;L%s;)L%s;", OBJECT, STRAND_CLASS, OBJECT);
    public static final String TO_ARRAY = String.format("([L%s;)[L%s;", OBJECT, OBJECT);
    public static final String TO_CHAR = String.format("(L%s;)L%s;", OBJECT, B_STRING_VALUE);
    public static final String TRY_TAKE_DATA = String.format("(L%s;)L%s;", STRAND_CLASS, OBJECT);
    public static final String TUPLE_SET_MEMBERS_METHOD = String.format("(L%s;L%s;)V", LIST, TYPE);
    public static final String TWO_OBJECTS_ARGS = String.format("(L%s;L%s;)V", OBJECT, OBJECT);
    public static final String TYPE_DESC_CONSTRUCTOR = String.format("(L%s;[L%s;)V", TYPE, MAP_VALUE);
    public static final String UPDATE_CHANNEL_DETAILS = String.format("([L%s;)V", CHANNEL_DETAILS);
    public static final String VALUE_CLASS_INIT = String.format("(L%s;L%s;)L%s;", STRAND_CLASS, MAP_VALUE, OBJECT);
    public static final String VALUE_OF_DECIMAL = String.format("(D)L%s;", BIG_DECIMAL);
    public static final String VALUE_OF_JSTRING = String.format("(L%s;)L%s;", OBJECT, STRING_VALUE);
    public static final String WAIT_RESULT = String.format("%s$WaitResult", STRAND_CLASS);
    public static final String XML_ADD_CHILDREN = String.format("(L%s;)V", XML_VALUE);
    public static final String XML_CHILDREN = String.format("()L%s;", XML_VALUE);
    public static final String XML_CHILDREN_FROM_STRING = String.format("(L%s;)L%s;", STRING_VALUE, XML_VALUE);
    public static final String XML_CONCAT = String.format("(L%s;L%s;)L%s;", XML_VALUE, XML_VALUE, XML_VALUE);
    public static final String XML_GET_ATTRIBUTE = String.format("(L%s;)L%s;", B_XML_QNAME, STRING_VALUE);
    public static final String XML_GET_ITEM = String.format("(I)L%s;", XML_VALUE);
    public static final String XML_SET_ATTRIBUTE = String.format("(L%s;L%s;)V", B_XML_QNAME, B_STRING_VALUE);

    private JvmSignatures() {
    }
}
