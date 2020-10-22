// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

// jvm bytecode instructions
const int NOP = 0; // visitInsn
const int ACONST_NULL = 1; // -
const int ICONST_M1 = 2; // -
const int ICONST_0 = 3; // -
const int ICONST_1 = 4; // -
const int ICONST_2 = 5; // -
const int ICONST_3 = 6; // -
const int ICONST_4 = 7; // -
const int ICONST_5 = 8; // -
const int LCONST_0 = 9; // -
const int LCONST_1 = 10; // -
const int FCONST_0 = 11; // -
const int FCONST_1 = 12; // -
const int FCONST_2 = 13; // -
const int DCONST_0 = 14; // -
const int DCONST_1 = 15; // -
const int BIPUSH = 16; // visitIntInsn
const int SIPUSH = 17; // -
const int LDC = 18; // visitLdcInsn
const int ILOAD = 21; // visitVarInsn
const int LLOAD = 22; // -
const int FLOAD = 23; // -
const int DLOAD = 24; // -
const int ALOAD = 25; // -
const int IALOAD = 46; // visitInsn
const int LALOAD = 47; // -
const int FALOAD = 48; // -
const int DALOAD = 49; // -
const int AALOAD = 50; // -
const int BALOAD = 51; // -
const int CALOAD = 52; // -
const int SALOAD = 53; // -
const int ISTORE = 54; // visitVarInsn
const int LSTORE = 55; // -
const int FSTORE = 56; // -
const int DSTORE = 57; // -
const int ASTORE = 58; // -
const int IASTORE = 79; // visitInsn
const int LASTORE = 80; // -
const int FASTORE = 81; // -
const int DASTORE = 82; // -
const int AASTORE = 83; // -
const int BASTORE = 84; // -
const int CASTORE = 85; // -
const int SASTORE = 86; // -
const int POP = 87; // -
const int POP2 = 88; // -
const int DUP = 89; // -
const int DUP_X1 = 90; // -
const int DUP_X2 = 91; // -
const int DUP2 = 92; // -
const int DUP2_X1 = 93; // -
const int DUP2_X2 = 94; // -
const int SWAP = 95; // -
const int IADD = 96; // -
const int LADD = 97; // -
const int FADD = 98; // -
const int DADD = 99; // -
const int ISUB = 100; // -
const int LSUB = 101; // -
const int FSUB = 102; // -
const int DSUB = 103; // -
const int IMUL = 104; // -
const int LMUL = 105; // -
const int FMUL = 106; // -
const int DMUL = 107; // -
const int IDIV = 108; // -
const int LDIV = 109; // -
const int FDIV = 110; // -
const int DDIV = 111; // -
const int IREM = 112; // -
const int LREM = 113; // -
const int FREM = 114; // -
const int DREM = 115; // -
const int INEG = 116; // -
const int LNEG = 117; // -
const int FNEG = 118; // -
const int DNEG = 119; // -
const int ISHL = 120; // -
const int LSHL = 121; // -
const int ISHR = 122; // -
const int LSHR = 123; // -
const int IUSHR = 124; // -
const int LUSHR = 125; // -
const int IAND = 126; // -
const int LAND = 127; // -
const int IOR = 128; // -
const int LOR = 129; // -
const int IXOR = 130; // -
const int LXOR = 131; // -
const int IINC = 132; // visitIincInsn
const int I2L = 133; // visitInsn
const int I2F = 134; // -
const int I2D = 135; // -
const int L2I = 136; // -
const int L2F = 137; // -
const int L2D = 138; // -
const int F2I = 139; // -
const int F2L = 140; // -
const int F2D = 141; // -
const int D2I = 142; // -
const int D2L = 143; // -
const int D2F = 144; // -
const int I2B = 145; // -
const int I2C = 146; // -
const int I2S = 147; // -
const int LCMP = 148; // -
const int FCMPL = 149; // -
const int FCMPG = 150; // -
const int DCMPL = 151; // -
const int DCMPG = 152; // -
const int IFEQ = 153; // visitJumpInsn
const int IFNE = 154; // -
const int IFLT = 155; // -
const int IFGE = 156; // -
const int IFGT = 157; // -
const int IFLE = 158; // -
const int IF_ICMPEQ = 159; // -
const int IF_ICMPNE = 160; // -
const int IF_ICMPLT = 161; // -
const int IF_ICMPGE = 162; // -
const int IF_ICMPGT = 163; // -
const int IF_ICMPLE = 164; // -
const int IF_ACMPEQ = 165; // -
const int IF_ACMPNE = 166; // -
const int GOTO = 167; // -
const int JSR = 168; // -
const int RET = 169; // visitVarInsn
const int TABLESWITCH = 170; // visiTableSwitchInsn
const int LOOKUPSWITCH = 171; // visitLookupSwitch
const int IRETURN = 172; // visitInsn
const int LRETURN = 173; // -
const int FRETURN = 174; // -
const int DRETURN = 175; // -
const int ARETURN = 176; // -
const int RETURN = 177; // -
const int GETSTATIC = 178; // visitFieldInsn
const int PUTSTATIC = 179; // -
const int GETFIELD = 180; // -
const int PUTFIELD = 181; // -
const int INVOKEVIRTUAL = 182; // visitMethodInsn
const int INVOKESPECIAL = 183; // -
const int INVOKESTATIC = 184; // -
const int INVOKEINTERFACE = 185; // -
const int INVOKEDYNAMIC = 186; // visitInvokeDynamicInsn
const int NEW = 187; // visitTypeInsn
const int NEWARRAY = 188; // visitIntInsn
const int ANEWARRAY = 189; // visitTypeInsn
const int ARRAYLENGTH = 190; // visitInsn
const int ATHROW = 191; // -
const int CHECKCAST = 192; // visitTypeInsn
const int INSTANCEOF = 193; // -
const int MONITORENTER = 194; // visitInsn
const int MONITOREXIT = 195; // -
const int MULTIANEWARRAY = 197; // visitMultiANewArrayInsn
const int IFNULL = 198; // visitJumpInsn
const int IFNONNULL = 199; // -


// asm constants
const int ACC_PUBLIC = 1;
const int ACC_PRIVATE = 2;
const int ACC_PROTECTED = 4;
const int ACC_STATIC = 8;
const int ACC_FINAL = 16;
const int ACC_SUPER = 32;
const int ACC_SYNCHRONIZED = 32;
const int ACC_OPEN = 32;
const int ACC_TRANSITIVE = 32;
const int ACC_VOLATILE = 64;
const int ACC_BRIDGE = 64;
const int ACC_STATIC_PHASE = 64;
const int ACC_VARARGS = 128;
const int ACC_TRANSIENT = 128;
const int ACC_NATIVE = 256;
const int ACC_INTERFACE = 512;
const int ACC_ABSTRACT = 1024;
const int ACC_STRICT = 2048;
const int ACC_SYNTHETIC = 4096;
const int ACC_ANNOTATION = 8192;
const int ACC_ENUM = 16384;
const int ACC_MANDATED = 32768;
const int ACC_MODULE = 32768;
const int ACC_DEPRECATED = 131072;

const int COMPUTE_MAXS = 1;
const int COMPUTE_FRAMES = 2;
final int V1_8 = 0 << 16 | 52;

// Possible values for the type operand of the NEWARRAY instruction.
// See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.newarray.
int T_BOOLEAN = 4;
int T_CHAR = 5;
int T_FLOAT = 6;
int T_DOUBLE = 7;
int T_BYTE = 8;
int T_SHORT = 9;
int T_INT = 10;
int T_LONG = 11;

// javm values public API classes
const string BXML_QNAME = "io/ballerina/runtime/values/api/BXMLQName";

// jvm runtime values related classes
const string MAP_VALUE = "io/ballerina/runtime/values/MapValue";
const string MAP_VALUE_IMPL = "io/ballerina/runtime/values/MapValueImpl";
const string TABLE_VALUE = "io/ballerina/runtime/values/TableValue";
const string STREAM_VALUE = "io/ballerina/runtime/values/StreamValue";
const string ARRAY_VALUE = "io/ballerina/runtime/values/ArrayValue";
const string OBJECT_VALUE = "io/ballerina/runtime/values/ObjectValue";
const string ABSTRACT_OBJECT_VALUE = "io/ballerina/runtime/values/AbstractObjectValue";
const string REF_VALUE = "io/ballerina/runtime/values/RefValue";
const string ERROR_VALUE = "io/ballerina/runtime/values/ErrorValue";
const string STRING_VALUE = "java/lang/String";
const string BMP_STRING_VALUE = "io/ballerina/runtime/values/BmpStringValue";
const string LONG_VALUE = "java/lang/Long";
const string BOOLEAN_VALUE = "java/lang/Boolean";
const string DOUBLE_VALUE = "java/lang/Double";
const string DECIMAL_VALUE = "io/ballerina/runtime/values/DecimalValue";
const string INT_VALUE = "java/lang/Integer";
const string XML_VALUE = "io/ballerina/runtime/values/XMLValue";
const string XML_QNAME = "io/ballerina/runtime/values/XMLQName";
const string FUTURE_VALUE = "io/ballerina/runtime/values/FutureValue";
const string TYPEDESC_VALUE = "io/ballerina/runtime/values/TypedescValue";
const string HANDLE_VALUE = "io/ballerina/runtime/values/HandleValue";
const string LOCK_VALUE = "io/ballerina/runtime/BLock";
const string LOCK_STORE = "io/ballerina/runtime/BLockStore";
const string FUNCTION_POINTER = "io/ballerina/runtime/values/FPValue";
const string ARRAY_VALUE_IMPL = "io/ballerina/runtime/values/ArrayValueImpl";
const string TUPLE_VALUE_IMPL = "io/ballerina/runtime/values/TupleValueImpl";
const string SIMPLE_VALUE = "io/ballerina/runtime/values/SimpleValue";

const string BHANDLE = "io/ballerina/runtime/values/api/BHandle";

// types related classes
const string BTYPE = "io/ballerina/runtime/types/BType";
const string BTYPES = "io/ballerina/runtime/types/BTypes";
const string ARRAY_TYPE = "io/ballerina/runtime/types/BArrayType";
const string MAP_TYPE = "io/ballerina/runtime/types/BMapType";
const string TABLE_TYPE = "io/ballerina/runtime/types/BTableType";
const string STREAM_TYPE = "io/ballerina/runtime/types/BStreamType";
const string UNION_TYPE = "io/ballerina/runtime/types/BUnionType";
const string RECORD_TYPE = "io/ballerina/runtime/types/BRecordType";
const string OBJECT_TYPE = "io/ballerina/runtime/types/BObjectType";
const string SERVICE_TYPE = "io/ballerina/runtime/types/BServiceType";
const string ERROR_TYPE = "io/ballerina/runtime/types/BErrorType";
const string TUPLE_TYPE = "io/ballerina/runtime/types/BTupleType";
const string FUNCTION_TYPE = "io/ballerina/runtime/types/BFunctionType";
const string TYPEDESC_TYPE = "io/ballerina/runtime/types/BTypedescType";
const string BFIELD = "io/ballerina/runtime/types/BField";
const string ATTACHED_FUNCTION = "io/ballerina/runtime/types/AttachedFunction";
const string FINITE_TYPE = "io/ballerina/runtime/types/BFiniteType";
const string FUTURE_TYPE = "io/ballerina/runtime/types/BFutureType";
const string PACKAGE_TYPE = "io/ballerina/runtime/types/BPackage";

// other jvm-specific classes
const string TYPE_CHECKER = "io/ballerina/runtime/TypeChecker";
const string SCHEDULER = "io/ballerina/runtime/scheduling/Scheduler";
const string JSON_UTILS = "io/ballerina/runtime/JSONUtils";
const string STRAND = "io/ballerina/runtime/scheduling/Strand";
const string TYPE_CONVERTER = "io/ballerina/runtime/TypeConverter";
const string LIST_UTILS = "io/ballerina/runtime/Lists";
const string STRAND_STATE = "io/ballerina/runtime/scheduling/State";
const string VALUE_CREATOR = "io/ballerina/runtime/values/ValueCreator";
const string XML_FACTORY = "io/ballerina/runtime/XMLFactory";
const string WD_CHANNELS = "io/ballerina/runtime/scheduling/WDChannels";
const string WORKER_DATA_CHANNEL = "io/ballerina/runtime/scheduling/WorkerDataChannel";
const string CHANNEL_DETAILS = "io/ballerina/runtime/values/ChannelDetails";
const string WORKER_UTILS = "io/ballerina/runtime/scheduling/WorkerUtils";
const string MAP_UTILS = "io/ballerina/runtime/MapUtils";
const string STRING_UTILS = "io/ballerina/runtime/StringUtils";
const string BAL_ERRORS = "io/ballerina/runtime/BallerinaErrors";
const string RUNTIME_UTILS = "io/ballerina/runtime/util/RuntimeUtils";
const string ARGUMENT_PARSER = "io/ballerina/runtime/util/ArgumentParser";
const string LAUNCH_UTILS = "io/ballerina/runtime/launch/LaunchUtils";
const string MATH_UTILS = "io/ballerina/runtime/MathUtils";
const string BAL_ERROR_REASONS = "io/ballerina/runtime/util/exceptions/BallerinaErrorReasons";
const string RUNTIME_ERRORS = "io/ballerina/runtime/util/exceptions/RuntimeErrors";
const string BLANG_EXCEPTION_HELPER = "io/ballerina/runtime/util/exceptions/BLangExceptionHelper";
const string COMPATIBILITY_CHECKER = "io/ballerina/runtime/util/CompatibilityChecker";

// other java classes
const string OBJECT = "java/lang/Object";
const string MATH = "java/lang/Math";
const string MAP = "java/util/Map";
const string LINKED_HASH_MAP = "java/util/LinkedHashMap";
const string ARRAY_LIST = "java/util/ArrayList";
const string LIST = "java/util/List";
const string SET = "java/util/Set";
const string LINKED_HASH_SET = "java/util/LinkedHashSet";
const string STRING_BUILDER = "java/lang/StringBuilder";
const string STRING_JOINER = "java/util/StringJoiner";
const string COMPARABLE = "java/lang/Comparable";
const string FUNCTION = "java/util/function/Function";
const string EXCEPTION = "java/lang/Exception";
const string LONG_STREAM = "java/util/stream/LongStream";
const string JAVA_THREAD = "java/lang/Thread";
const string JAVA_RUNTIME = "java/lang/Runtime";
const string MAP_ENTRY = "java/util/Map$Entry";
const string MAP_SIMPLE_ENTRY = "java/util/AbstractMap$SimpleEntry";
const string COLLECTION = "java/util/Collection";
const string BIG_DECIMAL = "java/math/BigDecimal";
const string NUMBER = "java/lang/Number";

// service objects, annotation processing related classes
const string ANNOTATION_UTILS = "io/ballerina/runtime/AnnotationUtils";
const string ANNOTATION_MAP_NAME = "$annotation_data";
const string DEFAULTABLE_ARGS_ANOT_NAME = "DefaultableArgs";
const string DEFAULTABLE_ARGS_ANOT_FIELD = "args";

// types related constants
const string TYPES_ERROR =  "typeError";

// error related constants
const string PANIC_FIELD =  "panic";
const string PRINT_STACK_TRACE_METHOD = "printStackTrace";
const string SET_DETAIL_TYPE_METHOD = "setDetailType";
const string ERROR_REASON_METHOD_TOO_LARGE = "MethodTooLarge";
const string ERROR_REASON_CLASS_TOO_LARGE = "ClassTooLarge";
const string TRAP_ERROR_METHOD = "trapError";

// exception classes
const string BLANG_RUNTIME_EXCEPTION = "io/ballerina/runtime/util/exceptions/BLangRuntimeException";
const string THROWABLE = "java/lang/Throwable";
const string STACK_OVERFLOW_ERROR = "java/lang/StackOverflowError";
const string HANDLE_THROWABLE_METHOD = "handleRuntimeErrors";
const string HANDLE_STOP_PANIC_METHOD = "silentlyLogBadSad";
const string HANDLE_RETURNED_ERROR_METHOD = "handleRuntimeReturnValues";
const string UNSUPPORTED_OPERATION_EXCEPTION = "java/lang/UnsupportedOperationException";

// code generation related constants.
const string MODULE_INIT_CLASS_NAME = "$_init";
const string CURRENT_MODULE_INIT = "$currentModuleInit";
const string MODULE_INIT = "$moduleInit";
const string MODULE_START = "$moduleStart";
const string MODULE_STOP = "$moduleStop";
const string BAL_EXTENSION = ".bal";
const string WINDOWS_PATH_SEPERATOR = "\\";
const string UNIX_PATH_SEPERATOR = "/";
const string JAVA_PACKAGE_SEPERATOR = "/";
const string FILE_NAME_PERIOD_SEPERATOR = "$$$";
const string BALLERINA = "ballerina";
const string BUILT_IN_PACKAGE_NAME = "lang.annotations";


// scheduler related constants
const string SCHEDULE_FUNCTION_METHOD =  "scheduleFunction";
const string SCHEDULE_LOCAL_METHOD = "scheduleLocal";
const string SCHEDULER_START_METHOD =  "start";

// observability related constants
const string OBSERVER_CONTEXT = "io/ballerina/runtime/observability/ObserverContext";
const string OBSERVE_UTILS = "io/ballerina/runtime/observability/ObserveUtils";

// visibility flags
const int BAL_PUBLIC = 1;
const int BAL_NATIVE = 2;
const int BAL_ATTACHED = 8;
const int BAL_REQUIRED = 256;
const int BAL_PRIVATE = 1024;
const int BAL_OPTIONAL = 8192;
const int BAL_SERVICE = 524288;

// type flags
const int TYPE_FLAG_NILABLE = 1;
const int TYPE_FLAG_ANYDATA = 2;
const int TYPE_FLAG_PURETYPE = 4;
