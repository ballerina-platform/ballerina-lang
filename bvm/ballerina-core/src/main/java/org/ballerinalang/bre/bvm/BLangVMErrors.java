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
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.util.Map;

import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;
import static org.ballerinalang.util.BLangConstants.BALLERINA_RUNTIME_PKG;

/**
 * Util Class for handling Error in Ballerina VM.
 *
 * @since 0.88
 */
public class BLangVMErrors {

    private static final String DEFAULT_PKG_PATH = ".";
    private static final String MSG_CALL_FAILED = "call failed";
    private static final String MSG_CALL_CANCELLED = "call cancelled";
    public static final String STRUCT_GENERIC_ERROR = "error";
    private static final String STRUCT_NULL_REF_EXCEPTION = "NullReferenceException";
    public static final String STRUCT_CALL_STACK_ELEMENT = "CallStackElement";
    public static final String STRUCT_CALL_FAILED_EXCEPTION = "CallFailedException";
    public static final String TRANSACTION_ERROR = "TransactionError";
    public static final String ERROR_MESSAGE_FIELD = "message";
    public static final String ERROR_CAUSE_FIELD = "cause";
    public static final String ERROR_CAUSE_ARRAY_FIELD = "causes";
    public static final String STACK_FRAME_CALLABLE_NAME = "callableName";
    public static final String STACK_FRAME_PACKAGE_NAME = "moduleName";
    public static final String STACK_FRAME_FILE_NAME = "fileName";
    public static final String STACK_FRAME_LINE_NUMBER = "lineNumber";
    
    /**
     * Create error Struct from given error message.
     *
     * @param context current Context
     * @param message error message
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BMap<String, BValue> createError(Context context, String message) {
        return createError(context, true, message);
    }

    public static BMap<String, BValue> createError(WorkerExecutionContext context, String message) {
        return generateError(context, true, message);
    }

    /**
     * Create error Struct from given message.
     *
     * @param context         current Context
     * @param attachCallStack attach Call Stack
     * @param message         error message
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BMap<String, BValue> createError(Context context, boolean attachCallStack, String message) {
        return createError(context, attachCallStack, message, null);
    }

    /**
     * Create error Struct from given error message and cause.
     *
     * @param context         current Context
     * @param attachCallStack attach Call Stack
     * @param message         error message
     * @param cause           caused error struct
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BMap<String, BValue> createError(Context context, boolean attachCallStack, String message,
                                                   BMap<String, BValue> cause) {
        return generateError(context.getProgramFile(), context.getCallableUnitInfo(), attachCallStack, message, cause);
    }

    /**
     * Create an error Struct from given struct type and message.
     *
     * @param context         current Context
     * @param attachCallStack attach Call Stack
     * @param errorType       error struct type
     * @param values          values of the error
     * @return generated error struct
     */
    public static BMap<String, BValue> createError(Context context, boolean attachCallStack,
                                                   StructureTypeInfo errorType, Object... values) {
        return generateError(context.getProgramFile(), context.getCallableUnitInfo(), attachCallStack, errorType,
                values);
    }

    /* Custom errors messages */

    public static BMap<String, BValue> createTypeCastError(WorkerExecutionContext context, String sourceType, 
            String targetType) {
        String errorMessage = "'" + sourceType + "' cannot be cast to '" + targetType + "'";
        return createError(context, errorMessage);
    }

    public static BMap<String, BValue> createTypeConversionError(WorkerExecutionContext context, String errorMessage) {
        return createError(context, errorMessage);
    }

    /* Type Specific Errors */

    public static BMap<String, BValue> createNullRefException(Context context) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(BALLERINA_RUNTIME_PKG);
        StructureTypeInfo typeInfo = errorPackageInfo.getStructInfo(STRUCT_NULL_REF_EXCEPTION);
        if (typeInfo == null || typeInfo.getType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new BallerinaConnectorException("record - " + STRUCT_NULL_REF_EXCEPTION + " does not exist");
        }
        return generateError(context.getProgramFile(), context.getCallableUnitInfo(), true, typeInfo, "");
    }

    public static BMap<String, BValue> createNullRefException(WorkerExecutionContext context) {
        PackageInfo errorPackageInfo = context.programFile.getPackageInfo(BALLERINA_RUNTIME_PKG);
        StructureTypeInfo typeInfo = errorPackageInfo.getStructInfo(STRUCT_NULL_REF_EXCEPTION);
        if (typeInfo == null || typeInfo.getType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new BallerinaConnectorException("record - " + STRUCT_NULL_REF_EXCEPTION + " does not exist");
        }
        return generateError(context, true, typeInfo);
    }

    public static BMap<String, BValue> createCallFailedException(WorkerExecutionContext context,
                                                       Map<String, BMap<String, BValue>> errors) {
        PackageInfo errorPackageInfo = context.programFile.getPackageInfo(BALLERINA_RUNTIME_PKG);
        StructureTypeInfo typeInfo = errorPackageInfo.getStructInfo(STRUCT_CALL_FAILED_EXCEPTION);
        if (typeInfo == null || typeInfo.getType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new BallerinaConnectorException("record - " + STRUCT_CALL_FAILED_EXCEPTION + " does not exist");
        }
        return generateError(context, true, typeInfo, MSG_CALL_FAILED,
                errors.isEmpty() ? null : errors.values().iterator().next(), createErrorCauseArray(errors));
    }

    public static BMap<String, BValue> createCallCancelledException(WorkerExecutionContext context) {
        PackageInfo errorPackageInfo = context.programFile.getPackageInfo(BALLERINA_RUNTIME_PKG);
        StructureTypeInfo typeInfo = errorPackageInfo.getStructInfo(STRUCT_CALL_FAILED_EXCEPTION);
        if (typeInfo == null || typeInfo.getType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new BallerinaConnectorException("record - " + STRUCT_CALL_FAILED_EXCEPTION + " does not exist");
        }
        return generateError(context, true, typeInfo, MSG_CALL_CANCELLED);
    }

    /* Private Util Methods */
    
    private static BRefValueArray createErrorCauseArray(Map<String, BMap<String, BValue>> errors) {
        BRefValueArray result = new BRefValueArray();
        long i = 0;
        for (BMap<String, BValue> entry : errors.values()) {
            result.add(i, entry);
            i++;
        }
        return result;
    }

    private static BMap<String, BValue> generateError(WorkerExecutionContext context, boolean attachCallStack,
                                                      Object... values) {
        PackageInfo errorPackageInfo = context.programFile.getPackageInfo(BALLERINA_BUILTIN_PKG);
        StructureTypeInfo typeInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        if (typeInfo == null || typeInfo.getType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new BallerinaConnectorException("record - " + STRUCT_GENERIC_ERROR + " does not exist");
        }
        return generateError(context, attachCallStack, typeInfo, values);
    }

    private static BMap<String, BValue> generateError(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                                         boolean attachCallStack, Object... values) {
        ProgramFile progFile = callableUnitInfo.getPackageInfo().getProgramFile();
        PackageInfo errorPackageInfo = progFile.getPackageInfo(BALLERINA_BUILTIN_PKG);
        StructureTypeInfo typeInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        if (typeInfo == null || typeInfo.getType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new BallerinaConnectorException("record - " + STRUCT_GENERIC_ERROR + " does not exist");
        }
        return generateError(programFile, callableUnitInfo, attachCallStack, typeInfo, values);
    }

    private static BMap<String, BValue> generateError(WorkerExecutionContext context,
                                         boolean attachCallStack, StructureTypeInfo structInfo, Object... values) {
        BMap<String, BValue> error = BLangVMStructs.createBStruct(structInfo, values);
        if (attachCallStack) {
            attachStackFrame(error, context);
        }
        return error;
    }

    private static BMap<String, BValue> generateError(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                                         boolean attachCallStack, StructureTypeInfo structInfo, Object... values) {
        BMap<String, BValue> error = BLangVMStructs.createBStruct(structInfo, values);
        if (attachCallStack) {
            attachStackFrame(programFile, error, callableUnitInfo);
        }
        return error;
    }

    public static void attachStackFrame(BMap<String, BValue> error, WorkerExecutionContext context) {
        error.addNativeData(STRUCT_CALL_STACK_ELEMENT, getStackFrame(context));
    }

    public static void attachStackFrame(ProgramFile programFile, BMap<String, BValue> error,
                                        CallableUnitInfo callableUnitInfo) {
        error.addNativeData(STRUCT_CALL_STACK_ELEMENT, getStackFrame(programFile, callableUnitInfo, 0));
    }

    public static BRefValueArray generateCallStack(WorkerExecutionContext context, CallableUnitInfo nativeCUI) {
        BRefValueArray callStack = new BRefValueArray();
        long index = 0;
        if (nativeCUI != null) {
            callStack.add(index, getStackFrame(context.programFile, nativeCUI, 0));
            index++;
        }
        while (!context.isRootContext()) {
            callStack.add(index, getStackFrame(context));
            context = context.parent;
            index++;
        }
        return callStack;
    }

    public static BMap<String, BValue> getStackFrame(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                                                     int ip) {
        if (callableUnitInfo == null) {
            return null;
        }
        
        PackageInfo runtimePackage = programFile.getPackageInfo(BALLERINA_RUNTIME_PKG);
        StructureTypeInfo typeInfo = runtimePackage.getStructInfo(STRUCT_CALL_STACK_ELEMENT);
        if (typeInfo == null || typeInfo.getType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new BallerinaConnectorException("record - " + STRUCT_CALL_STACK_ELEMENT + " does not exist");
        }

        int currentIP = ip - 1;
        Object[] values;
        values = new Object[4];

        String parentScope = "";
        if (callableUnitInfo instanceof ResourceInfo) {
            parentScope = ((ResourceInfo) callableUnitInfo).getServiceInfo().getName() + ".";
        }

        values[0] = parentScope + callableUnitInfo.getName();
        values[1] = callableUnitInfo.getPkgPath();
        if (callableUnitInfo.isNative()) {
            values[2] = "<native>";
            values[3] = 0;
        } else {
            LineNumberInfo lineNumberInfo = callableUnitInfo.getPackageInfo().getLineNumberInfo(currentIP);
            if (lineNumberInfo != null) {
                values[2] = lineNumberInfo.getFileName();
                values[3] = lineNumberInfo.getLineNumber();
            }
        }

        return BLangVMStructs.createBStruct(typeInfo, values);
    }

    public static BMap<String, BValue> getStackFrame(WorkerExecutionContext context) {
        if (context == null) {
            return null;
        }
        return getStackFrame(context.programFile, context.callableUnitInfo, context.ip);
    }
    
    private static boolean isCFE(BMap<String, BValue> error) {
        return error.getType().getName().equals(STRUCT_CALL_FAILED_EXCEPTION);
    }
    
    public static String getPrintableStackTrace(BMap<String, BValue> error) {
        BRefValueArray causeArray = null;
        BMap<String, BValue> causeStruct = null;
        if (isCFE(error)) {
            causeArray = (BRefValueArray) error.get(BLangVMErrors.ERROR_CAUSE_ARRAY_FIELD);
        } else {
            causeStruct = (BMap<String, BValue>) error.get(BLangVMErrors.ERROR_CAUSE_FIELD);
        }

        /* skip the first call failed error, since it would be the root context that calls the
         * entry point functions (i.e. main etc..). The error at the root context will have all
         * the errors as causes of the entry point function */
        if (causeArray != null) {
            return getCauseStackTraceArray(causeArray);
        } else if (causeStruct != null) {
            return getCasueStackTrace(error);
        }

        return null;
    }
    
    public static String getCauseStackTraceArray(BRefValueArray cause) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cause.size(); i++) {
            sb.append(getCasueStackTrace((BMap<String, BValue>) cause.get(i)) + "\n");
        }
        return sb.toString();
    }

    public static String getCasueStackTrace(BMap<String, BValue> error) {
        StringBuilder sb = new StringBuilder();

        // Get error type name and the message (if any)
        String errorMsg = getErrorMessage(error);
        sb.append(errorMsg).append("\n\tat ");

        BMap<String, BValue> stackFrame = (BMap<String, BValue>) error.getNativeData(STRUCT_CALL_STACK_ELEMENT);
        // Append function/action/resource name with package path (if any)
        String pkgName = stackFrame.get(STACK_FRAME_PACKAGE_NAME).stringValue();
        if (pkgName.isEmpty() || DEFAULT_PKG_PATH.equals(pkgName) || BALLERINA_BUILTIN_PKG.equals(pkgName)) {
            sb.append(stackFrame.get(STACK_FRAME_CALLABLE_NAME).stringValue());
        } else {
            sb.append(pkgName).append(":").append(stackFrame.get(STACK_FRAME_CALLABLE_NAME).stringValue());
        }

        // Append the filename
        sb.append("(").append(stackFrame.get(STACK_FRAME_FILE_NAME).stringValue());

        // Append the line number
        long lineNo = ((BInteger) stackFrame.get(STACK_FRAME_LINE_NUMBER)).intValue();
        if (lineNo > 0) {
            sb.append(":").append(lineNo);
        }
        sb.append(")");

        if (isCFE(error)) {
            BRefValueArray cause = (BRefValueArray) error.get(ERROR_CAUSE_ARRAY_FIELD);
            if (cause != null && cause.size() > 0) {
                sb.append("\ncaused by ").append(getCauseStackTraceArray(cause));
            }
        } else {
            BMap<String, BValue> cause = (BMap<String, BValue>) error.get(ERROR_CAUSE_FIELD);
            if (cause != null) {
                sb.append("\ncaused by ").append(getCasueStackTrace(cause));
            }
        }

        return sb.toString();
    }

    private static String getErrorMessage(BMap<String, BValue> error) {
        String errorMsg = error.getType().getName();
        if (error.getType().getPackagePath() != null && !error.getType().getPackagePath().equals(DEFAULT_PKG_PATH) &&
                !error.getType().getPackagePath().equals(BALLERINA_BUILTIN_PKG)) {
            errorMsg = error.getType().getPackagePath() + ":" + errorMsg;
        }

        BValue msgBVal = error.get(ERROR_MESSAGE_FIELD);
        if (msgBVal == null) {
            return errorMsg;
        }

        String msg = msgBVal.stringValue();
        if (msgBVal != null && !msg.isEmpty()) {
            errorMsg = errorMsg + ", message: " + removeJava(makeFirstLetterLowerCase(msg));
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

    private static String removeJava(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("java", "runtime");
    }
}
