package org.ballerinalang.net.http.serviceendpoint;

import io.ballerina.runtime.api.values.BObject;
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

    protected static HTTPServicesRegistry getHttpServicesRegistry(BObject serviceEndpoint) {
        return (HTTPServicesRegistry) serviceEndpoint.getNativeData(HttpConstants.HTTP_SERVICE_REGISTRY);
    }

    protected static WebSocketServicesRegistry getWebSocketServicesRegistry(BObject serviceEndpoint) {
        return (WebSocketServicesRegistry) serviceEndpoint.getNativeData(HttpConstants.WS_SERVICE_REGISTRY);
    }

    protected static ServerConnector getServerConnector(BObject serviceEndpoint) {
        return (ServerConnector) serviceEndpoint.getNativeData(HttpConstants.HTTP_SERVER_CONNECTOR);
    }

    static boolean isConnectorStarted(BObject serviceEndpoint) {
        return serviceEndpoint.getNativeData(HttpConstants.CONNECTOR_STARTED) != null &&
                (Boolean) serviceEndpoint.getNativeData(HttpConstants.CONNECTOR_STARTED);
    }

    static void resetRegistry(BObject serviceEndpoint) {
        WebSocketServicesRegistry webSocketServicesRegistry = new WebSocketServicesRegistry();
        HTTPServicesRegistry httpServicesRegistry = new HTTPServicesRegistry(webSocketServicesRegistry);
        serviceEndpoint.addNativeData(HttpConstants.HTTP_SERVICE_REGISTRY, httpServicesRegistry);
        serviceEndpoint.addNativeData(HttpConstants.WS_SERVICE_REGISTRY, webSocketServicesRegistry);
    }
}
