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
    public static final String B_XML_QNAME = "io/ballerina/runtime/api/values/BXMLQName";
    public static final String B_FUNCTION_POINTER = "io/ballerina/runtime/api/values/BFunctionPointer";
    public static final String B_MAP = "io/ballerina/runtime/api/values/BMap";
    public static final String B_OBJECT = "io/ballerina/runtime/api/values/BObject";

    // jvm runtime values related classes
    public static final String MAP_VALUE = "io/ballerina/runtime/values/MapValue";
    public static final String MAP_VALUE_IMPL = "io/ballerina/runtime/values/MapValueImpl";
    public static final String STREAM_VALUE = "io/ballerina/runtime/values/StreamValue";
    public static final String TABLE_VALUE = "io/ballerina/runtime/values/TableValue";
    public static final String ARRAY_VALUE = "io/ballerina/runtime/values/ArrayValue";
    public static final String OBJECT_VALUE = "io/ballerina/runtime/values/ObjectValue";
    public static final String ABSTRACT_OBJECT_VALUE = "io/ballerina/runtime/values/AbstractObjectValue";
    public static final String REF_VALUE = "io/ballerina/runtime/values/RefValue";
    public static final String ERROR_VALUE = "io/ballerina/runtime/values/ErrorValue";
    public static final String BERROR = "io/ballerina/runtime/api/values/BError";
    public static final String STRING_VALUE = "java/lang/String";
    public static final String B_STRING_VALUE = "io/ballerina/runtime/api/values/BString";
    public static final String NON_BMP_STRING_VALUE = "io/ballerina/runtime/values/NonBmpStringValue";
    public static final String BMP_STRING_VALUE = "io/ballerina/runtime/values/BmpStringValue";
    public static final String LONG_VALUE = "java/lang/Long";
    public static final String BYTE_VALUE = "java/lang/Byte";
    public static final String SHORT_VALUE = "java/lang/Short";
    public static final String BOOLEAN_VALUE = "java/lang/Boolean";
    public static final String DOUBLE_VALUE = "java/lang/Double";
    public static final String DECIMAL_VALUE = "io/ballerina/runtime/values/DecimalValue";
    public static final String INT_VALUE = "java/lang/Integer";
    public static final String XML_VALUE = "io/ballerina/runtime/values/XMLValue";
    public static final String XML_QNAME = "io/ballerina/runtime/values/XMLQName";
    public static final String FUTURE_VALUE = "io/ballerina/runtime/values/FutureValue";
    public static final String TYPEDESC_VALUE_IMPL = "io/ballerina/runtime/values/TypedescValueImpl";
    public static final String TYPEDESC_VALUE_IMPL_CLOSURES = "closures";
    public static final String TYPEDESC_VALUE = "io/ballerina/runtime/values/TypedescValue";
    public static final String HANDLE_VALUE = "io/ballerina/runtime/values/HandleValue";
    public static final String LOCK_VALUE = "io/ballerina/runtime/BLock";
    public static final String LOCK_STORE = "io/ballerina/runtime/BLockStore";
    public static final String FUNCTION_POINTER = "io/ballerina/runtime/values/FPValue";
    public static final String ARRAY_VALUE_IMPL = "io/ballerina/runtime/values/ArrayValueImpl";
    public static final String TUPLE_VALUE_IMPL = "io/ballerina/runtime/values/TupleValueImpl";
    public static final String TABLE_VALUE_IMPL = "io/ballerina/runtime/values/TableValueImpl";
    public static final String SIMPLE_VALUE = "io/ballerina/runtime/values/SimpleValue";

    public static final String B_HANDLE = "io/ballerina/runtime/api/values/BHandle";

    public static final String B_INITIAL_VALUE_ENTRY = "io/ballerina/runtime/api/values/BInitialValueEntry";
    public static final String B_MAPPING_INITIAL_VALUE_ENTRY = "io/ballerina/runtime/api/values/BMapInitialValueEntry";
    public static final String MAPPING_INITIAL_VALUE_ENTRY = "io/ballerina/runtime/values/MappingInitialValueEntry";
    public static final String MAPPING_INITIAL_KEY_VALUE_ENTRY =
            "io/ballerina/runtime/values/MappingInitialValueEntry$KeyValueEntry";
    public static final String MAPPING_INITIAL_SPREAD_FIELD_ENTRY =
            "io/ballerina/runtime/values/MappingInitialValueEntry$SpreadFieldEntry";
    public static final String LIST_INITIAL_VALUE_ENTRY = "io/ballerina/runtime/values/ListInitialValueEntry";
    public static final String LIST_INITIAL_EXPRESSION_ENTRY =
            "io/ballerina/runtime/values/ListInitialValueEntry$ExpressionEntry";

    // types related classes
    public static final String TYPE = "io/ballerina/runtime/api/types/Type";
    public static final String PREDEFINED_TYPES = "io/ballerina/runtime/api/PredefinedTypes";

    public static final String ARRAY_TYPE = "io/ballerina/runtime/api/types/ArrayType";
    public static final String MAP_TYPE = "io/ballerina/runtime/api/types/MapType";
    public static final String XML_TYPE = "io/ballerina/runtime/api/types/XMLType";
    public static final String JSON_TYPE = "io/ballerina/runtime/api/types/JSONType";
    public static final String STREAM_TYPE = "io/ballerina/runtime/api/types/StreamType";
    public static final String TABLE_TYPE = "io/ballerina/runtime/api/types/TableType";
    public static final String UNION_TYPE = "io/ballerina/runtime/api/types/UnionType";
    public static final String INTERSECTION_TYPE = "io/ballerina/runtime/api/types/IntersectionType";
    public static final String RECORD_TYPE = "io/ballerina/runtime/api/types/RecordType";
    public static final String OBJECT_TYPE = "io/ballerina/runtime/api/types/ObjectType";
    public static final String SERVICE_TYPE = "io/ballerina/runtime/api/types/ServiceType";
    public static final String ERROR_TYPE = "io/ballerina/runtime/api/types/ErrorType";
    public static final String TUPLE_TYPE = "io/ballerina/runtime/api/types/TupleType";
    public static final String FUNCTION_TYPE = "io/ballerina/runtime/api/types/FunctionType";
    public static final String TYPEDESC_TYPE = "io/ballerina/runtime/api/types/TypedescType";
    public static final String FIELD = "io/ballerina/runtime/api/types/Field";
    public static final String ATTACHED_FUNCTION = "io/ballerina/runtime/api/types/AttachedFunctionType";
    public static final String FINITE_TYPE = "io/ballerina/runtime/api/types/FiniteType";
    public static final String FUTURE_TYPE = "io/ballerina/runtime/api/types/FutureType";
    public static final String INTEGER_TYPE = "io/ballerina/runtime/api/types/IntegerType";
    public static final String BYTE_TYPE = "io/ballerina/runtime/api/types/ByteType";
    public static final String FLOAT_TYPE = "io/ballerina/runtime/api/types/FloatType";
    public static final String STRING_TYPE = "io/ballerina/runtime/api/types/StringType";
    public static final String BOOLEAN_TYPE = "io/ballerina/runtime/api/types/BooleanType";
    public static final String DECIMAL_TYPE = "io/ballerina/runtime/api/types/DecimalType";
    public static final String READONLY_TYPE = "io/ballerina/runtime/api/types/ReadonlyType";
    public static final String ANY_TYPE = "io/ballerina/runtime/api/types/AnyType";
    public static final String ANYDATA_TYPE = "io/ballerina/runtime/api/types/AnydataType";
    public static final String NEVER_TYPE = "io/ballerina/runtime/api/types/NeverType";
    public static final String NULL_TYPE = "io/ballerina/runtime/api/types/NullType";
    public static final String HANDLE_TYPE = "io/ballerina/runtime/api/types/HandleType";


    public static final String TYPE_IMPL = "io/ballerina/runtime/types/BType";
    public static final String ARRAY_TYPE_IMPL = "io/ballerina/runtime/types/BArrayType";
    public static final String MAP_TYPE_IMPL = "io/ballerina/runtime/types/BMapType";
    public static final String XML_TYPE_IMPL = "io/ballerina/runtime/types/BXMLType";
    public static final String STREAM_TYPE_IMPL = "io/ballerina/runtime/types/BStreamType";
    public static final String TABLE_TYPE_IMPL = "io/ballerina/runtime/types/BTableType";
    public static final String UNION_TYPE_IMPL = "io/ballerina/runtime/types/BUnionType";
    public static final String INTERSECTION_TYPE_IMPL = "io/ballerina/runtime/types/BIntersectionType";
    public static final String RECORD_TYPE_IMPL = "io/ballerina/runtime/types/BRecordType";
    public static final String OBJECT_TYPE_IMPL = "io/ballerina/runtime/types/BObjectType";
    public static final String SERVICE_TYPE_IMPL = "io/ballerina/runtime/types/BServiceType";
    public static final String ERROR_TYPE_IMPL = "io/ballerina/runtime/types/BErrorType";
    public static final String TUPLE_TYPE_IMPL = "io/ballerina/runtime/types/BTupleType";
    public static final String FUNCTION_TYPE_IMPL = "io/ballerina/runtime/types/BFunctionType";
    public static final String TYPEDESC_TYPE_IMPL = "io/ballerina/runtime/types/BTypedescType";
    public static final String FIELD_IMPL = "io/ballerina/runtime/types/BField";
    public static final String ATTACHED_FUNCTION_IMPL = "io/ballerina/runtime/types/AttachedFunction";
    public static final String FINITE_TYPE_IMPL = "io/ballerina/runtime/types/BFiniteType";
    public static final String FUTURE_TYPE_IMPL = "io/ballerina/runtime/types/BFutureType";
    public static final String MODULE = "io/ballerina/runtime/api/Module";
    public static final String TYPE_ID_SET = "io/ballerina/runtime/types/BTypeIdSet";
    public static final String TYPE_ID = "io/ballerina/runtime/types/BTypeIdSet$TypeId";

    // other jvm-specific classes
    public static final String TYPE_CHECKER = "io/ballerina/runtime/TypeChecker";
    public static final String SCHEDULER = "io/ballerina/runtime/scheduling/Scheduler";
    public static final String JSON_UTILS = "io/ballerina/runtime/JSONUtils";
    public static final String STRAND_CLASS = "io/ballerina/runtime/scheduling/Strand";
    public static final String STRAND_METADATA = "io/ballerina/runtime/api/async/StrandMetadata";
    public static final String BAL_ENV = "io/ballerina/runtime/api/Environment";
    public static final String BAL_FUTURE = "io/ballerina/runtime/api/Future";
    public static final String TYPE_CONVERTER = "io/ballerina/runtime/TypeConverter";
    public static final String STRAND_STATE = "io/ballerina/runtime/scheduling/State";
    public static final String VALUE_CREATOR = "io/ballerina/runtime/values/ValueCreator";
    public static final String XML_FACTORY = "io/ballerina/runtime/XMLFactory";
    public static final String WD_CHANNELS = "io/ballerina/runtime/scheduling/WDChannels";
    public static final String WORKER_DATA_CHANNEL = "io/ballerina/runtime/scheduling/WorkerDataChannel";
    public static final String CHANNEL_DETAILS = "io/ballerina/runtime/values/ChannelDetails";
    public static final String WORKER_UTILS = "io/ballerina/runtime/scheduling/WorkerUtils";
    public static final String MAP_UTILS = "io/ballerina/runtime/MapUtils";
    public static final String TABLE_UTILS = "io/ballerina/runtime/TableUtils";
    public static final String STRING_UTILS = "io/ballerina/runtime/api/StringUtils";
    public static final String ERROR_UTILS = "io/ballerina/runtime/internal/ErrorUtils";
    public static final String ERROR_CREATOR = "io/ballerina/runtime/api/ErrorCreator";
    public static final String RUNTIME_UTILS = "io/ballerina/runtime/util/RuntimeUtils";
    public static final String ARGUMENT_PARSER = "io/ballerina/runtime/util/ArgumentParser";
    public static final String LAUNCH_UTILS = "io/ballerina/runtime/launch/LaunchUtils";
    public static final String MATH_UTILS = "io/ballerina/runtime/MathUtils";
    public static final String BAL_ERROR_REASONS = "io/ballerina/runtime/util/exceptions/BallerinaErrorReasons";
    public static final String RUNTIME_ERRORS = "io/ballerina/runtime/util/exceptions/RuntimeErrors";
    public static final String BLANG_EXCEPTION_HELPER = "io/ballerina/runtime/util/exceptions/BLangExceptionHelper";
    public static final String COMPATIBILITY_CHECKER = "io/ballerina/runtime/util/CompatibilityChecker";

    // other java classes
    public static final String OBJECT = "java/lang/Object";
    public static final String MAP = "java/util/Map";
    public static final String LINKED_HASH_MAP = "java/util/LinkedHashMap";
    public static final String ARRAY_LIST = "java/util/ArrayList";
    public static final String LIST = "java/util/List";
    public static final String SET = "java/util/Set";
    public static final String LINKED_HASH_SET = "java/util/LinkedHashSet";
    public static final String STRING_BUILDER = "java/lang/StringBuilder";
    public static final String COMPARABLE = "java/lang/Comparable";
    public static final String FUNCTION = "java/util/function/Function";
    public static final String LONG_STREAM = "java/util/stream/LongStream";
    public static final String JAVA_THREAD = "java/lang/Thread";
    public static final String JAVA_RUNTIME = "java/lang/Runtime";
    public static final String MAP_ENTRY = "java/util/Map$Entry";
    public static final String MAP_SIMPLE_ENTRY = "java/util/AbstractMap$SimpleEntry";
    public static final String COLLECTION = "java/util/Collection";
    public static final String NUMBER = "java/lang/Number";
    public static final String HASH_MAP = "java/util/HashMap";

    // service objects, annotation processing related classes
    public static final String ANNOTATION_UTILS = "io/ballerina/runtime/AnnotationUtils";
    public static final String ANNOTATION_MAP_NAME = "$annotation_data";
    public static final String DEFAULTABLE_ARGS_ANOT_NAME = "DefaultableArgs";
    public static final String DEFAULTABLE_ARGS_ANOT_FIELD = "args";

    // types related constants
    public static final String TYPES_ERROR = "TYPE_ERROR";

    // error related constants
    public static final String PANIC_FIELD = "panic";
    public static final String PRINT_STACK_TRACE_METHOD = "printStackTrace";
    public static final String SET_DETAIL_TYPE_METHOD = "setDetailType";
    public static final String SET_TYPEID_SET_METHOD = "setTypeIdSet";
    public static final String TRAP_ERROR_METHOD = "trapError";
    public static final String BLOCKED_ON_EXTERN_FIELD = "blockedOnExtern";
    public static final String IS_BLOCKED_ON_EXTERN_FIELD = "isBlockedOnExtern";

    // Immutable type related constants.
    public static final String SET_IMMUTABLE_TYPE_METHOD = "setImmutableType";

    // exception classes
    public static final String BLANG_RUNTIME_EXCEPTION = "io/ballerina/runtime/util/exceptions/BLangRuntimeException";
    public static final String THROWABLE = "java/lang/Throwable";
    public static final String STACK_OVERFLOW_ERROR = "java/lang/StackOverflowError";
    public static final String HANDLE_THROWABLE_METHOD = "handleRuntimeErrorsAndExit";
    public static final String HANDLE_RETURNED_ERROR_METHOD = "handleRuntimeReturnValues";
    public static final String UNSUPPORTED_OPERATION_EXCEPTION = "java/lang/UnsupportedOperationException";
    public static final String HANDLE_STOP_PANIC_METHOD = "handleRuntimeErrors";

    // code generation related constants.
    public static final String MODULE_INIT_CLASS_NAME = "$_init";
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
    public static final String MODULE_START_ATTEMPTED = "$moduleStartAttempted";
    public static final String MODULE_STARTED = "$moduleStarted";
    public static final String DESUGARED_BB_ID_NAME = "desugaredBB";
    public static final String WRAPPER_GEN_BB_ID_NAME = "wrapperGen";
    public static final String JVM_INIT_METHOD = "<init>";
    public static final String JVM_TO_STRING_METHOD = "toString";
    public static final String JVM_TO_UNSIGNED_INT_METHOD = "toUnsignedInt";
    public static final String GET_VALUE_METHOD = "getValue";
    public static final String ANY_TO_BYTE_METHOD = "anyToByte";
    public static final String ANY_TO_INT_METHOD = "anyToInt";
    public static final String ANY_TO_FLOAT_METHOD = "anyToFloat";
    public static final String ANY_TO_DECIMAL_METHOD = "anyToDecimal";
    public static final String ANY_TO_BOOLEAN_METHOD = "anyToBoolean";
    public static final String DECIMAL_VALUE_OF_J_METHOD = "valueOfJ";
    public static final String VALUE_OF_METHOD = "valueOf";
    public static final String POPULATE_INITIAL_VALUES_METHOD = "populateInitialValues";
    public static final String CREATE_TYPES_METHOD = "$createTypes";
    public static final String CREATE_TYPE_INSTANCES_METHOD = "$createTypeInstances";
    public static final String GLOBAL_LOCK_NAME = "lock";
    public static final String SERVICE_EP_AVAILABLE = "$serviceEPAvailable";
    public static final String LOCK_STORE_VAR_NAME = "$LOCK_STORE";
    public static final String RECORD_INIT_WRAPPER_NAME = "$init";


    // scheduler related constants
    public static final String SCHEDULE_FUNCTION_METHOD = "scheduleFunction";
    public static final String SCHEDULE_LOCAL_METHOD = "scheduleLocal";
    public static final String SCHEDULER_START_METHOD = "start";
    public static final String CREATE_RECORD_VALUE = "createRecordValue";
    public static final String CREATE_OBJECT_VALUE = "createObjectValue";

    // strand data related constants
    public static final String STRAND = "strand";
    public static final String STRAND_THREAD = "thread";
    public static final String STRAND_NAME = "name";
    public static final String STRAND_POLICY_NAME = "policy";
    public static final String STRAND_VALUE_ANY = "any";
    public static final String STRAND_METADATA_VAR_PREFIX = "$strand_metadata$";
    public static final String DEFAULT_STRAND_DISPATCHER = "DEFAULT";

    // observability related constants
    public static final String OBSERVE_UTILS = "io/ballerina/runtime/observability/ObserveUtils";
    public static final String START_RESOURCE_OBSERVATION_METHOD = "startResourceObservation";
    public static final String START_CALLABLE_OBSERVATION_METHOD = "startCallableObservation";
    public static final String REPORT_ERROR_METHOD = "reportError";
    public static final String STOP_OBSERVATION_METHOD = "stopObservation";
    public static final String OBSERVABLE_ANNOTATION = "ballerina/observe/Observable";

    // visibility flags
    public static final int BAL_PUBLIC = 1;
    public static final int BAL_NATIVE = 2;
    public static final int BAL_ATTACHED = 8;
    public static final int BAL_REQUIRED = 256;
    public static final int BAL_PRIVATE = 1024;
    public static final int BAL_OPTIONAL = 4096;
    public static final int BAL_SERVICE = 262144;

    // type flags
    public static final int TYPE_FLAG_NILABLE = 1;
    public static final int TYPE_FLAG_ANYDATA = 2;
    public static final int TYPE_FLAG_PURETYPE = 4;

    // ballerina error reasons for ASM operations.
    public static final String CLASS_TOO_LARGE = "ClassTooLarge";


    public static final String TYPE_NOT_SUPPORTED_MESSAGE = "JVM generation is not supported for type ";

    private JvmConstants() {
    }
}
