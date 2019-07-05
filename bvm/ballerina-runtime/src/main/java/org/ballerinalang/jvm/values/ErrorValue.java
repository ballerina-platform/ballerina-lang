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
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.BErrorType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.values.freeze.Status;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.jvm.BallerinaErrors.ERROR_PRINT_PREFIX;

/**
 * Represent an error in ballerina.
 *
 * @since 0.995.0
 */
public class ErrorValue extends RuntimeException implements RefValue {

    private static final long serialVersionUID = 1L;
    private final BType type;
    private final String reason;
    private final Object details;

    public ErrorValue(String reason, Object details) {
        super(reason);
        this.type = new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage(),
                BTypes.typeString, TypeChecker.getType(details));
        this.reason = reason;
        this.details = details;
    }

    public ErrorValue(BType type, String reason, Object details) {
        super(reason);
        this.type = type;
        this.reason = reason;
        this.details = details;
    }

    @Override
    public String stringValue() {
        return reason + " " + details.toString();
    }

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {

    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        // Error values are immutable and frozen, copy give same value.
        return this;
    }

    @Override
    public void attemptFreeze(Status freezeStatus) {
        // do nothing, since error types are always frozen
    }

    @Override
    public String toString() {
        return stringValue();
    }

    public String getReason() {
        return reason;
    }

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

    public void printStackTrace(PrintWriter printWriter) {
        printWriter.print(ERROR_PRINT_PREFIX + getPrintableStackTrace());
    }
    
    @Override
    public StackTraceElement[] getStackTrace() {
        StackTraceElement[] stackTrace = super.getStackTrace();
        List<StackTraceElement> filteredStack = new LinkedList<>();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement stackTraceElement = BallerinaErrors.filterStackTraceElement(stackTrace, i);
            if (stackTraceElement != null) {
                filteredStack.add(stackTraceElement);
            }
        }
        StackTraceElement[] filteredStackArray = new StackTraceElement[filteredStack.size()];
        return filteredStack.toArray(filteredStackArray);
    }

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
        // Append the method name
        sb.append(tab).append(stackTraceElement.getMethodName());
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
