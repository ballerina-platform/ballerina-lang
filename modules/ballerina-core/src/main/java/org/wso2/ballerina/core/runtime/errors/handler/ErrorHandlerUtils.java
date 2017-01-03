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
package org.wso2.ballerina.core.runtime.errors.handler;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.NodeInfo;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.StackFrameType;
import org.wso2.ballerina.core.model.Position;

import java.util.Iterator;

/**
 * Class contains utility methods for ballerina server error handling.
 */
public class ErrorHandlerUtils {
    
    /**
     * Get the ballerina stack trace from a throwable.
     * 
     * @param throwable Throwable
     * @return          Ballerina stack trace
     */
    public static String getBallerinaStackTrace(Throwable throwable) {
        if (throwable instanceof BallerinaException) {
            Context context = ((BallerinaException) throwable).getContext();
            if (context != null) {
                return getBallerinaStackTrace(context);
            }
        }

        return null;
    }

    /**
     * Get the ballerina stack trace from context.
     * 
     * @param context   Ballerina context
     * @return          Ballerina stack trace
     */
    public static String getBallerinaStackTrace(Context context) {
        ControlStack controlStack = context.getControlStack();
        StringBuilder sb = new StringBuilder();
        NodeInfo serviceInfo = context.getServiceInfo();
        if (serviceInfo != null) {
            sb.append("\t at " + serviceInfo.getNodePackage() + ":" + serviceInfo.getNodeName() + 
                    getNodeLocation(serviceInfo) + "\n");
        }

        Iterator<StackFrame> itr = controlStack.iterator();
        while (true) {
            while (itr.hasNext()) {
                NodeInfo frameInfo = itr.next().getNodeInfo();
                
                if (frameInfo.getNodePackage() != null && frameInfo.getNodeType() != StackFrameType.NATIVE_FUNCTION) {
                    sb.append("\t at " + frameInfo.getNodePackage() + ":" + frameInfo.getNodeName() + 
                            getNodeLocation(frameInfo) + "\n");
                } else if (frameInfo.getNodeType() == StackFrameType.BALLERINA_FUNCTION && serviceInfo != null) {
                    sb.append("\t at " + serviceInfo.getNodePackage() + ":" + frameInfo.getNodeName() + 
                            getNodeLocation(frameInfo) + "\n");
                } else {
                    sb.append("\t at " + frameInfo.getNodeName() + "\n");
                }
            }

            return sb.toString();
        }
    }
    
    /**
     * Get the ballerina stack trace for a main function.
     * 
     * @param context       Ballerina context associated with the main function
     * @param packageName   Package name of the main function
     * @return              Ballerina stack trace
     */
    public static String getMainFunctionStackTrace(Context context, String packageName) {
        ControlStack controlStack = context.getControlStack();
        StringBuilder sb = new StringBuilder();
        Iterator<StackFrame> itr = controlStack.iterator();

        while (itr.hasNext()) {
            NodeInfo frameInfo = itr.next().getNodeInfo();
            if (frameInfo.getNodePackage() != null && frameInfo.getNodeType() != StackFrameType.NATIVE_FUNCTION) {
                sb.append("\t at " + frameInfo.getNodePackage() + ":" + frameInfo.getNodeName() + 
                        getNodeLocation(frameInfo) + "\n");
            } else if (frameInfo.getNodeType() == StackFrameType.BALLERINA_FUNCTION || 
                       frameInfo.getNodeType() == StackFrameType.MAIN_FUNCTION) {
                sb.append("\t at " + packageName + ":" + frameInfo.getNodeName() + getNodeLocation(frameInfo) + "\n");
            } else {
                sb.append("\t at " + frameInfo.getNodeName() + "\n");
            }
        }

        return sb.toString();
    }
    
    private static String getNodeLocation(NodeInfo nodeInfo) {
        Position nodePosition = nodeInfo.getLocation();
        if (nodePosition != null) {
            String fileName = nodePosition.getFileName();
            int line = nodePosition.getLine();
            return "(" + fileName + ":" + line + ")";
        } else {
            return "";
        }
    }
}
