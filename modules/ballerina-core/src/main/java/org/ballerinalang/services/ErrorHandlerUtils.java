/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.services;

import org.ballerinalang.bre.CallableUnitInfo;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.ControlStack;
import org.ballerinalang.bre.StackFrame;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.file.Paths;
import java.util.Stack;

/**
 * Class contains utility methods for ballerina server error handling.
 */
public class ErrorHandlerUtils {

    private static final int STACK_TRACE_LIMIT = 20;

    /**
     * Get the error message of a throwable.
     *
     * @param throwable Throwable
     * @return Error message
     */
    public static String getErrorMessage(Throwable throwable) {
        String errorMsg;
        String errorPrefix = "error in ballerina program: ";
        if (throwable instanceof StackOverflowError) {
            errorMsg = "fatal " + errorPrefix + "stack overflow ";
        } else if (throwable.getMessage() != null) {
            errorMsg = errorPrefix + makeFirstLetterLowerCase(throwable.getMessage());
        } else {
            errorMsg = errorPrefix;
        }
        return errorMsg;
    }

    /**
     * Get the stack trace from the context.
     *
     * @param context Ballerina context
     * @return Stack trace
     */
    public static String getStackTrace(Context context) {
        // if the context is null, stack trace cannot be generated.
        if (context == null || context.getControlStack().getStack().size() == 0) {
            return "";
        }
        ControlStack controlStack = context.getControlStack();
        StringBuilder sb = new StringBuilder();
        Stack<StackFrame> stack = controlStack.getStack();

        // TODO : ? For stack Trace generation ignores first stackFrame, where execution starts.
        int lastStackTracePosition = 0;
        if (context.getServiceName() != null) {
            // For Resource invocation we have to skip, resources invocation expressions stack frame as well.
            lastStackTracePosition = 1;
        }
        // Adding current node's line to Stack trace.
        int currentStackLocation = stack.size() - 1;

        while (currentStackLocation >= lastStackTracePosition) {
            StackFrame stackFrame = stack.get(currentStackLocation);
            String pkgName = (stackFrame.nodeLocation.getPackageDirPath() != null) ?
                    stackFrame.nodeLocation.getPackageDirPath() + ":" : "";
            sb.append("\t at ").append(pkgName);
            if (currentStackLocation == lastStackTracePosition && context.getServiceName() != null) {
                // Service Invocation and has to append Service name after package name.
                sb.append(context.getServiceName()).append(".");
            }
            sb.append(stackFrame.unitName).append(getNodeLocation(stackFrame.nodeLocation));
            if (currentStackLocation > lastStackTracePosition) {
                sb.append("\n");
            }
            currentStackLocation--;
        }
        return sb.toString();
    }

    public static String getCause(Throwable t) {
        StringBuilder sb = new StringBuilder();
        getCause(t, sb);
        return sb.toString();
    }

    private static void getCause(Throwable t, StringBuilder sb) {
        if (t instanceof BallerinaException || t instanceof BLangRuntimeException) {
            return;
        }
        sb.append("reason  ").append(t.getLocalizedMessage()).append(",");
        if (t.getCause() != null) {
            getCause(t.getCause(), sb);
        }
    }

    /**
     * Get the source location as string in the format of '(fileName:lineNumber)'.
     *
     * @param nodeLocation {@link NodeLocation} to get the location
     * @return source location of this {@link CallableUnitInfo}
     */
    private static String getNodeLocation(NodeLocation nodeLocation) {
        if (nodeLocation != null) {
            String fileName = Paths.get(nodeLocation.getFileName()).getFileName().toString();
            int line = nodeLocation.getLineNumber();
            return "(" + fileName + ":" + line + ")";
        } else {
            return "";
        }
    }

    /**
     * Get the ballerina stack trace from context.
     *
     * @param context Ballerina context
     * @param throwable Throwable associated with the error
     * @return Ballerina stack trace
     * @deprecated as of 0.8.3
     */
    public static String getServiceStackTrace(Context context, Throwable throwable) {
        if (context == null && throwable instanceof BallerinaException) {
            context = ((BallerinaException) throwable).getContext();
        }

        // if the context is null, stack trace cannot be generated
        if (context == null) {
            return throwable.getMessage();
        }

        String stackTrace = getStackTrace(context, throwable, 1);

        // print the service info
        CallableUnitInfo serviceInfo = context.getServiceInfo();
        if (serviceInfo != null) {
            String pkgName = (serviceInfo.getPackage() != null) ? serviceInfo.getPackage() + ":" : "";
            stackTrace = stackTrace + "\t at " + pkgName + serviceInfo.getName() + getNodeLocation(serviceInfo) + "\n";
        }

        return stackTrace;
    }

    /**
     * Get the ballerina stack trace for a main function.
     *
     * @param context Ballerina context associated with the main function
     * @param throwable Throwable associated with the error
     * @return Ballerina stack trace
     * @deprecated as of 0.8.3
     */
    public static String getMainFuncStackTrace(Context context, Throwable throwable) {
        // Need to omit the main function invocation from the stack trace. Hence the starting index is 1
        return getStackTrace(context, throwable, 0);
    }

    /**
     * Get the stack trace from the context.
     *
     * @param context         Ballerina context
     * @param throwable       Throwable associated with the error occurred
     * @param stackStartIndex Start index of the stack to generate the stack trace
     * @return Stack trace
     * @deprecated as of 0.8.3
     */
    private static String getStackTrace(Context context, Throwable throwable, int stackStartIndex) {
        ControlStack controlStack = context.getControlStack();
        StringBuilder sb = new StringBuilder();
        Stack<StackFrame> stack = controlStack.getStack();

        if (throwable instanceof StackOverflowError) {
            populateStackOverflowTrace(sb, stack, stackStartIndex);
        } else {
            for (int i = stack.size() - 1; i >= stackStartIndex; i--) {
                CallableUnitInfo frameInfo = stack.get(i).getNodeInfo();
                String pkgName = (frameInfo.getPackage() != null) ? frameInfo.getPackage() + ":" : "";
                sb.append("\t at ").append(pkgName).append(frameInfo.getName())
                        .append(getNodeLocation(frameInfo)).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Get the source location as string in the format of '(fileName:lineNumber)'.
     *
     * @param nodeInfo {@link CallableUnitInfo} to get the location
     * @return source location of this {@link CallableUnitInfo}
     * @deprecated as of 0.8.3
     */
    private static String getNodeLocation(CallableUnitInfo nodeInfo) {
        NodeLocation nodeLocation = nodeInfo.getNodeLocation();
        if (nodeLocation != null) {
            String fileName = nodeLocation.getFileName();
            int line = nodeLocation.getLineNumber();
            return "(" + fileName + ":" + line + ")";
        } else {
            return "";
        }
    }

    /**
     * Populate the stack trace of a stack overflow error.
     *
     * @param sb    String buffer to populate the stack trace
     * @param stack Current stack
     * @deprecated as of 0.8.3
     */
    private static void populateStackOverflowTrace(StringBuilder sb, Stack<StackFrame> stack, int stackStartIndex) {
        for (int i = stack.size() - 1; i >= stack.size() - STACK_TRACE_LIMIT; i--) {
            CallableUnitInfo frameInfo = stack.get(i).getNodeInfo();
            String pkgName = (frameInfo.getPackage() != null) ? frameInfo.getPackage() + ":" : "";
            sb.append("\t at " + pkgName + frameInfo.getName() + getNodeLocation(frameInfo)
                    + "\n");
        }
        sb.append("\t ...\n\t ...\n");
        for (int i = STACK_TRACE_LIMIT + stackStartIndex - 1; i >= stackStartIndex; i--) {
            CallableUnitInfo frameInfo = stack.get(i).getNodeInfo();
            String pkgName = (frameInfo.getPackage() != null) ? frameInfo.getPackage() + ":" : "";
            sb.append("\t at " + pkgName + frameInfo.getName() + getNodeLocation(frameInfo)
                    + "\n");
        }
    }

    private static String makeFirstLetterLowerCase(String s) {
        char c[] = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}
