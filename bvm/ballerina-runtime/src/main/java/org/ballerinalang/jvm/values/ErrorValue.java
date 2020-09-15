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

import org.ballerinalang.jvm.CycleUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.BValueCreator;
import org.ballerinalang.jvm.api.values.BError;
import org.ballerinalang.jvm.api.values.BLink;
import org.ballerinalang.jvm.api.values.BMap;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.api.values.BValue;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BErrorType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypeIdSet;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.types.TypeTags;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_RUNTIME_PKG_ID;
import static org.ballerinalang.jvm.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;
import static org.ballerinalang.jvm.util.BLangConstants.GENERATE_PKG_INIT;
import static org.ballerinalang.jvm.util.BLangConstants.GENERATE_PKG_START;
import static org.ballerinalang.jvm.util.BLangConstants.GENERATE_PKG_STOP;
import static org.ballerinalang.jvm.util.BLangConstants.INIT_FUNCTION_SUFFIX;
import static org.ballerinalang.jvm.util.BLangConstants.MODULE_INIT_CLASS_NAME;
import static org.ballerinalang.jvm.util.BLangConstants.START_FUNCTION_SUFFIX;
import static org.ballerinalang.jvm.util.BLangConstants.STOP_FUNCTION_SUFFIX;

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
    private final BString message;
    private final BError cause;
    private final Object details;

    public static final String GENERATE_OBJECT_CLASS_PREFIX = ".$value$";
    public static final String CALL_STACK_ELEMENT = "CallStackElement";

    public ErrorValue(BString message, Object details) {
        this(new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage(), TypeChecker.getType(details)),
                message, null, details);
    }

    public ErrorValue(BType type, BString message, BError cause, Object details) {
        super(message);
        this.type = type;
        this.message = message;
        this.cause = cause;
        this.details = details;
    }

    public ErrorValue(BType type, BString message, BError cause, Object details,
                      String typeIdName, BPackage typeIdPkg) {
        super(message);
        this.type = type;
        this.message = message;
        this.cause = cause;
        this.details = details;
        BTypeIdSet typeIdSet = new BTypeIdSet();
        typeIdSet.add(typeIdPkg, typeIdName, true);
        ((BErrorType) type).setTypeIdSet(typeIdSet);
    }

    @Override
    public String stringValue(BLink parent) {
        CycleUtils.Node linkParent = new CycleUtils.Node(this, parent);
        if (isEmptyDetail()) {
            return "error" + getModuleName() + "(" + ((StringValue) message).informalStringValue(linkParent) + ")";
        }

        return "error" + getModuleName() + "(" + ((StringValue) message).informalStringValue(linkParent) +
                getCauseToString(linkParent) + getDetailsToString(linkParent) + ")";
    }

    private String getCauseToString(BLink parent) {
        if (cause != null) {
            return "," + cause.informalStringValue(parent);
        }
        return "";
    }

    private String getDetailsToString(BLink parent) {
        StringJoiner sj = new StringJoiner(",");
        for (Object key : ((MapValue) details).getKeys()) {
            Object value = ((MapValue) details).get(key);
            if (value == null) {
                sj.add(key + "=null");
            } else {
                BType type = TypeChecker.getType(value);
                switch (type.getTag()) {
                    case TypeTags.STRING_TAG:
                    case TypeTags.XML_TAG:
                    case TypeTags.XML_ELEMENT_TAG:
                    case TypeTags.XML_ATTRIBUTES_TAG:
                    case TypeTags.XML_COMMENT_TAG:
                    case TypeTags.XML_PI_TAG:
                    case TypeTags.XMLNS_TAG:
                    case TypeTags.XML_TEXT_TAG:
                        sj.add(key + "=" + ((BValue) value).informalStringValue(parent));
                        break;
                    default:
                        sj.add(key + "=" + BStringUtils.getStringValue(value, parent));
                        break;
                }
            }
        }
        return "," + sj.toString();
    }

    private String getModuleName() {
        return type.getPackage().name == null ? "" : " " + type.getName() + " ";
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
    }

    @Override
    public String toString() {
        return stringValue(null);
    }

    /**
     * Returns error message.
     *
     * @return message string
     */
    @Override
    public BString getErrorMessage() {
        return this.message;
    }

    /**
     * Returns error details.
     *
     * @return detail record
     */
    public Object getDetails() {
        if (details instanceof RefValue) {
            return ((RefValue) details).copy(new HashMap<>());
        }
        return details;
    }

    @Override
    public BError getCause() {
        return this.cause;
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
            Optional<StackTraceElement> stackTraceElement = filterStackTraceElement(stackFrame, index++);
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
        String errorMsg = getPrintableError();
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

    @Override
    public ArrayValue getCallStack() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> filteredStack = new LinkedList<>();
        int index = 0;
        for (StackTraceElement stackFrame : stackTrace) {
            Optional<StackTraceElement> stackTraceElement = filterStackTraceElement(stackFrame, index++);
            stackTraceElement.ifPresent(filteredStack::add);
        }
        BType recordType = BValueCreator.createRecordValue(BALLERINA_RUNTIME_PKG_ID, CALL_STACK_ELEMENT).getType();
        ArrayValue callStack = new ArrayValueImpl(new BArrayType(recordType));
        for (int i = 0; i < filteredStack.size(); i++) {
            callStack.add(i, getStackFrame(filteredStack.get(i)));
        }
        return callStack;
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

    private String getPrintableError() {
        StringJoiner joiner = new StringJoiner(" ");

        joiner.add(this.message.getValue());
        if (this.cause != null) {
            joiner.add("cause: " + this.cause.getMessage());
        }
        if (!isEmptyDetail()) {
            joiner.add(this.details.toString());
        }

        return joiner.toString();
    }

    private boolean isEmptyDetail() {
        if (details == null) {
            return true;
        }
        return (details instanceof MapValue) && ((MapValue<?, ?>) details).isEmpty();
    }

    private Optional<StackTraceElement> filterStackTraceElement(StackTraceElement stackFrame, int currentIndex) {
        String fileName = stackFrame.getFileName();
        int lineNo = stackFrame.getLineNumber();
        if (lineNo < 0) {
            return Optional.empty();
        }
        // Handle init function
        String className = stackFrame.getClassName();
        String methodName = stackFrame.getMethodName();
        if (className.equals(MODULE_INIT_CLASS_NAME)) {
            if (currentIndex == 0) {
                return Optional.empty();
            }
            switch (methodName) {
                case GENERATE_PKG_INIT:
                    methodName = INIT_FUNCTION_SUFFIX;
                    break;
                case GENERATE_PKG_START:
                    methodName = START_FUNCTION_SUFFIX;
                    break;
                case GENERATE_PKG_STOP:
                    methodName = STOP_FUNCTION_SUFFIX;
                    break;
                default:
                    return Optional.empty();
            }
            return Optional.of(new StackTraceElement(cleanupClassName(className), methodName, fileName,
                                                     stackFrame.getLineNumber()));

        }
        if (fileName != null && !fileName.endsWith(BLANG_SRC_FILE_SUFFIX)) {
            // Remove java sources for bal stacktrace if they are not extern functions.
            return Optional.empty();
        }
        return Optional.of(
                new StackTraceElement(cleanupClassName(className), methodName, fileName, stackFrame.getLineNumber()));
    }

    private BMap<BString, Object> getStackFrame(StackTraceElement stackTraceElement) {
        Object[] values = new Object[4];
        values[0] = stackTraceElement.getMethodName();
        values[1] = stackTraceElement.getClassName();
        values[2] = stackTraceElement.getFileName();
        values[3] = stackTraceElement.getLineNumber();
        return BValueCreator.
                createRecordValue(BValueCreator.createRecordValue(BALLERINA_RUNTIME_PKG_ID, CALL_STACK_ELEMENT),
                                  values);
    }

    private String cleanupClassName(String className) {
        return className.replace(GENERATE_OBJECT_CLASS_PREFIX, ".");
    }
}
