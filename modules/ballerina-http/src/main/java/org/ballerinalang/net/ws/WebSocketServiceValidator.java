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

package org.ballerinalang.net.ws;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
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

    public static boolean validateClientService(WebSocketService wsService) {
        if (wsService.getAnnotation(Constants.PROTOCOL_PACKAGE_WS, Constants.ANNOTATION_CONFIGURATION) != null) {
            throw new BallerinaException(
                    String.format("Cannot define %s:%s annotation for WebSocket client service",
                                  Constants.PROTOCOL_PACKAGE_WS, Constants.ANNOTATION_CONFIGURATION));
        }
        return validateResources(wsService.getName(), wsService.getResources(), true);
    }

    public static boolean validateServiceEndpoint(WebSocketService wsService) {
        if (wsService.getAnnotation(Constants.PROTOCOL_PACKAGE_WS,
                                    Constants.ANNOTATION_WEBSOCKET_CLIENT_SERVICE) != null) {
            throw new BallerinaException(
                    String.format("Cannot define %s:%s annotation for WebSocket client service",
                                  Constants.PROTOCOL_PACKAGE_WS, Constants.ANNOTATION_WEBSOCKET_CLIENT_SERVICE));
        }
        validateConfigAnnotation(wsService);
        return validateResources(wsService.getName(), wsService.getResources(), false);
    }

    private static boolean validateResources(String serviceName, Resource[] resources, boolean isClientService) {
        for (Resource resource : resources) {
            String resourceName = resource.getName();
            switch (resourceName) {
                case Constants.RESOURCE_NAME_ON_HANDSHAKE:
                    validateOnHandshakeResource(serviceName, resource, isClientService);
                    break;
                case Constants.RESOURCE_NAME_ON_OPEN:
                    validateOnOpenResource(serviceName, resource, isClientService);
                    break;
                case Constants.RESOURCE_NAME_ON_TEXT_MESSAGE:
                    validateOnTextMessageResource(serviceName, resource, isClientService);
                    break;
                case Constants.RESOURCE_NAME_ON_BINARY_MESSAGE:
                    validateOnBinaryMessageResource(serviceName, resource, isClientService);
                    break;
                case Constants.RESOURCE_NAME_ON_PING:
                    validateOnPingResource(serviceName, resource, isClientService);
                    break;
                case Constants.RESOURCE_NAME_ON_PONG:
                    validateOnPongResource(serviceName, resource, isClientService);
                    break;
                case Constants.RESOURCE_NAME_ON_IDLE_TIMEOUT:
                    validateOnIdleTimeoutResource(serviceName, resource, isClientService);
                    break;
                case Constants.RESOURCE_NAME_ON_CLOSE:
                    validateOnCloseResource(serviceName, resource, isClientService);
                    break;
                default:
                    throw new BallerinaException(String.format("Invalid resource name %s", resourceName));
            }
        }
        return true;
    }

    private static void validateConfigAnnotation(WebSocketService wsService) {
        Annotation configAnnotation =
                wsService.getAnnotation(Constants.PROTOCOL_PACKAGE_WS, Constants.ANNOTATION_CONFIGURATION);
        if (configAnnotation == null) {
            return;
        }
        AnnAttrValue basePath = configAnnotation.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_BASE_PATH);
        AnnAttrValue host = configAnnotation.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_HOST);
        AnnAttrValue port = configAnnotation.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_PORT);
        if (basePath == null && (host != null || port != null)) {
            String msg = String.format("service %s: cannot define host, port configurations without base path",
                                       wsService.getName());
            throw new BallerinaConnectorException(msg);
        }
    }

    /**
     * Find out the given service is a WebSocket client service or not.
     *
     * @param service {@link WebSocketService} which should be identified.
     * @return true if the given service is a client service.
     */
    public static boolean isWebSocketClientService(WebSocketService service) {
        Annotation annotation = service.getAnnotation(
                Constants.PROTOCOL_PACKAGE_WS, Constants.ANNOTATION_WEBSOCKET_CLIENT_SERVICE);
        return !(annotation == null);
    }

    private static void validateOnHandshakeResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 1, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_HANDSHAKE_CONNECTION);
    }

    private static void validateOnOpenResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 1, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
    }

    private static void validateOnTextMessageResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
        validateStructType(resource.getName(), paramDetails.get(1), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_TEXT_FRAME);
    }

    private static void validateOnBinaryMessageResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
        validateStructType(resource.getName(), paramDetails.get(1), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_BINARY_FRAME);

    }

    private static void validateOnPingResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
        validateStructType(resource.getName(), paramDetails.get(1), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_PING_FRAME);

    }

    private static void validateOnPongResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
        validateStructType(resource.getName(), paramDetails.get(1), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_PONG_FRAME);

    }

    private static void validateOnIdleTimeoutResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 1, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
    }

    private static void validateOnCloseResource(String serviceName, Resource resource, boolean isClientService) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource.getName(), isClientService);
        validateStructType(resource.getName(), paramDetails.get(0), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
        validateStructType(resource.getName(), paramDetails.get(1), Constants.PROTOCOL_PACKAGE_WS,
                           Constants.STRUCT_WEBSOCKET_CLOSE_FRAME);
    }

    private static void validateParamDetailsSize(List<ParamDetail> paramDetails, int expectedSize,String serviceName,
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
