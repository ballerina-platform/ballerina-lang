package org.ballerinalang.net.http.serviceendpoint;

import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketServicesRegistry;
import org.ballerinalang.net.http.WebSubSubscriberConstants;
import org.ballerinalang.net.http.websub.WebSubServicesRegistry;
import org.wso2.transport.http.netty.contract.ServerConnector;

/**
 * Includes common functions to all the actions.
 *
 * @since 0.966
 */
public abstract class AbstractHttpNativeFunction extends BlockingNativeCallableUnit {

    protected HTTPServicesRegistry getHttpServicesRegistry(Struct serviceEndpoint) {
        return (HTTPServicesRegistry) serviceEndpoint.getNativeData(HttpConstants.HTTP_SERVICE_REGISTRY);
    }

    protected WebSocketServicesRegistry getWebSocketServicesRegistry(Struct serviceEndpoint) {
        return (WebSocketServicesRegistry) serviceEndpoint.getNativeData(HttpConstants.WS_SERVICE_REGISTRY);
    }

    protected WebSubServicesRegistry getWebSubServicesRegistry(Struct serviceEndpoint) {
        return (WebSubServicesRegistry) serviceEndpoint.getNativeData(
                WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY);
    }

    protected ServerConnector getServerConnector(Struct serviceEndpoint) {
        return (ServerConnector) serviceEndpoint.getNativeData(HttpConstants.HTTP_SERVER_CONNECTOR);
    }

}
