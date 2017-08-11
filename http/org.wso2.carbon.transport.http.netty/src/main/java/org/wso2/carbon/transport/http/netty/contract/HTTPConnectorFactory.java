package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;

import java.util.Map;

/**
 * Allows you create server and client connectors.
 */
public interface HTTPConnectorFactory {
    /**
     * This method can be used to get new server connectors.
     * @param serverBootstrapConfiguration configure socket related stuff.
     * @param listenerConfiguration contains SSL and socket bindings
     * @return connector that represents the server socket and additional details.
     */
    ServerConnector getServerConnector(ServerBootstrapConfiguration serverBootstrapConfiguration,
            ListenerConfiguration listenerConfiguration);

    /**
     * This method can be used to get http client connectors.
     * @param transportProperties configure stuff like global timeout, number of outbound connections, etc.
     * @param senderConfiguration contains SSL configuration and endpoint details.
     * @return
     */
    HTTPClientConnector getHTTPClientConnector(Map<String, Object> transportProperties,
            SenderConfiguration senderConfiguration);

    /**
     * This method can be used to get websockets client connector.
     * @param connectorConfig contains endpoint details.
     * @return the client connection that represents the connection.
     */
    WebSocketClientConnector getWSClientConnector(Map<String, String> connectorConfig);
}
