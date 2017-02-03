package org.wso2.ballerina.core.nativeimpl.connectors.http.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.nativeimpl.connectors.http.Constants;
import org.wso2.ballerina.core.runtime.dispatching.ServiceDispatcher;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Dispatcher for WebSocket protocol.
 *
 * @since 0.8.0
 */
public class WebSocketServiceDispatcher implements ServiceDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServiceDispatcher.class);

    private Map<String, Service> services = new HashMap<>();

    @Override
    public Service findService(CarbonMessage cMsg, CarbonCallback callback, Context balContext) {
        String interfaceId = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Interface id not found on the message, hence using the default interface");
            }
        }

        String serviceUri = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.TO);
        if (serviceUri == null) {
            throw new BallerinaException("No service found to dispatch");
        }

        serviceUri = refactorUri(serviceUri);

        Service service = services.get(serviceUri);

        if (service == null) {
            throw new BallerinaException("No service found to handle message for " + serviceUri);
        }

        return service;
    }


    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_WEBSOCKET;
    }


    @Override
    public void serviceRegistered(Service service) {
        String websocketUri = findWebSocketPath(service);
        if (websocketUri != null) {
            services.put(websocketUri, service);
        }
    }

    @Override
    public void serviceUnregistered(Service service) {
        String websocketUri = findWebSocketPath(service);
        if (websocketUri != null) {
            services.remove(websocketUri);
        }
    }

    private String findWebSocketPath(Service service) {
        Annotation websocketUpgradePathAnnotation = null;
        Annotation basePathAnnotation = null;
        Annotation[] annotations = service.getAnnotations();
        for (Annotation annotation: annotations) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePathAnnotation = annotation;
            } else if (annotation.getName().equals(Constants.ANNOTATION_NAME_WEBSOCKET_UPGRADE_PATH)) {
                websocketUpgradePathAnnotation = annotation;
            }
        }
        if (websocketUpgradePathAnnotation != null && websocketUpgradePathAnnotation.getValue() != null) {
            if (basePathAnnotation == null) {
                throw new BallerinaException("Cannot define @WebSocketPathUpgrade without @BasePath");
            }

            String basePath = refactorUri(basePathAnnotation.getValue());
            String websocketUpgradePath = refactorUri(websocketUpgradePathAnnotation.getValue());

            return basePath.concat(websocketUpgradePath);
        }

        return null;
    }

    private String refactorUri(String uri) {
        if (uri.startsWith("\"")) {
            uri = uri.substring(1, uri.length() - 1);
        }

        if (!uri.startsWith("/")) {
            uri = "/".concat(uri);
        }

        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }
}
