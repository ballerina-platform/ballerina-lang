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

/**
 * JVM bytecode generation related constants.
 *
 * @since 1.2.0
 */
public class JvmConstants {

    // jvm values public API classes
    public static final String BXML_QNAME = "org/ballerinalang/jvm/values/api/BXMLQName";

    // jvm runtime values related classes
    public static final String MAP_VALUE = "org/ballerinalang/jvm/values/MapValue";
    public static final String MAP_VALUE_IMPL = "org/ballerinalang/jvm/values/MapValueImpl";
    public static final String STREAM_VALUE = "org/ballerinalang/jvm/values/StreamValue";
    public static final String TABLE_VALUE = "org/ballerinalang/jvm/values/TableValue";
    public static final String ARRAY_VALUE = "org/ballerinalang/jvm/values/ArrayValue";
    public static final String OBJECT_VALUE = "org/ballerinalang/jvm/values/ObjectValue";
    public static final String ABSTRACT_OBJECT_VALUE = "org/ballerinalang/jvm/values/AbstractObjectValue";
    public static final String REF_VALUE = "org/ballerinalang/jvm/values/RefValue";
    public static final String ERROR_VALUE = "org/ballerinalang/jvm/values/ErrorValue";
    public static final String STRING_VALUE = "java/lang/String";
    public static final String BMP_STRING_VALUE = "org/ballerinalang/jvm/values/BmpStringValue";
    public static final String LONG_VALUE = "java/lang/Long";
    public static final String BYTE_VALUE = "java/lang/Byte";
    public static final String SHORT_VALUE = "java/lang/Short";
    public static final String BOOLEAN_VALUE = "java/lang/Boolean";
    public static final String DOUBLE_VALUE = "java/lang/Double";
    public static final String DECIMAL_VALUE = "org/ballerinalang/jvm/values/DecimalValue";
    public static final String INT_VALUE = "java/lang/Integer";
    public static final String XML_VALUE = "org/ballerinalang/jvm/values/XMLValue";
    public static final String XML_QNAME = "org/ballerinalang/jvm/values/XMLQName";
    public static final String FUTURE_VALUE = "org/ballerinalang/jvm/values/FutureValue";
    public static final String TYPEDESC_VALUE_IMPL = "org/ballerinalang/jvm/values/TypedescValueImpl";
    public static final String TYPEDESC_VALUE_IMPL_CLOSURES = "closures";
    public static final String TYPEDESC_VALUE = "org/ballerinalang/jvm/values/TypedescValue";
    public static final String HANDLE_VALUE = "org/ballerinalang/jvm/values/HandleValue";
    public static final String LOCK_VALUE = "org/ballerinalang/jvm/BLock";
    public static final String LOCK_STORE = "org/ballerinalang/jvm/BLockStore";
    public static final String FUNCTION_POINTER = "org/ballerinalang/jvm/values/FPValue";
    public static final String ARRAY_VALUE_IMPL = "org/ballerinalang/jvm/values/ArrayValueImpl";
    public static final String TUPLE_VALUE_IMPL = "org/ballerinalang/jvm/values/TupleValueImpl";
    public static final String TABLE_VALUE_IMPL = "org/ballerinalang/jvm/values/TableValueImpl";
    public static final String SIMPLE_VALUE = "org/ballerinalang/jvm/values/SimpleValue";

    public static final String BHANDLE = "org/ballerinalang/jvm/values/api/BHandle";

    public static final String BINITIAL_VALUE_ENTRY = "org/ballerinalang/jvm/values/api/BInitialValueEntry";
    public static final String MAPPING_INITIAL_VALUE_ENTRY = "org/ballerinalang/jvm/values/MappingInitialValueEntry";
    public static final String MAPPING_INITIAL_KEY_VALUE_ENTRY =
            "org/ballerinalang/jvm/values/MappingInitialValueEntry$KeyValueEntry";
    public static final String MAPPING_INITIAL_SPREAD_FIELD_ENTRY =
            "org/ballerinalang/jvm/values/MappingInitialValueEntry$SpreadFieldEntry";
    public static final String LIST_INITIAL_VALUE_ENTRY = "org/ballerinalang/jvm/values/ListInitialValueEntry";
    public static final String LIST_INITIAL_EXPRESSION_ENTRY =
            "org/ballerinalang/jvm/values/ListInitialValueEntry$ExpressionEntry";

    // types related classes
    public static final String BTYPE = "org/ballerinalang/jvm/types/BType";
    public static final String BTYPES = "org/ballerinalang/jvm/types/BTypes";
    public static final String ARRAY_TYPE = "org/ballerinalang/jvm/types/BArrayType";
    public static final String MAP_TYPE = "org/ballerinalang/jvm/types/BMapType";
    public static final String XML_TYPE = "org/ballerinalang/jvm/types/BXMLType";
    public static final String STREAM_TYPE = "org/ballerinalang/jvm/types/BStreamType";
    public static final String TABLE_TYPE = "org/ballerinalang/jvm/types/BTableType";
    public static final String UNION_TYPE = "org/ballerinalang/jvm/types/BUnionType";
    public static final String INTERSECTION_TYPE = "org/ballerinalang/jvm/types/BIntersectionType";
    public static final String RECORD_TYPE = "org/ballerinalang/jvm/types/BRecordType";
    public static final String OBJECT_TYPE = "org/ballerinalang/jvm/types/BObjectType";
    public static final String SERVICE_TYPE = "org/ballerinalang/jvm/types/BServiceType";
    public static final String ERROR_TYPE = "org/ballerinalang/jvm/types/BErrorType";
    public static final String TUPLE_TYPE = "org/ballerinalang/jvm/types/BTupleType";
    public static final String FUNCTION_TYPE = "org/ballerinalang/jvm/types/BFunctionType";
    public static final String TYPEDESC_TYPE = "org/ballerinalang/jvm/types/BTypedescType";
    public static final String BFIELD = "org/ballerinalang/jvm/types/BField";
    public static final String ATTACHED_FUNCTION = "org/ballerinalang/jvm/types/AttachedFunction";
    public static final String FINITE_TYPE = "org/ballerinalang/jvm/types/BFiniteType";
    public static final String FUTURE_TYPE = "org/ballerinalang/jvm/types/BFutureType";
    public static final String PACKAGE_TYPE = "org/ballerinalang/jvm/types/BPackage";
    public static final String TYPE_ID_SET = "org/ballerinalang/jvm/types/BTypeIdSet";
    public static final String TYPE_ID = "org/ballerinalang/jvm/types/BTypeIdSet$TypeId";

    // other jvm-specific classes
    public static final String TYPE_CHECKER = "org/ballerinalang/jvm/TypeChecker";
    public static final String SCHEDULER = "org/ballerinalang/jvm/scheduling/Scheduler";
    public static final String JSON_UTILS = "org/ballerinalang/jvm/JSONUtils";
    public static final String STRAND = "org/ballerinalang/jvm/scheduling/Strand";
    public static final String TYPE_CONVERTER = "org/ballerinalang/jvm/TypeConverter";
    public static final String LIST_UTILS = "org/ballerinalang/jvm/Lists";
    public static final String STRAND_STATE = "org/ballerinalang/jvm/scheduling/State";
    public static final String VALUE_CREATOR = "org/ballerinalang/jvm/values/ValueCreator";
    public static final String XML_FACTORY = "org/ballerinalang/jvm/XMLFactory";
    public static final String WD_CHANNELS = "org/ballerinalang/jvm/scheduling/WDChannels";
    public static final String WORKER_DATA_CHANNEL = "org/ballerinalang/jvm/scheduling/WorkerDataChannel";
    public static final String CHANNEL_DETAILS = "org/ballerinalang/jvm/values/ChannelDetails";
    public static final String WORKER_UTILS = "org/ballerinalang/jvm/scheduling/WorkerUtils";
    public static final String MAP_UTILS = "org/ballerinalang/jvm/MapUtils";
    public static final String TABLE_UTILS = "org/ballerinalang/jvm/TableUtils";
    public static final String STRING_UTILS = "org/ballerinalang/jvm/StringUtils";
    public static final String BAL_ERRORS = "org/ballerinalang/jvm/BallerinaErrors";
    public static final String RUNTIME_UTILS = "org/ballerinalang/jvm/util/RuntimeUtils";
    public static final String ARGUMENT_PARSER = "org/ballerinalang/jvm/util/ArgumentParser";
    public static final String LAUNCH_UTILS = "org/ballerinalang/jvm/launch/LaunchUtils";
    public static final String MATH_UTILS = "org/ballerinalang/jvm/MathUtils";
    public static final String BAL_ERROR_REASONS = "org/ballerinalang/jvm/util/exceptions/BallerinaErrorReasons";
    public static final String RUNTIME_ERRORS = "org/ballerinalang/jvm/util/exceptions/RuntimeErrors";
    public static final String BLANG_EXCEPTION_HELPER = "org/ballerinalang/jvm/util/exceptions/BLangExceptionHelper";
    public static final String COMPATIBILITY_CHECKER = "org/ballerinalang/jvm/util/CompatibilityChecker";

    // other java classes
    public static final String OBJECT = "java/lang/Object";
    public static final String MATH = "java/lang/Math";
    public static final String MAP = "java/util/Map";
    public static final String LINKED_HASH_MAP = "java/util/LinkedHashMap";
    public static final String ARRAY_LIST = "java/util/ArrayList";
    public static final String LIST = "java/util/List";
    public static final String SET = "java/util/Set";
    public static final String LINKED_HASH_SET = "java/util/LinkedHashSet";
    public static final String STRING_BUILDER = "java/lang/StringBuilder";
    public static final String STRING_JOINER = "java/util/StringJoiner";
    public static final String COMPARABLE = "java/lang/Comparable";
    public static final String FUNCTION = "java/util/function/Function";
    public static final String EXCEPTION = "java/lang/Exception";
    public static final String LONG_STREAM = "java/util/stream/LongStream";
    public static final String JAVA_THREAD = "java/lang/Thread";
    public static final String JAVA_RUNTIME = "java/lang/Runtime";
    public static final String MAP_ENTRY = "java/util/Map$Entry";
    public static final String MAP_SIMPLE_ENTRY = "java/util/AbstractMap$SimpleEntry";
    public static final String COLLECTION = "java/util/Collection";
    public static final String BIG_DECIMAL = "java/math/BigDecimal";
    public static final String NUMBER = "java/lang/Number";

    // service objects, annotation processing related classes
    public static final String ANNOTATION_UTILS = "org/ballerinalang/jvm/AnnotationUtils";
    public static final String ANNOTATION_MAP_NAME = "$annotation_data";
    public static final String DEFAULTABLE_ARGS_ANOT_NAME = "DefaultableArgs";
    public static final String DEFAULTABLE_ARGS_ANOT_FIELD = "args";

    // types related constants
    public static final String TYPES_ERROR = "typeError";

    // error related constants
    public static final String PANIC_FIELD = "panic";
    public static final String PRINT_STACK_TRACE_METHOD = "printStackTrace";
    public static final String SET_DETAIL_TYPE_METHOD = "setDetailType";
    public static final String SET_TYPEID_SET_METHOD = "setTypeIdSet";
    public static final String ERROR_REASON_METHOD_TOO_LARGE = "MethodTooLarge";
    public static final String ERROR_REASON_CLASS_TOO_LARGE = "ClassTooLarge";
    public static final String TRAP_ERROR_METHOD = "trapError";

    // exception classes
    public static final String BLANG_RUNTIME_EXCEPTION = "org/ballerinalang/jvm/util/exceptions/BLangRuntimeException";
    public static final String THROWABLE = "java/lang/Throwable";
    public static final String STACK_OVERFLOW_ERROR = "java/lang/StackOverflowError";
    public static final String HANDLE_THROWABLE_METHOD = "handleRuntimeErrors";
    public static final String HANDLE_STOP_PANIC_METHOD = "silentlyLogBadSad";
    public static final String HANDLE_RETURNED_ERROR_METHOD = "handleRuntimeReturnValues";
    public static final String UNSUPPORTED_OPERATION_EXCEPTION = "java/lang/UnsupportedOperationException";

    // code generation related constants.
    public static final String MODULE_INIT_CLASS_NAME = "___init";
    public static final String CURRENT_MODULE_INIT = "$currentModuleInit";
    public static final String MODULE_INIT = "$moduleInit";
    public static final String MODULE_START = "$moduleStart";
    public static final String MODULE_STOP = "$moduleStop";
    public static final String BAL_EXTENSION = ".bal";
    public static final String WINDOWS_PATH_SEPERATOR = "\\";
    public static final String UNIX_PATH_SEPERATOR = "/";
    public static final String JAVA_PACKAGE_SEPERATOR = "/";
    public static final String FILE_NAME_PERIOD_SEPERATOR = "$$$";
    public static final String VALUE_CLASS_PREFIX = "$value$";
    public static final String TYPEDESC_CLASS_PREFIX = "$typedesc$";
    public static final String BALLERINA = "ballerina";
    public static final String BUILT_IN_PACKAGE_NAME = "lang.annotations";

    // scheduler related constants
    public static final String SCHEDULE_FUNCTION_METHOD = "scheduleFunction";
    public static final String SCHEDULE_LOCAL_METHOD = "scheduleLocal";
    public static final String SCHEDULER_START_METHOD = "start";

    // strand data related constants
    public static final String STRAND_ANNOTATION = "strand";
    public static final String STRAND_THREAD = "thread";
    public static final String STRAND_DATA_NAME = "name";
    public static final String STRAND_VALUE_ANY = "any";
    public static final String DEFAULT_STRAND_DISPATCHER = "DEFAULT";

    // observability related constants
    public static final String OBSERVER_CONTEXT = "org/ballerinalang/jvm/observability/ObserverContext";
    public static final String OBSERVE_UTILS = "org/ballerinalang/jvm/observability/ObserveUtils";

    // visibility flags
    public static final int BAL_PUBLIC = 1;
    public static final int BAL_NATIVE = 2;
    public static final int BAL_ATTACHED = 8;
    public static final int BAL_REQUIRED = 256;
    public static final int BAL_PRIVATE = 1024;
    public static final int BAL_OPTIONAL = 8192;
    public static final int BAL_SERVICE = 524288;

    // type flags
    public static final int TYPE_FLAG_NILABLE = 1;
    public static final int TYPE_FLAG_ANYDATA = 2;
    public static final int TYPE_FLAG_PURETYPE = 4;

    // ballerina error reasons for ASM operations.
    public static final String METHOD_TOO_LARGE = "MethodTooLarge";
    public static final String CLASS_TOO_LARGE = "ClassTooLarge";

    public static final String GLOBAL_LOCK_NAME = "lock";
    public static final String I_STRING_VALUE = "org/ballerinalang/jvm/values/StringValue";
    public static final String B_STRING_VALUE = "org/ballerinalang/jvm/values/api/BString";
    public static final String NON_BMP_STRING_VALUE = "org/ballerinalang/jvm/values/NonBmpStringValue";
}
