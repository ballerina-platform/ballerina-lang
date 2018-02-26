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
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.StructInfo;

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


    /**
     * Create error Struct from given error message.
     *
     * @param context current Context
     * @param ip      current instruction pointer
     * @param message error message
     * @return generated ballerina.lang.errors:Error struct
     */
    public static BStruct createError(Context context, int ip, String message) {
        return createError(context, ip, true, message);
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
    public static BStruct createError(Context context, int ip, boolean attachCallStack, String message, BStruct cause) {
        return generateError(context, ip, attachCallStack, message, cause);
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
        return generateError(context, ip, attachCallStack, errorType, values);
    }

    /* Custom errors messages */

    /**
     * Create TypeCastError.
     *
     * @param context    current Context
     * @param ip         current instruction pointer
     * @param sourceType For which error happened
     * @param targetType For which error happened
     * @return created NullReferenceError
     */
    public static BStruct createTypeCastError(Context context, int ip, String sourceType, String targetType) {
        String errorMsg = "'" + sourceType + "' cannot be cast to '" + targetType + "'";
        return createError(context, ip, false, errorMsg);
    }

    /**
     * Create TypeConversionError.
     *
     * @param context        current Context
     * @param ip             current instruction pointer
     * @param errorMessage   error message
     * @return created TypeConversionError
     */
    public static BStruct createTypeConversionError(Context context, int ip, String errorMessage) {
        return createError(context, ip, false, errorMessage);
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
        return createError(context, ip, true, errorStructInfo, "");
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
    private static BStruct generateError(Context context, int ip, boolean attachCallStack, Object... values) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_BUILTIN);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        return generateError(context, ip, attachCallStack, errorStructInfo, values);
    }

    /**
     * Generate Error from current error.
     *
     * @param context         current Context
     * @param ip              current instruction pointer
     * @param attachCallStack attach call stack to the error struct
     * @param structInfo      {@link StructInfo} of the error that need to be generated
     * @param values          field values of the error struct.
     * @return generated error {@link BStruct}
     */
    private static BStruct generateError(Context context, int ip, boolean attachCallStack, StructInfo structInfo,
                                         Object... values) {
        BStruct error = BLangVMStructs.createBStruct(structInfo, values);
        if (attachCallStack) {
            attachCallStack(error, context, ip);
        }
        return error;
    }


    /**
     * Attach CallStack to given Error Struct.
     *
     * @param error   error struct
     * @param context current Context
     * @param ip      current instruction pointer
     */
    public static void attachCallStack(BStruct error, Context context, int ip) {
        error.addNativeData(CALL_STACK, generateCallStack(context, ip));
    }

    /**
     * Generate StackTraceItem array.
     *
     * @param context current Context
     * @param ip      current instruction pointer
     * @return generated StackTraceItem struct array
     */
    public static BRefValueArray generateCallStack(Context context, int ip) {
        BRefValueArray callStack = new BRefValueArray();
        PackageInfo runtimePackage = context.getProgramFile().getPackageInfo(PACKAGE_RUNTIME);
        StructInfo callStackElement = runtimePackage.getStructInfo(STRUCT_CALL_STACK_ELEMENT);
        ControlStack controlStack = context.getControlStack();

        int currentIP = ip - 1;
        Object[] values;
        int stackTraceLocation = 0;
        StackFrame stackFrame = controlStack.currentFrame;
        while (stackFrame != null) {
            values = new Object[4];
            CallableUnitInfo callableUnitInfo = stackFrame.callableUnitInfo;
            if (callableUnitInfo == null) {
                stackFrame = stackFrame.prevStackFrame;
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
            // Always get the previous instruction pointer.
            currentIP = stackFrame.retAddrs - 1;
            stackTraceLocation++;
            stackFrame = stackFrame.prevStackFrame;
        }
        return callStack;
    }

    public static String getPrintableStackTrace(BStruct error) {
        StringBuilder sb = new StringBuilder();

        // Get error type name and the message (if any)
        String errorMsg = getErrorMessage(error);
        sb.append(errorMsg).append("\n");

        BRefValueArray stackFrame = (BRefValueArray) error.getNativeData(CALL_STACK);
        if (stackFrame == null) {
            return errorMsg;
        }
        for (long i = 0; i < stackFrame.size(); i++) {
            sb.append("\tat ");
            BStruct item = (BStruct) stackFrame.get(i);

            // Append function/action/resource name with package path (if any)
            if (item.getStringField(1).isEmpty() || item.getStringField(1).equals(PACKAGE_BUILTIN)) {
                sb.append(item.getStringField(0));
            } else {
                sb.append(item.getStringField(1)).append(":").append(item.getStringField(0));
            }

            // Append the filename
            sb.append("(").append(item.getStringField(2));

            // Append the line number
            if (item.getIntField(0) > 0) {
                sb.append(":").append(item.getIntField(0));
            }
            sb.append(")");
            // Do not append new line char if this is the last item
            if (i != stackFrame.size() - 1) {
                sb.append("\n");
            }
        }

        if (error.getRefField(0) != null) {
            sb.append("\n\t caused by ").append(getPrintableStackTrace((BStruct) error.getRefField(0)));
        }

        return sb.toString();
    }

    private static String getErrorMessage(BStruct error) {
        String errorMsg = error.getType().getName();
        if (error.getType().getPackagePath() != null && !error.getType().getPackagePath().equals(".")
                && !error.getType().getPackagePath().equals(PACKAGE_BUILTIN)) {
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

