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
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.ActionInfo;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.StructInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Util Class for handling Error in Ballerina VM.
 *
 * @since 0.88
 */
public class BLangVMErrors {

    private static final String MSG_CALL_FAILED = "call failed";
    private static final String MSG_CALL_CANCELLED = "call cancelled";
    public static final String PACKAGE_BUILTIN = "ballerina.builtin";
    private static final String PACKAGE_RUNTIME = "ballerina.runtime";
    public static final String STRUCT_GENERIC_ERROR = "error";
    private static final String STRUCT_NULL_REF_EXCEPTION = "NullReferenceException";
    private static final String STRUCT_ILLEGAL_STATE_EXCEPTION = "IllegalStateException";
    public static final String STRUCT_CALL_STACK_ELEMENT = "CallStackElement";
    private static final String STRUCT_CALL_FAILED_EXCEPTION = "CallFailedException";
    public static final String TRANSACTION_ERROR = "TransactionError";

    /**
     * Create error Struct from given error message.
     *
     * @param context current Context
     * @param message error message
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BStruct createError(Context context, String message) {
        return createError(context, true, message);
    }
    
    public static BStruct createError(Context context, int ip, String message) {
        return createError(context, true, message);
    }

    public static BStruct createError(WorkerExecutionContext context, String message) {
        return generateError(context, true, message);
    }

    public static BStruct createError(CallableUnitInfo callableUnitInfo, String message) {
        return generateError(callableUnitInfo, true, message);
    }

    /**
     * Create error Struct from given message.
     *
     * @param context         current Context
     * @param attachCallStack attach Call Stack
     * @param message         error message
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BStruct createError(Context context, boolean attachCallStack, String message) {
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
    public static BStruct createError(Context context, boolean attachCallStack, String message, 
            BStruct cause) {
        return generateError(context.getCallableUnitInfo(), attachCallStack, message, cause);
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
    public static BStruct createError(Context context, boolean attachCallStack, StructInfo errorType,
                                      Object... values) {
        return generateError(context.getCallableUnitInfo(), attachCallStack, errorType, values);
    }

    /* Custom errors messages */

    public static BStruct createTypeCastError(WorkerExecutionContext context, String sourceType, 
            String targetType) {
        String errorMessage = "'" + sourceType + "' cannot be cast to '" + targetType + "'";
        return createError(context, errorMessage);
    }

    public static BStruct createTypeConversionError(WorkerExecutionContext context, String errorMessage) {
        return createError(context, errorMessage);
    }

    /* Type Specific Errors */

    public static BStruct createNullRefException(Context context) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_NULL_REF_EXCEPTION);
        return generateError(context.getCallableUnitInfo(), true, errorStructInfo, "");
    }

    public static BStruct createNullRefException(WorkerExecutionContext context) {
        PackageInfo errorPackageInfo = context.programFile.getPackageInfo(PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_NULL_REF_EXCEPTION);
        return generateError(context, true, errorStructInfo);
    }

    public static BStruct createNullRefException(CallableUnitInfo callableUnitInfo) {
        ProgramFile progFile = callableUnitInfo.getPackageInfo().getProgramFile();
        PackageInfo errorPackageInfo = progFile.getPackageInfo(PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_NULL_REF_EXCEPTION);
        return generateError(callableUnitInfo, true, errorStructInfo);
    }

    public static BStruct createCallFailedException(WorkerExecutionContext context, Map<String, BStruct> errors) {
        PackageInfo errorPackageInfo = context.programFile.getPackageInfo(PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_CALL_FAILED_EXCEPTION);
        return generateError(context, true, errorStructInfo, MSG_CALL_FAILED, null, createErrorCauseArray(errors));
    }
    
    public static BStruct createCallCancelledException(CallableUnitInfo callableUnitInfo) {
        PackageInfo errorPackageInfo = callableUnitInfo.getPackageInfo().getProgramFile().getPackageInfo(
                PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_CALL_FAILED_EXCEPTION);
        return generateError(callableUnitInfo, true, errorStructInfo, MSG_CALL_CANCELLED);
    }

    public static BStruct createIllegalStateException(Context context, String msg) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_ILLEGAL_STATE_EXCEPTION);
        return createError(context, true, errorStructInfo, msg);
    }

    /* Private Util Methods */
    
    private static BRefValueArray createErrorCauseArray(Map<String, BStruct> errors) {
        BRefValueArray result = new BRefValueArray();
        long i = 0;
        for (BStruct entry : errors.values()) {
            result.add(i, entry);
            i++;
        }
        return result;
    }

    private static BStruct generateError(WorkerExecutionContext context, boolean attachCallStack, Object... values) {
        PackageInfo errorPackageInfo = context.programFile.getPackageInfo(PACKAGE_BUILTIN);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        return generateError(context, attachCallStack, errorStructInfo, values);
    }

    private static BStruct generateError(CallableUnitInfo callableUnitInfo, boolean attachCallStack,
                                         Object... values) {
        ProgramFile progFile = callableUnitInfo.getPackageInfo().getProgramFile();
        PackageInfo errorPackageInfo = progFile.getPackageInfo(PACKAGE_BUILTIN);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        return generateError(callableUnitInfo, attachCallStack, errorStructInfo, values);
    }

    private static BStruct generateError(WorkerExecutionContext context,
                                         boolean attachCallStack, StructInfo structInfo, Object... values) {
        BStruct error = BLangVMStructs.createBStruct(structInfo, values);
        if (attachCallStack) {
            attachStackFrame(error, context);
        }
        return error;
    }

    private static BStruct generateError(CallableUnitInfo callableUnitInfo, boolean attachCallStack, 
            StructInfo structInfo, Object... values) {
        BStruct error = BLangVMStructs.createBStruct(structInfo, values);
        if (attachCallStack) {
            attachStackFrame(error, callableUnitInfo);
        }
        return error;
    }

    public static void attachStackFrame(BStruct error, WorkerExecutionContext context) {
        error.addNativeData(STRUCT_CALL_STACK_ELEMENT, getStackFrame(context));
    }

    public static void attachStackFrame(BStruct error, CallableUnitInfo callableUnitInfo) {
        error.addNativeData(STRUCT_CALL_STACK_ELEMENT, getStackFrame(callableUnitInfo, 0));
    }

    public static BRefValueArray generateCallStack(WorkerExecutionContext context, CallableUnitInfo nativeCUI) {
        BRefValueArray callStack = new BRefValueArray();
        long index = 0;
        if (nativeCUI != null) {
            callStack.add(index, getStackFrame(nativeCUI, 0));
            index++;
        }
        while (!context.isRootContext()) {
            callStack.add(index, getStackFrame(context));
            context = context.parent;
            index++;
        }
        return callStack;
    }

    public static BStruct getStackFrame(CallableUnitInfo callableUnitInfo, int ip) {
        if (callableUnitInfo == null) {
            return null;
        }
        
        ProgramFile progFile = callableUnitInfo.getPackageInfo().getProgramFile();
        PackageInfo runtimePackage = progFile.getPackageInfo(PACKAGE_RUNTIME);
        StructInfo callStackElement = runtimePackage.getStructInfo(STRUCT_CALL_STACK_ELEMENT);

        int currentIP = ip - 1;
        Object[] values;
        values = new Object[4];

        String parentScope = "";
        if (callableUnitInfo instanceof ResourceInfo) {
            parentScope = ((ResourceInfo) callableUnitInfo).getServiceInfo().getName() + ".";
        } else if (callableUnitInfo instanceof ActionInfo) {
            parentScope = ((ActionInfo) callableUnitInfo).getConnectorInfo().getName() + ".";
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

        return BLangVMStructs.createBStruct(callStackElement, values);
    }

    public static BStruct getStackFrame(WorkerExecutionContext context) {
        if (context == null) {
            return null;
        }
        return getStackFrame(context.callableUnitInfo, context.ip);
    }
    
    private static boolean isCFE(BStruct error) {
        return error.getType().getName().equals(STRUCT_CALL_FAILED_EXCEPTION);
    }
    
    public static String getPrintableStackTrace(BStruct error) {
        BRefValueArray causeArray = null;
        BStruct causeStruct = null;
        if (isCFE(error)) {
            causeArray = (BRefValueArray) error.getRefField(1);
        } else {
            causeStruct = (BStruct) error.getRefField(0);
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
            sb.append(getCasueStackTrace((BStruct) cause.get(i)) + "\n");
        }
        return sb.toString();
    }

    public static String getCasueStackTrace(BStruct error) {
        StringBuilder sb = new StringBuilder();

        // Get error type name and the message (if any)
        String errorMsg = getErrorMessage(error);
        sb.append(errorMsg).append("\n\tat ");

        BStruct stackFrame = (BStruct) error.getNativeData(STRUCT_CALL_STACK_ELEMENT);
        // Append function/action/resource name with package path (if any)
        if (stackFrame.getStringField(1).isEmpty() || stackFrame.getStringField(1).equals(PACKAGE_BUILTIN)) {
            sb.append(stackFrame.getStringField(0));
        } else {
            sb.append(stackFrame.getStringField(1)).append(":").append(stackFrame.getStringField(0));
        }

        // Append the filename
        sb.append("(").append(stackFrame.getStringField(2));

        // Append the line number
        if (stackFrame.getIntField(0) > 0) {
            sb.append(":").append(stackFrame.getIntField(0));
        }
        sb.append(")");

        if (isCFE(error)) {
            BRefValueArray cause = (BRefValueArray) error.getRefField(1);
            if (cause != null && cause.size() > 0) {
                sb.append("\ncaused by ").append(getCauseStackTraceArray(cause));
            }
        } else {
            BStruct cause = (BStruct) error.getRefField(0);
            if (cause != null) {
                sb.append("\ncaused by ").append(getCasueStackTrace(cause));
            }
        }

        return sb.toString();
    }

    private static String getErrorMessage(BStruct error) {
        String errorMsg = error.getType().getName();
        if (error.getType().getPackagePath() != null && !error.getType().getPackagePath().equals(".") &&
                !error.getType().getPackagePath().equals(PACKAGE_BUILTIN)) {
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
    
    public static String getAggregatedRootErrorMessages(BStruct error) {
        BRefValueArray causesArray = (BRefValueArray) error.getRefField(0);
        if (causesArray != null && causesArray.size() > 0) {
            List<String> messages = new ArrayList<>();
            for (int i = 0; i < causesArray.size(); i++) {
                messages.add(getAggregatedRootErrorMessages((BStruct) causesArray.get(i)));
            }
            return String.join(", ", messages.toArray(new String[0]));
        } else {
            return error.getStringField(0);
        }
    }
    
}
