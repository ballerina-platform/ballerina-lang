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
import org.ballerinalang.model.Node;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.values.BException;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Stack;

/**
 * Class contains utility methods for ballerina server error handling.
 */
public class ErrorHandlerUtils {

    private static final int STACK_TRACE_LIMIT = 20;

    private static final Logger log = LoggerFactory.getLogger(ErrorHandlerUtils.class);

    private static final String ERROR_PREFIX = "error in ballerina program: ";


    /**
     * Get the error message of a throwable.
     *
     * @param throwable Throwable
     * @return Error message
     */
    public static String getErrorMessage(Throwable throwable) {
        String errorMsg;
        if (throwable instanceof StackOverflowError) {
            errorMsg = "fatal " + ERROR_PREFIX + "stack overflow ";
        } else if (throwable.getMessage() != null) {
            errorMsg = ERROR_PREFIX + makeFirstLetterLowerCase(throwable.getMessage());
        } else {
            errorMsg = ERROR_PREFIX;
        }
        return errorMsg;
    }

    /**
     * Get the ballerina stack trace from context.
     *
     * @param context Ballerina context
     * @return Ballerina stack trace
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
     * Deprecated method to Get the ballerina stack trace for a main function. Use {@code getStackTrace} instead.
     *
     * @param context Ballerina context associated with the main function
     * @return Ballerina stack trace
     * @since 0.8.0.
     * @deprecated
     */
    public static String getMainFuncStackTrace(Context context, Throwable throwable) {
        // Need to omit the main function invocation from the stack trace. Hence the starting index is 1
        return getStackTrace(context, throwable, 0);
    }

    /**
     * Root Level Error handling for Resource invocations.
     *
     * @param bContext    Context instance.
     * @param currentNode Last execution node.
     * @param bException  BException to handle.
     * @param throwable   throwable to handle.
     */
    public static void handleResourceInvocationError(Context bContext, Node currentNode, BException bException,
                                                     Throwable throwable) {
        CarbonMessage cMsg = bContext.getCarbonMessage();
        CarbonCallback callback = bContext.getBalCallback();
        String errorMsg;
        if (throwable != null) {
            errorMsg = ErrorHandlerUtils.getErrorMessage(throwable);
        } else if (bException != null && bException.value() != null) {
            errorMsg = ERROR_PREFIX + bException.value();
        } else {
            errorMsg = ERROR_PREFIX;
        }
        String stacktrace = null;
        if (bException != null) {
            stacktrace = bException.value().getStackTrace();
        }
        if (stacktrace == null || "".equals(stacktrace)) {
            stacktrace = ErrorHandlerUtils.getStackTrace(bContext, currentNode);
        }
        String errorWithTrace = errorMsg + "\n" + stacktrace;
        log.error(errorWithTrace);
        PrintStream outStream = System.err;
        outStream.println(errorWithTrace);

        Object protocol = bContext.getServerConnectorProtocol();
        Optional<ServerConnectorErrorHandler> optionalErrorHandler =
                BallerinaConnectorManager.getInstance().getServerConnectorErrorHandler((String) protocol);

        try {
            optionalErrorHandler
                    .orElseGet(DefaultServerConnectorErrorHandler::getInstance)
                    .handleError(new BallerinaException(errorMsg, throwable, bContext), cMsg, callback);
        } catch (Exception e) {
            // Something wrong. This shouldn't execute.
            log.error("Cannot handle error using the error handler for : " + protocol, e);
        }
    }

    /**
     * Root Level Error handling for Main function invocations.
     *
     * @param bContext    Context instance.
     * @param currentNode Last execution node.
     * @param bException  BException to handle.
     * @param throwable   throwable to handle.
     */
    public static void handleMainFuncInvocationError(Context bContext, Node currentNode, BException bException,
                                                     Throwable throwable) {
        String errorMsg;
        if (throwable != null) {
            errorMsg = ErrorHandlerUtils.getErrorMessage(throwable);
        } else if (bException != null && bException.value() != null) {
            errorMsg = ERROR_PREFIX + bException.value();
        } else {
            errorMsg = ERROR_PREFIX;
        }
        String stacktrace = null;
        if (bException != null) {
            stacktrace = bException.value().getStackTrace();
        }
        if (stacktrace == null || "".equals(stacktrace)) {
            stacktrace = ErrorHandlerUtils.getStackTrace(bContext, currentNode);
        }
        String errorWithTrace = errorMsg + "\n" + stacktrace;
        log.error(errorWithTrace);
        PrintStream outStream = System.err;
        outStream.println(errorWithTrace);
//        Runtime.getRuntime().exit(1);
    }

    /**
     * Get the stack trace from the context and current node.
     *
     * @param context     Ballerina context
     * @param currentNode CurrentNode
     * @return Stack trace
     */
    public static String getStackTrace(Context context, Node currentNode) {
        ControlStack controlStack = context.getControlStack();
        StringBuilder sb = new StringBuilder();
        Stack<StackFrame> stack = controlStack.getStack();
        int i = stack.size() - 1;
        CallableUnitInfo frameInfo = stack.get(i).getNodeInfo();
        String pkgName = (frameInfo.getPackage() != null) ? frameInfo.getPackage() + ":" : "";
        // Adding first node's Stack trace.
        if (currentNode.getNodeLocation() != null) {
            sb.append("\t at ").append(pkgName).append(frameInfo.getName())
                    .append(getNodeLocation(currentNode.getNodeLocation())).append("\n");
        }
        int lastStackTracePosition = 0;
        if (context.getServiceInfo() != null) {
            // For Resource we have to skip, 0th stackFrame.
            lastStackTracePosition = 1;
        }
        while (i >= lastStackTracePosition) {
            frameInfo = stack.get(i).getNodeInfo();
            pkgName = (frameInfo.getPackage() != null) ? frameInfo.getPackage() + ":" : "";
            sb.append("\t at ").append(pkgName).append(frameInfo.getName())
                    .append(getNodeLocation(frameInfo));
            if (i > lastStackTracePosition) {
                sb.append("\n");
            }
            i--;
        }
        // print the service info
        CallableUnitInfo serviceInfo = context.getServiceInfo();
        if (serviceInfo != null) {
            pkgName = (serviceInfo.getPackage() != null) ? serviceInfo.getPackage() + ":" : "";
            sb.append("\n\t at service ").append(pkgName).append(serviceInfo.getName())
                    .append(getNodeLocation(serviceInfo));
        }

        return sb.toString();
    }

    /**
     * Get the stack trace from the context.
     *
     * @param context         Ballerina context
     * @param throwable       Throwable associated with the error occurred
     * @param stackStartIndex Start index of the stack to generate the stack trace
     * @return Stack trace
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
     */
    private static String getNodeLocation(CallableUnitInfo nodeInfo) {
        NodeLocation nodeLocation = nodeInfo.getNodeLocation();
        return getNodeLocation(nodeLocation);
    }

    /**
     * Get the source location as string in the format of '(fileName:lineNumber)'.
     *
     * @param nodeLocation {@link NodeLocation} to get the location
     * @return source location of this {@link CallableUnitInfo}
     */
    private static String getNodeLocation(NodeLocation nodeLocation) {
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
