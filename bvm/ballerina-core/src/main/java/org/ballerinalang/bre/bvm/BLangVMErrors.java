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
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;

import java.util.ArrayList;
import java.util.List;
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
    public static final String NULL_REF_EXCEPTION = "NullReferenceException";
    public static final String STRUCT_CALL_STACK_ELEMENT = "CallStackElement";
    public static final String TRANSACTION_ERROR = "TransactionError";
    public static final String ERROR_MESSAGE_FIELD = "message";
    public static final String STACK_FRAME_CALLABLE_NAME = "callableName";
    public static final String STACK_FRAME_PACKAGE_NAME = "moduleName";
    public static final String STACK_FRAME_FILE_NAME = "fileName";
    public static final String STACK_FRAME_LINE_NUMBER = "lineNumber";

    /**
     * Create BError value.
     *
     * @param reason error reason
     * @param detail error details
     * @return BError instance
     */
    public static BError createError(String reason, BMap<String, BValue> detail) {
        return generateError(null, false, BTypes.typeError, reason, detail);
    }

    /**
     * Create error Struct from given error reason.
     *
     * @param context current Context
     * @param reason error reason
     * @return generated error
     */
    public static BError createError(Context context, String reason) {
        return generateError(context.getStrand(), true, BTypes.typeError, reason, null);
    }

    public static BError createError(Strand strand, String message) {
        return generateError(strand, true, BTypes.typeError, message, null);
    }

    public static BError createError(Strand strand, String reason, String detail) {
        BMap<String, BValue> detailMap = new BMap<>(BTypes.typeMap);
        if (detail != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, new BString(detail));
        }
        return generateError(strand, true, BTypes.typeError, reason, detailMap);
    }

    public static BError createError(Context context, boolean attachCallStack, BErrorType errorType, String reason,
            BMap<String, BValue> details) {
        return generateError(context.getStrand(), attachCallStack, errorType, reason, details);
    }

    public static BError createError(Strand strand, boolean attachCallStack, BErrorType errorType, String reason,
            BMap<String, BValue> details) {
        return generateError(strand, attachCallStack, errorType, reason, details);
    }

    /* Custom errors messages */

    public static BError createTypeCastError(Strand context, String sourceType,
            String targetType) {
        String errorMessage = "'" + sourceType + "' cannot be cast to '" + targetType + "'";
        return createError(context, errorMessage);
    }

    public static BError createCancelledFutureError(Strand context) {
        String errorMessage = "future is already cancelled";
        return createError(context, errorMessage);
    }

    public static BError createTypeConversionError(Strand context, String errorMessage) {
        return createError(context, BallerinaErrorReasons.CONVERSION_ERROR, errorMessage);
    }

    /* Type Specific Errors */

    public static BError createNullRefException(Strand strand) {
        return generateError(strand, true, BTypes.typeError, NULL_REF_EXCEPTION, null);
    }

    /* Private Util Methods */

    private static BError generateError(Strand strand, boolean attachCallStack, BErrorType type, String reason,
            BMap<String, BValue> details) {
        BMap<String, BValue> detailMap = Optional.ofNullable(details).orElse(new BMap<>(BTypes.typeMap));
        BError error = new BError(type, Optional.ofNullable(reason).orElse(""), detailMap);
        StructureTypeInfo typeInfo = getStructureTypeInfo(strand.programFile);
        if (attachCallStack) {
            attachStack(error, typeInfo, strand);
        }
        return error;
    }

    public static void attachStack(BError error, StructureTypeInfo typeInfo, Strand strand) {
        for (StackFrame frame : strand.getStack()) {
            Optional.ofNullable(getStackFrame(typeInfo, frame)).ifPresent(sf -> error.callStack.add(0, sf));
        }
    }

    public static BValueArray generateCallStack(ProgramFile programFile, Strand strand) {
        StructureTypeInfo typeInfo = getStructureTypeInfo(programFile);
        List<BMap<String, BValue>> sfList = new ArrayList<>();
        for (StackFrame frame : strand.getStack()) {
            BMap<String, BValue> sf = getStackFrame(typeInfo, frame);
            if (sf != null) {
                sfList.add(0, sf);
            }
        }

        BValueArray callStack = new BValueArray(typeInfo.getType());
        for (int i = 0; i < sfList.size(); i++) {
            callStack.add(i, sfList.get(i));
        }
        return callStack;
    }

    public static BMap<String, BValue> getStackFrame(StructureTypeInfo typeInfo, CallableUnitInfo callableUnitInfo,
                                                     int ip) {
        if (callableUnitInfo == null) {
            return null;
        }

        int currentIP = ip - 1;
        Object[] values;
        values = new Object[4];

        String parentScope = "";

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

    public static BMap<String, BValue> getStackFrame(StructureTypeInfo typeInfo, StackFrame sf) {
        if (sf == null) {
            return null;
        }
        return getStackFrame(typeInfo, sf.callableUnitInfo, sf.ip);
    }

    public static String getPrintableStackTrace(BError error) {
        StringBuilder sb = new StringBuilder();

        // Get error type name and the message (if any)
        String errorMsg = getErrorMessage(error);
        sb.append(errorMsg).append("\n\tat ");

        List<BMap<String, BValue>> stackFrames = error.callStack;
        // Append function/action/resource name with package path (if any)
        for (int i = 0; i < stackFrames.size(); i++) {
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
            if (i != stackFrames.size() - 1) {
                sb.append("\n\t   ");
            }
        }
        return sb.toString();
    }

    public static String getErrorMessage(BError error) {
        // No longer consider nominal error type, just the reason and details.

        String errorMsg = "";
        boolean reasonAdded = false;
        if (error.getReason() != null && !error.getReason().isEmpty()) {
            errorMsg = removeJava(error.getReason());
            reasonAdded = true;
        }

        if (error.getDetails() != null) {
            errorMsg = errorMsg + (reasonAdded ? " " : "") + error.getDetails().toString();
        }

        return errorMsg;
    }

    private static String removeJava(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("java", "runtime");
    }

    private static StructureTypeInfo getStructureTypeInfo(ProgramFile programFile) {

        PackageInfo runtimePackage = programFile.getPackageInfo(BALLERINA_RUNTIME_PKG);
        StructureTypeInfo typeInfo = runtimePackage.getStructInfo(STRUCT_CALL_STACK_ELEMENT);
        return typeInfo;
    }
}
