package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;

import java.util.Map;

/**
 * Allows you create server and client connectors
 */
public interface HTTPConnectorFactory {
    ServerConnector getServerConnector(ServerBootstrapConfiguration serverBootstrapConfiguration,
            ListenerConfiguration listenerConfiguration);
    HTTPClientConnector getHTTPClientConnector(Map<String, Object> transportProperties,
            SenderConfiguration senderConfiguration);
    WebSocketClientConnector getWSClientConnector(Map<String, String> connectorConfig);
}
