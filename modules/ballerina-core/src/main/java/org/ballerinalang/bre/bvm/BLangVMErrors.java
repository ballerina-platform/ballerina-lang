/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.ActionInfo;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util Class for handling Error in Ballerina VM.
 *
 * @since 0.88
 */
public class BLangVMErrors {

    private static final Logger logger = LoggerFactory.getLogger(BLangVMErrors.class);

    public static final String ERROR_PACKAGE = "ballerina.lang.errors";
    public static final String STRUCT_GENERIC_ERROR = "Error";
    public static final String STRUCT_NULL_REF_ERROR = "NullReferenceError";
    public static final String STRUCT_TYPE_CAST_ERROR = "TypeCastError";
    public static final String STRUCT_TYPE_CONVERSION_ERROR = "TypeConversionError";
    public static final String STRUCT_STACKTRACE = "StackTrace";
    public static final String STRUCT_STACKTRACE_ITEM = "StackTraceItem";


    /**
     * Create ballerina.lang.errors:Error Struct from given error message.
     *
     * @param context current Context
     * @param ip      current instruction pointer
     * @param message error message
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BStruct createError(Context context, int ip, String message) {
        return generateError(context, ip, null, message);
    }

    /**
     * Create ballerina.lang.errors:Error Struct from given error message.
     *
     * @param context current Context
     * @param ip      current instruction pointer
     * @param message error message
     * @param cause   caused error struct
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BStruct createError(Context context, int ip, String message, BStruct cause) {
        return generateError(context, ip, null, message, cause);
    }

    /**
     * Create Error Struct from given struct type and message.
     *
     * @param context   current Context
     * @param ip        current instruction pointer
     * @param errorType error struct type
     * @param message   error message
     * @return generated error struct
     */
    public static BStruct createError(Context context, int ip, StructInfo errorType, String message) {
        return generateError(context, ip, errorType, message);
    }

    /**
     * Create NullReferenceError.
     *
     * @param context current Context
     * @param ip      current instruction pointer
     * @return created NullReferenceError
     */
    public static BStruct createNullRefError(Context context, int ip) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(ERROR_PACKAGE);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_NULL_REF_ERROR);
        BStruct error = createBStruct(errorStructInfo);

        // Set StackTrace.
        StructInfo stackTrace = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE);
        BStruct bStackTrace = createBStruct(stackTrace, generateStackTraceItems(context, ip - 1));
        error.setStackTrace(bStackTrace);
        return error;
    }

    /**
     * Create TypeCastError.
     *
     * @param context    current Context
     * @param ip         current instruction pointer
     * @param sourceType For which error happened
     * @param targetType For which error happened
     * @return created NullReferenceError
     */
    public static BStruct createTypeCastError(Context context, int ip, BType sourceType, BType targetType) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(ERROR_PACKAGE);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_TYPE_CAST_ERROR);

        String errorMsg = "'" + sourceType + "' cannot be cast to '" + targetType + "'";
        BStruct error = createBStruct(errorStructInfo, errorMsg, null,
                sourceType.toString(), targetType.toString());

        // Set StackTrace.
        StructInfo stackTrace = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE);
        BStruct bStackTrace = createBStruct(stackTrace, generateStackTraceItems(context, ip - 1));
        error.setStackTrace(bStackTrace);
        return error;
    }

    /**
     * Create TypeConversionError.
     *
     * @param context        current Context
     * @param ip             current instruction pointer
     * @param sourceTypeName For which error happened
     * @param targetTypeName For which error happened
     * @return created NullReferenceError
     */
    public static BStruct createTypeConversionError(Context context, int ip,
                                                    String sourceTypeName, String targetTypeName) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(ERROR_PACKAGE);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_TYPE_CONVERSION_ERROR);

        String errorMsg = "'" + sourceTypeName + "' cannot be converted to '" + targetTypeName + "'";
        BStruct error = createBStruct(errorStructInfo, errorMsg, null,
                sourceTypeName, targetTypeName);

        // Set StackTrace.
        StructInfo stackTrace = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE);
        BStruct bStackTrace = createBStruct(stackTrace, generateStackTraceItems(context, ip - 1));
        error.setStackTrace(bStackTrace);
        return error;
    }

    /**
     * Create TypeConversionError.
     *
     * @param context        current Context
     * @param ip             current instruction pointer
     * @param errorMessage   error message
     * @param sourceTypeName source type name
     * @param targetTypeName target type name
     * @return created TypeConversionError
     */
    public static BStruct createTypeConversionError(Context context, int ip, String errorMessage,
                                                    String sourceTypeName, String targetTypeName) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(ERROR_PACKAGE);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_TYPE_CONVERSION_ERROR);

        BStruct error = createBStruct(errorStructInfo, errorMessage, null,
                sourceTypeName, targetTypeName);

        // Set StackTrace.
        StructInfo stackTrace = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE);
        BStruct bStackTrace = createBStruct(stackTrace, generateStackTraceItems(context, ip - 1));
        error.setStackTrace(bStackTrace);
        return error;
    }


    /**
     * Create Error struct from given Struct type.
     *
     * @param context current Context
     * @param ip      current instruction pointer
     * @param error   struct to be converted to error
     * @return generated error struct, if type incompatible generated ballerina.lang.errors:Error struct with type
     * miss matched error.
     */
    public static BStruct createError(Context context, int ip, BStruct error) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(ERROR_PACKAGE);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        if (!BLangVM.checkStructEquivalency((BStructType) error.getType(),
                (BStructType) errorStructInfo.getType())) {
            logger.error("bvm internal error! incompatible error strut type " + error.getType().getSig().getPkgPath()
                    + ":" + error.getType().getSig().getName());
            error = createError(context, ip,
                    "bvm internal error! incompatible error strut type " + error.getType().getSig().getPkgPath() + ":" +
                            error.getType().getSig().getName());
        }
        StructInfo stackTrace = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE);
        BStruct bStackTrace = createBStruct(stackTrace, generateStackTraceItems(context, ip - 1));
        error.setStackTrace(bStackTrace);
        return error;
    }

    /**
     * Generate Error from current error.
     *
     * @param context    current Context
     * @param ip         current instruction pointer
     * @param structInfo {@link StructInfo} of the error that need to be generated
     * @param values     field values of the error Struct.
     * @return generated error {@link BStruct}
     */
    private static BStruct generateError(Context context, int ip, StructInfo structInfo, Object... values) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(ERROR_PACKAGE);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        if (structInfo == null || BLangVM.checkStructEquivalency((BStructType) structInfo.getType(),
                (BStructType) errorStructInfo.getType())) {
            structInfo = errorStructInfo;
        }
        BStruct error = createBStruct(structInfo, values);

        // Set StackTrace.
        StructInfo stackTrace = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE);
        BStruct bStackTrace = createBStruct(stackTrace, generateStackTraceItems(context, ip - 1));
        error.setStackTrace(bStackTrace);
        return error;
    }


    /**
     * Set StackTrace for given Error Struct.
     *
     * @param context current Context
     * @param ip      current instruction pointer
     * @param error   error Struct to be set the stackTrace.
     */
    public static void setStackTrace(Context context, int ip, BStruct error) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(ERROR_PACKAGE);
        if (error == null) {
            // This shouldn't execute.
            logger.warn("Error struct is null. Default Error is created.");
            StructInfo structInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
            error = createBStruct(structInfo);
        }
        StructInfo stackTrace = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE);
        BStruct bStackTrace = createBStruct(stackTrace, generateStackTraceItems(context, ip - 1));
        error.setStackTrace(bStackTrace);
    }

    /**
     * Generate StackTraceItem array.
     *
     * @param context current Context
     * @param ip      current instruction pointer
     * @return generated StackTraceItem struct array
     */
    public static BRefValueArray generateStackTraceItems(Context context, int ip) {
        BRefValueArray stackTraceItems = new BRefValueArray();
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(ERROR_PACKAGE);
        StructInfo stackTraceItem = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE_ITEM);
        ControlStackNew controlStack = context.getControlStackNew();

        int currentIP = ip;
        Object[] values;
        int stackTraceLocation = 0;
        StackFrame[] stackFrames = controlStack.getStack();
        for (int i = controlStack.fp; i >= 0; i--) {
            values = new Object[4];
            StackFrame stackFrame = stackFrames[i];
            CallableUnitInfo callableUnitInfo = stackFrame.callableUnitInfo;
            if (callableUnitInfo == null) {
                continue;
            }

            String parentScope = "";
            if (callableUnitInfo instanceof ResourceInfo) {
                parentScope = ((ResourceInfo) callableUnitInfo).getServiceInfo().getName() + ".";
            } else if (callableUnitInfo instanceof ActionInfo) {
                parentScope = ((ActionInfo) callableUnitInfo).getConnectorInfo().getName() + ".";
            }

            values[0] = parentScope + callableUnitInfo.getName();
            values[1] = callableUnitInfo.getPkgPath();
            LineNumberInfo lineNumberInfo = callableUnitInfo.getPackageInfo().getLineNumberInfo(currentIP);
            if (lineNumberInfo != null) {
                values[2] = lineNumberInfo.getFileName();
                values[3] = lineNumberInfo.getLineNumber();
            } else {
                values[2] = "<native>";
                values[3] = 0;
            }

            stackTraceItems.add(stackTraceLocation, createBStruct(stackTraceItem, values));
            // Always get the previous instruction pointer.
            currentIP = stackFrame.retAddrs - 1;
            stackTraceLocation++;
        }
        return stackTraceItems;
    }

    /**
     * Create BStruct for given StructInfo and BValues.
     *
     * @param structInfo {@link StructInfo} of the BStruct
     * @param values     field values of the BStruct.
     * @return BStruct instance.
     */
    public static BStruct createBStruct(StructInfo structInfo, Object... values) {
        BStructType structType = structInfo.getType();
        BStruct bStruct = new BStruct(structType);

        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;
        BStructType.StructField[] structFields = structType.getStructFields();
        for (int i = 0; i < structFields.length; i++) {
            BType paramType = structFields[i].getFieldType();
            if (values.length < i + 1) {
                break;
            }
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    ++longRegIndex;
                    if (values[i] != null) {
                        if (values[i] instanceof Integer) {
                            bStruct.setIntField(longRegIndex, (Integer) values[i]);
                        } else if (values[i] instanceof Long) {
                            bStruct.setIntField(longRegIndex, (Long) values[i]);
                        }
                    }
                    break;
                case TypeTags.FLOAT_TAG:
                    ++doubleRegIndex;
                    if (values[i] != null) {
                        if (values[i] instanceof Float) {
                            bStruct.setFloatField(doubleRegIndex, (Float) values[i]);
                        } else if (values[i] instanceof Double) {
                            bStruct.setFloatField(doubleRegIndex, (Double) values[i]);
                        }
                    }
                    break;
                case TypeTags.STRING_TAG:
                    ++stringRegIndex;
                    if (values[i] != null && values[i] instanceof String) {
                        bStruct.setStringField(stringRegIndex, (String) values[i]);
                    }
                    break;
                case TypeTags.BOOLEAN_TAG:
                    ++booleanRegIndex;
                    if (values[i] != null && values[i] instanceof Boolean) {
                        bStruct.setBooleanField(booleanRegIndex, (Boolean) values[i] ? 1 : 0);
                    }
                    break;
                default:
                    ++refRegIndex;
                    if (values[i] != null && (values[i] instanceof BRefType)) {
                        bStruct.setRefField(refRegIndex, (BRefType) values[i]);
                    }
            }
        }
        return bStruct;
    }

    public static String getPrintableStackTrace(BStruct error) {
        StringBuilder sb = new StringBuilder();

        // Get error type name and the message (if any)
        String errorMsg = getErrorMessage(error);
        sb.append(errorMsg).append("\n");

        BStruct stackTrace = error.getStackTrace();
        BRefValueArray stackTraceItems = (BRefValueArray) stackTrace.getRefField(0);
        for (long i = 0; i < stackTraceItems.size(); i++) {
            sb.append("\tat ");
            BStruct item = (BStruct) stackTraceItems.get(i);

            // Append function/action/resource name with package path (if any)
            if (item.getStringField(1).isEmpty()) {
                sb.append(item.getStringField(0));
            } else {
                sb.append(item.getStringField(1)).append(":").append(item.getStringField(0));
            }

            // Append the filename
            sb.append("(").append(item.getStringField(2));

            // Append the line number
            sb.append(":").append(item.getIntField(0)).append(")");

            // Do not append new line char if this is the last item
            if (i != stackTraceItems.size() - 1) {
                sb.append("\n");
            }
        }

        if (error.getRefField(0) != null) {
            sb.append("\n\t caused by ").append(getPrintableStackTrace((BStruct) error.getRefField(0)));
        }

        return sb.toString();
    }

    public static String getErrorMessage(BStruct error) {
        String errorMsg = error.getType().getName();
        if (error.getType().getPackagePath() != null && !error.getType().getPackagePath().equals(".")) {
            errorMsg = error.getType().getPackagePath() + ":" + errorMsg;
        }

        String msg = error.getStringField(0);
        if (msg != null && !msg.isEmpty()) {
            errorMsg = errorMsg + ", message: " + makeFirstLetterLowerCase(msg);
        }

        return errorMsg;
    }

    private static String makeFirstLetterLowerCase(String s) {
        if (s == null) {
            return null;
        }
        char c[] = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}

