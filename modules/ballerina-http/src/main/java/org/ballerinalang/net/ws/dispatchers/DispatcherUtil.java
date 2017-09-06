package org.ballerinalang.net.ws.dispatchers;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.ws.Constants;
import org.ballerinalang.net.ws.WebSocketServicesRegistry;
import org.ballerinalang.runtime.ServerConnectorMessageHandler;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketMessage;

/**
 * Created by rajith on 9/5/17.
 */
public class DispatcherUtil {

    public static HttpService findService(CarbonMessage message, WebSocketMessage webSocketMessage) {
        if (!webSocketMessage.isServerMessage()) {
            String clientServiceName = webSocketMessage.getTarget();
            HttpService clientService = WebSocketServicesRegistry.getInstance().getClientService(clientServiceName);
            if (clientService == null) {
                throw new BallerinaConnectorException("no client service found to handle the service request");
            }
            return clientService;
        }
        try {
            String interfaceId = webSocketMessage.getListenerInterface();
            String serviceUri = webSocketMessage.getTarget();
            serviceUri = WebSocketServicesRegistry.getInstance().refactorUri(serviceUri);

            HttpService service =
                    WebSocketServicesRegistry.getInstance().getServiceEndpoint(interfaceId, serviceUri);

            if (service == null) {
                throw new BallerinaConnectorException("no Service found to handle the service request: " + serviceUri);
            }
            return service;
        } catch (Throwable throwable) {
            ServerConnectorMessageHandler.handleError(message, null, throwable);
            throw new BallerinaConnectorException("no Service found to handle the service request");
        }

    }

    public static Resource getResource(HttpService service, String annotationName) {
        for (Resource resource : service.getResources()) {
            if (resource.getAnnotation(Constants.WEBSOCKET_PACKAGE_PATH, annotationName) != null) {
                return resource;
            }
        }
        return null;
    }
}
