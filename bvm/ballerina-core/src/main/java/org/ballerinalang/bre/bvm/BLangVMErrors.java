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
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BError;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private static final String NULL_REF_EXCEPTION = "NullReferenceException";
    public static final String STRUCT_CALL_STACK_ELEMENT = "CallStackElement";
    public static final String TRANSACTION_ERROR = "TransactionError";
    public static final String ERROR_MESSAGE_FIELD = "message";
    public static final String STACK_FRAME_CALLABLE_NAME = "callableName";
    public static final String STACK_FRAME_PACKAGE_NAME = "moduleName";
    public static final String STACK_FRAME_FILE_NAME = "fileName";
    public static final String STACK_FRAME_LINE_NUMBER = "lineNumber";

    /**
     * Create BError value with given reason string.
     *
     * @param reason error reason
     * @return BError instance
     */
    public static BError createError(String reason) {
        return generateError(null, null, false, reason);
    }

    /**
     * Create BError value.
     *
     * @param reason error reason
     * @param detail error details
     * @return BError instance
     */
    public static BError createError(String reason, BMap<String, BValue> detail) {
        return generateError(null, null, false, BTypes.typeError, reason, detail);
    }

    /**
     * Create BError value.
     *
     * @param errorType BError type
     * @param reason    error reason
     * @return BError instance
     */
    public static BError createError(BErrorType errorType, String reason) {
        return generateError(null, null, false, errorType, reason, null);
    }

    /**
     * Create BError value.
     *
     * @param errorType BError type
     * @param reason    error reason
     * @param detail    error detail
     * @return BError instance
     */
    public static BError createError(BErrorType errorType, String reason, BMap<String, BValue> detail) {
        return generateError(null, null, false, errorType, reason, detail);
    }

    /**
     * Create error Struct from given error reason.
     *
     * @param context current Context
     * @param reason error reason
     * @return generated error
     */
    public static BError createError(Context context, String reason) {
        return createError(context, true, reason);
    }

    public static BError createError(WorkerExecutionContext context, String message) {
        return generateError(context, true, message);
    }

    /**
     * Create error Struct from given reason.
     *
     * @param context         current Context
     * @param attachCallStack attach Call Stack
     * @param reason         error reason
     * @return generated error
     */
    public static BError createError(Context context, boolean attachCallStack, String reason) {
        return generateError(context.getProgramFile(), context.getCallableUnitInfo(), attachCallStack, reason);
    }

    public static BError createError(Context context, boolean attachCallStack, BErrorType errorType, String reason,
            BMap<String, BValue> details) {
        return generateError(context.getProgramFile(), context.getCallableUnitInfo(), attachCallStack, errorType,
                reason, details);
    }

    /* Custom errors messages */

    public static BError createTypeCastError(WorkerExecutionContext context, String sourceType, 
            String targetType) {
        String errorMessage = "'" + sourceType + "' cannot be cast to '" + targetType + "'";
        return createError(context, errorMessage);
    }

    public static BError createTypeConversionError(WorkerExecutionContext context, String errorMessage) {
        return createError(context, errorMessage);
    }

    /* Type Specific Errors */

    static BError createNullRefException(Context context) {
        return generateError(context.getProgramFile(), context.getCallableUnitInfo(), true, NULL_REF_EXCEPTION);
    }

    public static BError createNullRefException(WorkerExecutionContext context) {
        return generateError(context, true, NULL_REF_EXCEPTION);
    }

    static BError handleError(WorkerExecutionContext context, Map<String, BError> errors) {
        if (!errors.isEmpty()) {
            BError error = errors.computeIfAbsent("default", key -> errors.values().iterator().next());
            attachStackFrame(error, context);
            return error;
        }
        return generateError(context, true, MSG_CALL_FAILED);
    }

    static BError createCallCancelledException(WorkerExecutionContext context) {
        return generateError(context, true, MSG_CALL_CANCELLED);
    }

    /* Private Util Methods */

    private static BError generateError(WorkerExecutionContext context, boolean attachCallStack, String reason) {
        BMap<String, BValue> details = new BMap<>(BTypes.typeMap);
        return generateError(context, attachCallStack, BTypes.typeError, reason, details);
    }

    private static BError generateError(WorkerExecutionContext context, boolean attachCallStack, BErrorType type,
            String reason, BMap<String, BValue> details) {
        BError error = new BError(type, reason, details);
        if (attachCallStack) {
            attachStackFrame(error, context);
        }
        return error;
    }

    private static BError generateError(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
            boolean attachCallStack, String reason) {
        return generateError(programFile, callableUnitInfo, attachCallStack, BTypes.typeError, reason, null);
    }

    private static BError generateError(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
            boolean attachCallStack, String reason, BMap<String, BValue> details) {
        return generateError(programFile, callableUnitInfo, attachCallStack, BTypes.typeError, reason, details);
    }

    private static BError generateError(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
            boolean attachCallStack, BErrorType type, String reason, BMap<String, BValue> details) {
        BMap<String, BValue> detailMap = Optional.ofNullable(details).orElse(new BMap<>(BTypes.typeMap));
        BError error = new BError(type, Optional.ofNullable(reason).orElse(""), detailMap);
        if (attachCallStack) {
            attachStackFrame(programFile, error, callableUnitInfo);
        }
        return error;
    }

    public static void attachStackFrame(BError error, WorkerExecutionContext context) {
        Optional.ofNullable(getStackFrame(context)).ifPresent(error.callStack::add);
    }

    public static void attachStackFrame(ProgramFile programFile, BError error,
                                        CallableUnitInfo callableUnitInfo) {
        Optional.ofNullable(getStackFrame(programFile, callableUnitInfo, 0)).ifPresent(error.callStack::add);
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

    public static String getPrintableStackTrace(BError error) {
        StringBuilder sb = new StringBuilder();

        // Get error type name and the message (if any)
        String errorMsg = getErrorMessage(error);
        sb.append(errorMsg).append("\n\tat ");

        List<BMap<String, BValue>> stackFrames = error.callStack;
        // Append function/action/resource name with package path (if any)
        for (int i = stackFrames.size() - 1; i >= 0; i--) {
            BMap<String, BValue> stackFrame = stackFrames.get(i);
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
            if (i != 0) {
                sb.append("\n\t   ");
            }
        }
        return sb.toString();
    }

    private static String getErrorMessage(BError error) {
        // No longer consider nominal error type, just the reason and details.

        String errorMsg = "";
        boolean reasonAdded = false;
        if (error.reason != null && !error.reason.isEmpty()) {
            errorMsg = removeJava(error.reason);
            reasonAdded = true;
        }

        if (error.details != null) {
            errorMsg = errorMsg + (reasonAdded ? " " : "") + error.details.toString();
        }

        return errorMsg;
    }

    private static String removeJava(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("java", "runtime");
    }
}
