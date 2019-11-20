/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.BErrorType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.values.api.BError;
import org.ballerinalang.jvm.values.freeze.Status;
import org.ballerinalang.jvm.values.utils.StringUtils;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.jvm.BallerinaErrors.ERROR_PRINT_PREFIX;
import static org.ballerinalang.jvm.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;
import static org.ballerinalang.jvm.util.BLangConstants.MODULE_INIT_CLASS_NAME;

/**
 * <p>
 * Represent an error in ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @since 0.995.0
 */
public class ErrorValue extends BError implements RefValue {

    private static final long serialVersionUID = 1L;
    private final BType type;
    private final String reason;
    private final Object details;

    @Deprecated
    public ErrorValue(String reason, Object details) {
        super(reason);
        this.type = new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage(),
                BTypes.typeString, TypeChecker.getType(details));
        this.reason = reason;
        this.details = details;
    }

    @Deprecated
    public ErrorValue(BType type, String reason, Object details) {
        super(reason);
        this.type = type;
        this.reason = reason;
        this.details = details;
    }

    @Override
    public String stringValue() {
        return stringValue(null);
    }

    @Override
    public String stringValue(Strand strand) {
        return "error " + reason + Optional.ofNullable(details).map(details -> " " + StringUtils.getStringValue(strand,
                details)).orElse("");
    }

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        // Error values are immutable and frozen, copy give same value.
        return this;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        // Error values are immutable and frozen, copy give same value.
        return this;
    }

    @Override
    public void attemptFreeze(Status freezeStatus) {
        // do nothing, since error types are always frozen
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
    }

    @Override
    public String toString() {
        return stringValue();
    }

    /**
     * Returns error reason.
     * @return reason string
     */
    public String getReason() {
        return reason;
    }

    /**
     * Returns error details.
     * @return detail record
     */
    public Object getDetails() {
        if (details instanceof RefValue) {
            return ((RefValue) details).copy(new HashMap<>());
        }
        return details;
    }

    @Override
    public void printStackTrace() {
        ErrorHandlerUtils.printError(ERROR_PRINT_PREFIX + getPrintableStackTrace());
    }

    /**
     * Print error stack trace to the given {@code PrintWriter}.
     * @param printWriter {@code PrintWriter} to be used
     */
    public void printStackTrace(PrintWriter printWriter) {
        printWriter.print(ERROR_PRINT_PREFIX + getPrintableStackTrace());
    }
    
    @Override
    public StackTraceElement[] getStackTrace() {
        StackTraceElement[] stackTrace = super.getStackTrace();
        List<StackTraceElement> filteredStack = new LinkedList<>();
        int index = 0;
        for (StackTraceElement stackFrame : stackTrace) {
            Optional<StackTraceElement> stackTraceElement =
                    BallerinaErrors.filterStackTraceElement(stackFrame, index++);
            stackTraceElement.ifPresent(filteredStack::add);
        }
        StackTraceElement[] filteredStackArray = new StackTraceElement[filteredStack.size()];
        return filteredStack.toArray(filteredStackArray);
    }

    /**
     * Returns error stack trace as a string.
     * @return stack trace string
     */
    public String getPrintableStackTrace() {
        String errorMsg = getErrorMessage();
        StringBuilder sb = new StringBuilder();
        sb.append(errorMsg);
        // Append function/action/resource name with package path (if any)
        StackTraceElement[] stackTrace = this.getStackTrace();
        if (stackTrace.length == 0) {
            return sb.toString();
        }
        sb.append("\n\tat ");
        // print first element
        printStackElement(sb, stackTrace[0], "");
        for (int i = 1; i < stackTrace.length; i++) {
            printStackElement(sb, stackTrace[i], "\n\t   ");
        }
        return sb.toString();
    }

    private void printStackElement(StringBuilder sb, StackTraceElement stackTraceElement, String tab) {
        String pkgName = stackTraceElement.getClassName();
        String fileName = stackTraceElement.getFileName();

        // clean file name from pkgName since we print the file name after the method name.
        fileName = fileName.replace(BLANG_SRC_FILE_SUFFIX, "");
        fileName = fileName.replace("/", "-");
        pkgName = pkgName.replace("." + fileName, "");
        // todo we need to seperate orgname and module name with '/'

        sb.append(tab);
        if (!pkgName.equals(MODULE_INIT_CLASS_NAME)) {
            sb.append(pkgName).append(":");
        }

        // Append the method name
        sb.append(stackTraceElement.getMethodName());
        // Append the filename
        sb.append("(").append(stackTraceElement.getFileName());
        // Append the line number
        sb.append(":").append(stackTraceElement.getLineNumber()).append(")");
    }

    private String getErrorMessage() {
        String errorMsg = "";
        boolean reasonAdded = false;
        if (reason != null && !reason.isEmpty()) {
            errorMsg = reason;
            reasonAdded = true;
        }
        if (details != null) {
            errorMsg = errorMsg + (reasonAdded ? " " : "") + details.toString();
        }
        return errorMsg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFrozen() {
        return true;
    }
}
