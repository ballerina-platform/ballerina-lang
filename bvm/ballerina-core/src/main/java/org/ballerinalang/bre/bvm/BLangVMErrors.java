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

import java.util.Map;
import java.util.stream.Stream;

/**
 * Util Class for handling Error in Ballerina VM.
 *
 * @since 0.88
 */
public class BLangVMErrors {

    public static final String PACKAGE_BUILTIN = "ballerina.builtin";
    private static final String PACKAGE_RUNTIME = "ballerina.runtime";
    public static final String STRUCT_GENERIC_ERROR = "error";
    private static final String STRUCT_NULL_REF_EXCEPTION = "NullReferenceException";
    private static final String STRUCT_ILLEGAL_STATE_EXCEPTION = "IllegalStateException";
    private static final String STRUCT_CALL_STACK_ELEMENT = "CallStackElement";
    public static final String CALL_STACK = "CallStack";
    private static final String STRUCT_CALL_FAILED_EXCEPTION = "CallFailedException";

    /**
     * Create error Struct from given error message.
     *
     * @param context current Context
     * @param ip current instruction pointer
     * @param message error message
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BStruct createError(Context context, int ip, String message) {
        return createError(context, ip, true, message);
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
     * @param ip              current instruction pointer
     * @param attachCallStack attach Call Stack
     * @param message         error message
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BStruct createError(Context context, int ip, boolean attachCallStack, String message) {
        return createError(context, ip, attachCallStack, message, null);
    }

    /**
     * Create error Struct from given error message and cause.
     *
     * @param context         current Context
     * @param ip              current instruction pointer
     * @param attachCallStack attach Call Stack
     * @param message         error message
     * @param cause           caused error struct
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BStruct createError(Context context, int ip, boolean attachCallStack, String message, 
            BStruct cause) {
        return generateError(context.getProgramFile(), ip, attachCallStack, message, cause);
    }

    /**
     * Create an error Struct from given struct type and message.
     *
     * @param context         current Context
     * @param ip              current instruction pointer
     * @param attachCallStack attach Call Stack
     * @param errorType       error struct type
     * @param values          values of the error
     * @return generated error struct
     */
    public static BStruct createError(Context context, int ip, boolean attachCallStack, StructInfo errorType,
                                      Object... values) {
        return generateError(context.getProgramFile(), ip, attachCallStack, errorType, values);
    }

    /* Custom errors messages */

    /**
     * Create TypeCastError.
     *
     * @param context current Context
     * @param sourceType For which error happened
     * @param targetType For which error happened
     * @return created NullReferenceError
     */
    public static BStruct createTypeCastError(WorkerExecutionContext context, String sourceType, 
            String targetType) {
        String errorMessage = "'" + sourceType + "' cannot be cast to '" + targetType + "'";
        return createError(context, errorMessage);
    }

    /**
     * Create TypeConversionError.
     *
     * @param context current Context
     * @param errorMessage error message
     * @return created TypeConversionError
     */
    public static BStruct createTypeConversionError(WorkerExecutionContext context, String errorMessage) {
        return createError(context, errorMessage);
    }

    /* Type Specific Errors */

    /**
     * Create NullReferenceException.
     *
     * @param context current Context
     * @param ip      current instruction pointer
     * @return created NullReferenceException
     */
    public static BStruct createNullRefException(Context context, int ip) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_NULL_REF_EXCEPTION);
        return generateError(context.getProgramFile(), ip, true, errorStructInfo, "");
    }

    public static BStruct createNullRefException(WorkerExecutionContext context) {
        PackageInfo errorPackageInfo = context.programFile.getPackageInfo(PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_NULL_REF_EXCEPTION);
        return generateError(context.programFile, context, context.ip, true, errorStructInfo);
    }

    public static BStruct createNullRefException(CallableUnitInfo callableUnitInfo) {
        ProgramFile progFile = callableUnitInfo.getPackageInfo().getProgramFile();
        PackageInfo errorPackageInfo = progFile.getPackageInfo(PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_NULL_REF_EXCEPTION);
        return generateError(progFile, callableUnitInfo, true, errorStructInfo);
    }

    public static BStruct createCallFailedException(WorkerExecutionContext context, Map<String, BStruct> errors) {
        PackageInfo errorPackageInfo = context.programFile.getPackageInfo(PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_CALL_FAILED_EXCEPTION);
        return generateError(context.programFile, context, context.ip, true, errorStructInfo,
                Stream.concat(Stream.of(""), errors.values().stream()).toArray(Object[]::new));
    }

    /**
     * Create IllegalStateException.
     *
     * @param context current Context
     * @param ip      current instruction pointer
     * @param msg     message of the exception
     * @return created IllegalStateException
     */
    public static BStruct createIllegalStateException(Context context, int ip, String msg) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_RUNTIME);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_ILLEGAL_STATE_EXCEPTION);
        return createError(context, ip, true, errorStructInfo, msg);
    }

    /* Private Util Methods */

    /**
     * Generate Error from current error.
     *
     * @param context         current Context
     * @param ip              current instruction pointer
     * @param attachCallStack attach call stack to the error struct
     * @param values          field values of the error struct.
     * @return generated error {@link BStruct}
     */
    private static BStruct generateError(ProgramFile progFile, int ip, boolean attachCallStack, Object... values) {
        PackageInfo errorPackageInfo = progFile.getPackageInfo(PACKAGE_BUILTIN);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        return generateError(progFile, null, ip, attachCallStack, errorStructInfo, values);
    }

    private static BStruct generateError(WorkerExecutionContext context, boolean attachCallStack, Object... values) {
        PackageInfo errorPackageInfo = context.programFile.getPackageInfo(PACKAGE_BUILTIN);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        return generateError(context.programFile, context, context.ip, attachCallStack, errorStructInfo, values);
    }

    private static BStruct generateError(CallableUnitInfo callableUnitInfo, boolean attachCallStack,
                                         Object... values) {
        ProgramFile progFile = callableUnitInfo.getPackageInfo().getProgramFile();
        PackageInfo errorPackageInfo = progFile.getPackageInfo(PACKAGE_BUILTIN);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        return generateError(progFile, callableUnitInfo, attachCallStack, errorStructInfo, values);
    }

    /**
     * Generate Error from current error.
     *
     * @param progFile        Program fle
     * @param ip              current instruction pointer
     * @param attachCallStack attach call stack to the error struct
     * @param structInfo      {@link StructInfo} of the error that need to be generated
     * @param values          field values of the error struct.
     * @return generated error {@link BStruct}
     */
    private static BStruct generateError(ProgramFile progFile, WorkerExecutionContext context, int ip,
                                         boolean attachCallStack, StructInfo structInfo, Object... values) {
        BStruct error = BLangVMStructs.createBStruct(structInfo, values);
        if (attachCallStack) {
            attachStackFrame(error, progFile, context, ip);
        }
        return error;
    }

    private static BStruct generateError(ProgramFile progFile, CallableUnitInfo callableUnitInfo,
                                         boolean attachCallStack, StructInfo structInfo, Object... values) {
        BStruct error = BLangVMStructs.createBStruct(structInfo, values);
        if (attachCallStack) {
            attachStackFrame(error, progFile, callableUnitInfo);
        }
        return error;
    }

    /**
     * Attach CallStack to given Error Struct.
     *
     * @param error error struct
     * @param progFile Program file
     * @param ip current instruction pointer
     */
    public static void attachStackFrame(BStruct error, ProgramFile progFile, WorkerExecutionContext context, int ip) {
        error.addNativeData(STRUCT_CALL_STACK_ELEMENT, getStackFrame(progFile, context, ip));
    }

    public static void attachStackFrame(BStruct error, ProgramFile progFile, CallableUnitInfo callableUnitInfo) {
        error.addNativeData(STRUCT_CALL_STACK_ELEMENT, getStackFrame(progFile, callableUnitInfo, -1));
    }

    /**
     * Generate StackTraceItem array.
     *  
     * @param progFile Program file
     * @param ip current instruction pointer
     * @return generated StackTraceItem struct array
     */
    public static BRefValueArray generateCallStack(ProgramFile progFile, WorkerExecutionContext context, int ip) {

        // TODO: attach the entire call stack going backwards to parent context

        BRefValueArray callStack = new BRefValueArray();
        PackageInfo runtimePackage = progFile.getPackageInfo(PACKAGE_RUNTIME);
        StructInfo callStackElement = runtimePackage.getStructInfo(STRUCT_CALL_STACK_ELEMENT);

        int currentIP = ip - 1;
        Object[] values;
        int stackTraceLocation = 0;
        if (context == null) {
            return callStack;
        }
        values = new Object[4];
        CallableUnitInfo callableUnitInfo = context.callableUnitInfo;
        if (callableUnitInfo == null) {
            return callStack;
        }

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

        callStack.add(stackTraceLocation, BLangVMStructs.createBStruct(callStackElement, values));
        return callStack;
    }

    public static BStruct getStackFrame(ProgramFile progFile, CallableUnitInfo callableUnitInfo, int ip) {
        PackageInfo runtimePackage = progFile.getPackageInfo(PACKAGE_RUNTIME);
        StructInfo callStackElement = runtimePackage.getStructInfo(STRUCT_CALL_STACK_ELEMENT);

        int currentIP = ip - 1;
        Object[] values;
        values = new Object[4];
        if (callableUnitInfo == null) {
            return null;
        }

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

    public static BStruct getStackFrame(ProgramFile progFile, WorkerExecutionContext context, int ip) {
        if (context == null) {
            return null;
        }
        return getStackFrame(progFile, context.callableUnitInfo, ip);
    }

    public static String getPrintableStackTrace(BStruct error) {
        BStruct cause = (BStruct) error.getRefField(0);

        // Skip printing the first callFailed error. Because its the entry point call (main function, service
        // invocation) and its a call made by ballerina VM internally.
        if (cause != null) {
            return getCasueStackTrace(cause);
        }

        return null;
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

        BStruct cause = (BStruct) error.getRefField(0);
        if (cause != null) {
            sb.append("\ncaused by ").append(getCasueStackTrace(cause));
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
}
