/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.List;

/**
 * Service validator for WebSocket.
 */
public class WebSocketServiceValidator {

    public static boolean validateServiceEndpoint(WebSocketService wsService) {
        return validateResources(wsService.getName(), wsService.getResources(), false);
    }

    private static boolean validateResources(String serviceName, Resource[] resources, boolean isClientService) {
        for (Resource resource : resources) {
            String resourceName = resource.getName();
            switch (resourceName) {
                case WebSocketConstants.RESOURCE_NAME_ON_UPGRADE:
                    validateOnUpgradeResource(serviceName, resource, isClientService);
                    break;
                case WebSocketConstants.RESOURCE_NAME_ON_OPEN:
                    validateOnOpenResource(serviceName, resource, isClientService);
                    break;
                case WebSocketConstants.RESOURCE_NAME_ON_TEXT:
                    validateOnTextMessageResource(serviceName, resource, isClientService);
                    break;
                case WebSocketConstants.RESOURCE_NAME_ON_BINARY:
                    validateOnBinaryMessageResource(serviceName, resource, isClientService);
                    break;
                case WebSocketConstants.RESOURCE_NAME_ON_PING:
                    validateOnPingResource(serviceName, resource, isClientService);
                    break;
                case WebSocketConstants.RESOURCE_NAME_ON_PONG:
                    validateOnPongResource(serviceName, resource, isClientService);
                    break;
                case WebSocketConstants.RESOURCE_NAME_ON_IDLE_TIMEOUT:
                    validateOnIdleTimeoutResource(serviceName, resource, isClientService);
                    break;
                case WebSocketConstants.RESOURCE_NAME_ON_CLOSE:
                    validateOnCloseResource(serviceName, resource, isClientService);
                    break;
                default:
                    throw new BallerinaException(String.format("Invalid resource name %s", resourceName));
            }
        }
        return true;
    }

    private static void validateOnUpgradeResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                           WebSocketConstants.WEBSOCKET_ENDPOINT);
        validateStructType(resource.getName(), paramDetails.get(1), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                           HttpConstants.REQUEST);
    }

    private static void validateOnOpenResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 1, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                           WebSocketConstants.WEBSOCKET_ENDPOINT);
    }

    private static void validateOnTextMessageResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_ENDPOINT);
        validateStructType(resource.getName(), paramDetails.get(1), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                           WebSocketConstants.STRUCT_WEBSOCKET_TEXT_FRAME);
    }

    private static void validateOnBinaryMessageResource(String serviceName, Resource resource,
                                                        boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_ENDPOINT);
        validateStructType(resource.getName(), paramDetails.get(1), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                           WebSocketConstants.STRUCT_WEBSOCKET_BINARY_FRAME);

    }

    private static void validateOnPingResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_ENDPOINT);
        validateStructType(resource.getName(), paramDetails.get(1), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                           WebSocketConstants.STRUCT_WEBSOCKET_PING_FRAME);

    }

    private static void validateOnPongResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_ENDPOINT);
        validateStructType(resource.getName(), paramDetails.get(1), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                           WebSocketConstants.STRUCT_WEBSOCKET_PONG_FRAME);

    }

    private static void validateOnIdleTimeoutResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 1, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_ENDPOINT);
    }

    private static void validateOnCloseResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_ENDPOINT);
        validateStructType(resource.getName(), paramDetails.get(1), HttpConstants.PROTOCOL_PACKAGE_HTTP,
                           WebSocketConstants.STRUCT_WEBSOCKET_CLOSE_FRAME);
    }

    private static void validateParamDetailsSize(List<ParamDetail> paramDetails, int expectedSize, String serviceName,
                                                 String resourceName, boolean isClientService) {
        if (paramDetails == null || paramDetails.size() < expectedSize) {
            throw new BallerinaException(String.format("Invalid resource signature for %s in service %s",
                                                       resourceName, serviceName));
        }

        if (paramDetails.size() > expectedSize) {
            if (isClientService) {
                throw new BallerinaConnectorException(
                        String.format("%s cannot have additional parameters since service %s is a client service",
                                      resourceName, serviceName));
            }
            for (int i = expectedSize; i < paramDetails.size(); i++) {
                if (!(paramDetails.get(i).getVarType() instanceof BStringType)) {
                    throw new BallerinaConnectorException(
                            String.format("Additional parameters of resource %s in service %s should be strings",
                                          resourceName, serviceName));
                }
            }
        }
    }

    private static void validateStructType(String resourceName, ParamDetail paramDetail, String packageName,
                                           String structName) {
        // TODO: checking instance of gives a struct def when running a program and BStructType when compile

        if (!paramDetail.getVarType().getPackagePath().equals(packageName)) {
            throw new BallerinaException(
                    String.format("Invalid parameter type %s:%s %s in resource %s. Requires %s:%s",
                                  paramDetail.getVarType().getPackagePath(), paramDetail.getVarType().getName(),
                                  paramDetail.getVarName(), resourceName, packageName, structName));
        }

        if (!paramDetail.getVarType().getName().equals(structName)) {
            throw new BallerinaException(
                    String.format("Invalid parameter type %s:%s %s in resource %s. Requires %s:%s",
                                  paramDetail.getVarType().getPackagePath(), paramDetail.getVarType().getName(),
                                  paramDetail.getVarName(), resourceName, packageName, structName));
        }
    }
}
