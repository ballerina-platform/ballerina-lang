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

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.List;

/**
 * Service validator for WebSocket.
 */
public class WebSocketServiceValidator {

    public static boolean validateClientService(WebSocketService wsService) {
        if (wsService.getAnnotation(Constants.WEBSOCKET_PACKAGE_NAME, Constants.ANNOTATION_CONFIGURATION) != null) {
            throw new BallerinaException(
                    String.format("Cannot define %s:%s annotation for WebSocket client service",
                                  Constants.WEBSOCKET_PACKAGE_NAME, Constants.ANNOTATION_CONFIGURATION));
        }
        return validateResources(wsService.getResources());
    }

    public static boolean validateServiceEndpoint(WebSocketService wsService) {
        if (wsService.getAnnotation(Constants.WEBSOCKET_PACKAGE_NAME,
                                    Constants.ANNOTATION_WEBSOCKET_CLIENT_SERVICE) != null) {
            throw new BallerinaException(
                    String.format("Cannot define %s:%s annotation for WebSocket client service",
                                  Constants.WEBSOCKET_PACKAGE_NAME, Constants.ANNOTATION_WEBSOCKET_CLIENT_SERVICE));
        }
        if (!validateConfigAnnotations(wsService)) {
            throw new BallerinaException("Cannot deploy WS service without ws:Configuration annotation");
        }
        return validateResources(wsService.getResources());
    }

    private static boolean validateResources(Resource[] resources) {
        for (Resource resource : resources) {
            String resourceName = resource.getName();
            if (resourceName.equals(Constants.RESOURCE_NAME_ON_HANDSHAKE)) {
                validateOnHandshakeResource(resource);
            } else if (resourceName.equals(Constants.RESOURCE_NAME_ON_OPEN)) {
                validateOnOpenResource(resource);
            } else if (resourceName.equals(Constants.RESOURCE_NAME_ON_TEXT_MESSAGE)) {
                validateOnTextMessageResource(resource);
            } else if (resourceName.equals(Constants.RESOURCE_NAME_ON_BINARY_MESSAGE)) {
                validateOnBinaryMessageResource(resource);
            } else if (resourceName.equals(Constants.RESOURCE_NAME_ON_PING)) {
                validateOnPingResource(resource);
            } else if (resourceName.equals(Constants.RESOURCE_NAME_ON_PONG)) {
                validateOnPongResource(resource);
            } else if (resourceName.equals(Constants.RESOURCE_NAME_ON_IDLE_TIMEOUT)) {
                validateOnIdleTimeoutResource(resource);
            } else if (resourceName.equals(Constants.RESOURCE_NAME_ON_CLOSE)) {
                validateOnCloseResource(resource);
            } else {
                throw new BallerinaException(String.format("Invalid resource name %s", resourceName));
            }
        }
        return true;
    }

    private static boolean validateConfigAnnotations(WebSocketService wsService) {
        Annotation configAnnotation =
                wsService.getAnnotation(Constants.WEBSOCKET_PACKAGE_NAME, Constants.ANNOTATION_CONFIGURATION);
        return configAnnotation != null;
    }

    /**
     * Find out the given service is a WebSocket client service or not.
     *
     * @param service {@link WebSocketService} which should be identified.
     * @return true if the given service is a client service.
     */
    public static boolean isWebSocketClientService(WebSocketService service) {
        Annotation annotation = service.getAnnotation(
                Constants.WEBSOCKET_PACKAGE_NAME, Constants.ANNOTATION_WEBSOCKET_CLIENT_SERVICE);
        return !(annotation == null);
    }

    private static void validateOnHandshakeResource(Resource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 1, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_HANDSHAKE_CONNECTION);
    }

    private static void validateOnOpenResource(Resource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 1, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
    }

    private static void validateOnTextMessageResource(Resource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
        validateStructType(resource.getName(), paramDetails.get(1), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_TEXT_FRAME);
    }

    private static void validateOnBinaryMessageResource(Resource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
        validateStructType(resource.getName(), paramDetails.get(1), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_BINARY_FRAME);

    }

    private static void validateOnPingResource(Resource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
        validateStructType(resource.getName(), paramDetails.get(1), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_PING_FRAME);

    }

    private static void validateOnPongResource(Resource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
        validateStructType(resource.getName(), paramDetails.get(1), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_PONG_FRAME);

    }

    private static void validateOnIdleTimeoutResource(Resource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 1, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
    }

    private static void validateOnCloseResource(Resource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();
        validateParamDetailsSize(paramDetails, 2, resource.getName());
        validateStructType(resource.getName(), paramDetails.get(0), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_CONNECTION);
        validateStructType(resource.getName(), paramDetails.get(1), Constants.WEBSOCKET_PACKAGE_NAME,
                           Constants.STRUCT_WEBSOCKET_CLOSE_FRAME);
    }

    private static void validateParamDetailsSize(List<ParamDetail> paramDetails,
                                                 int expectedSize, String resourceName) {
        if (paramDetails == null || paramDetails.size() < expectedSize || paramDetails.size() > expectedSize) {
            throw new BallerinaException(String.format("Invalid resource signature for %s", resourceName));
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
