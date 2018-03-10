package org.ballerinalang.net.http.serviceendpoint;

import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketServicesRegistry;
import org.wso2.transport.http.netty.contract.ServerConnector;

public abstract class AbstractHttpNativeFunction extends AbstractNativeFunction {

    protected HTTPServicesRegistry getHttpServicesRegistry(Struct serviceEndpoint) {
        return (HTTPServicesRegistry) serviceEndpoint.getNativeData(HttpConstants.HTTP_SERVICE_REGISTRY);
    }

    protected WebSocketServicesRegistry getWebSocketServicesRegistry(Struct serviceEndpoint) {
        return (WebSocketServicesRegistry) serviceEndpoint.getNativeData(HttpConstants.WS_SERVICE_REGISTRY);
    }

    protected ServerConnector getServerConnector(Struct serviceEndpoint) {
        return (ServerConnector) serviceEndpoint.getNativeData(HttpConstants.HTTP_SERVER_CONNECTOR);
    }

}
