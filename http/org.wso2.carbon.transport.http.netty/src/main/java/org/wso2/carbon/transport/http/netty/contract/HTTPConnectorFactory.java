package org.wso2.carbon.transport.http.netty.contract;

import java.util.Map;

/**
 * Allows you create server and client connectors
 */
public interface HTTPConnectorFactory {
    ServerConnector getServerConnector(Map<String, String> connectorConfig);
    ClientConnector getClientConnector(Map<String, String> connectorConfig);
}
