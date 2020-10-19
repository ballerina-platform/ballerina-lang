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
package io.ballerina.runtime.values;

import io.ballerina.runtime.CycleUtils;
import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeConstants;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.services.ErrorHandlerUtils;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BErrorType;
import io.ballerina.runtime.types.BTypeIdSet;
import io.ballerina.runtime.util.exceptions.BallerinaErrorReasons;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_RUNTIME_PKG_ID;
import static io.ballerina.runtime.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;
import static io.ballerina.runtime.util.BLangConstants.GENERATE_PKG_INIT;
import static io.ballerina.runtime.util.BLangConstants.GENERATE_PKG_START;
import static io.ballerina.runtime.util.BLangConstants.GENERATE_PKG_STOP;
import static io.ballerina.runtime.util.BLangConstants.INIT_FUNCTION_SUFFIX;
import static io.ballerina.runtime.util.BLangConstants.MODULE_INIT_CLASS_NAME;
import static io.ballerina.runtime.util.BLangConstants.START_FUNCTION_SUFFIX;
import static io.ballerina.runtime.util.BLangConstants.STOP_FUNCTION_SUFFIX;

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
    private final Type type;
    private final BString message;
    private final BError cause;
    private final Object details;

    public static final String GENERATE_OBJECT_CLASS_PREFIX = ".$value$";

    public ErrorValue(BString message, Object details) {
        this(new BErrorType(TypeConstants.ERROR, PredefinedTypes.TYPE_ERROR.getPackage(), TypeChecker.getType(details)),
                message, null, details);
    }

    public ErrorValue(Type type, BString message, BError cause, Object details) {
        super(message);
        this.type = type;
        this.message = message;
        this.cause = cause;
        this.details = details;
    }

    public ErrorValue(Type type, BString message, BError cause, Object details,
                      String typeIdName, Module typeIdPkg) {
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
            return "error" + getModuleNameToString() + "(" + ((StringValue) message).informalStringValue(linkParent)
                    + ")";
        }
        return "error" + getModuleNameToString() + "(" + ((StringValue) message).informalStringValue(linkParent) +
                getCauseToString(linkParent) + getDetailsToString(linkParent) + ")";
    }

    @Override
    public String expressionStringValue(BLink parent) {
        CycleUtils.Node linkParent = new CycleUtils.Node(this, parent);
        if (isEmptyDetail()) {
            return "error" + getModuleNameToBalString() + "(" + ((StringValue) message)
                    .informalStringValue(linkParent) + ")";
        }
        return "error" + getModuleNameToBalString() + "(" + ((StringValue) message).expressionStringValue(linkParent) +
                getCauseToBalString(linkParent) + getDetailsToBalString(linkParent) + ")";
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
                Type type = TypeChecker.getType(value);
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
                        sj.add(key + "=" + StringUtils.getStringValue(value, parent));
                        break;
                }
            }
        }
        return "," + sj.toString();
    }

    private String getModuleNameToString() {
        return type.getPackage().getName() == null ? "" : " " + type.getName() + " ";
    }

    private String getDetailsToBalString(BLink parent) {
        StringJoiner sj = new StringJoiner(",");
        for (Object key : ((MapValue) details).getKeys()) {
            Object value = ((MapValue) details).get(key);
            sj.add(key + "=" + StringUtils.getExpressionStringValue(value, parent));
        }
        return "," + sj.toString();
    }

    private String getCauseToBalString(BLink parent) {
        if (cause != null) {
            return "," + cause.expressionStringValue(parent);
        }
        return "";
    }

    private String getModuleNameToBalString() {
        return (type.getPackage().getOrg() != null && type.getPackage().getOrg().equals("$anon")) ||
                type.getPackage().getName() == null ? " " + type.getName() + " " :
                String.valueOf(
                        BallerinaErrorReasons.getModulePrefixedReason(type.getPackage().getName(), type.getName()));
    }

    @Override
    public Type getType() {
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
    public BArray getCallStack() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> filteredStack = new LinkedList<>();
        int index = 0;
        for (StackTraceElement stackFrame : stackTrace) {
            Optional<StackTraceElement> stackTraceElement = filterStackTraceElement(stackFrame, index++);
            stackTraceElement.ifPresent(filteredStack::add);
        }
        Type recordType = ValueCreator.createRecordValue(BALLERINA_RUNTIME_PKG_ID, CALL_STACK_ELEMENT).getType();
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
        return ValueCreator.
                createRecordValue(ValueCreator.createRecordValue(BALLERINA_RUNTIME_PKG_ID, CALL_STACK_ELEMENT),
                                  values);
    }

    private String cleanupClassName(String className) {
        return className.replace(GENERATE_OBJECT_CLASS_PREFIX, ".");
    }
}
