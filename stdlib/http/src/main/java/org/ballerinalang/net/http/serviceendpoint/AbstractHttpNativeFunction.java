package org.ballerinalang.net.http.serviceendpoint;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.websocket.server.WebSocketServicesRegistry;
import org.wso2.transport.http.netty.contract.ServerConnector;

/**
 * Includes common functions to all the actions.
 *
 * @since 0.966
 */
public abstract class AbstractHttpNativeFunction {

    protected static HTTPServicesRegistry getHttpServicesRegistry(ObjectValue serviceEndpoint) {
        return (HTTPServicesRegistry) serviceEndpoint.getNativeData(HttpConstants.HTTP_SERVICE_REGISTRY);
    }

    protected static WebSocketServicesRegistry getWebSocketServicesRegistry(ObjectValue serviceEndpoint) {
        return (WebSocketServicesRegistry) serviceEndpoint.getNativeData(HttpConstants.WS_SERVICE_REGISTRY);
    }

    protected static ServerConnector getServerConnector(ObjectValue serviceEndpoint) {
        return (ServerConnector) serviceEndpoint.getNativeData(HttpConstants.HTTP_SERVER_CONNECTOR);
    }

    static boolean isConnectorStarted(ObjectValue serviceEndpoint) {
        return serviceEndpoint.getNativeData(HttpConstants.CONNECTOR_STARTED) != null &&
                (Boolean) serviceEndpoint.getNativeData(HttpConstants.CONNECTOR_STARTED);
    }

    static void resetRegistry(ObjectValue serviceEndpoint) {
        WebSocketServicesRegistry webSocketServicesRegistry = new WebSocketServicesRegistry();
        HTTPServicesRegistry httpServicesRegistry = new HTTPServicesRegistry(webSocketServicesRegistry);
        serviceEndpoint.addNativeData(HttpConstants.HTTP_SERVICE_REGISTRY, httpServicesRegistry);
        serviceEndpoint.addNativeData(HttpConstants.WS_SERVICE_REGISTRY, webSocketServicesRegistry);
    }
}
