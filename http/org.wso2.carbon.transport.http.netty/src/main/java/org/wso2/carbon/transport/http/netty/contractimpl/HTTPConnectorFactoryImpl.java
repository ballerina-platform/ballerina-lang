package org.wso2.carbon.transport.http.netty.contractimpl;

import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.contractimpl.websocket.WebSocketClientConnectorImpl;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.listener.ServerConnectorBootstrap;
import org.wso2.carbon.transport.http.netty.sender.channel.BootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.Map;

/**
 * Implementation of HTTPConnectorFactory interface
 */
public class HTTPConnectorFactoryImpl implements HTTPConnectorFactory {

    private static final String NETTY_TRANSPORT_CONF = "transports.netty.conf";

    @Override
    public ServerConnector getServerConnector(ServerBootstrapConfiguration serverBootstrapConfiguration,
            ListenerConfiguration listenerConfig) {
        ServerConnectorBootstrap serverConnectorBootstrap = new ServerConnectorBootstrap(listenerConfig);
        serverConnectorBootstrap.addSocketConfiguration(serverBootstrapConfiguration);
        serverConnectorBootstrap.addSecurity(listenerConfig);
        serverConnectorBootstrap.addIdleTimeout(listenerConfig);
        serverConnectorBootstrap.addThreadPools(Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 2);

        return serverConnectorBootstrap.getServerConnector(listenerConfig);
    }

    @Override
    public HTTPClientConnector getHTTPClientConnector(Map<String, Object> transportProperties,
            SenderConfiguration senderConfiguration) {
        SSLConfig sslConfig = senderConfiguration.getSslConfig();
        int socketIdleTimeout = senderConfiguration.getSocketIdleTimeout(60000);

        ConnectionManager.init(transportProperties);
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        BootstrapConfiguration.createBootStrapConfiguration(transportProperties);

        return new HTTPClientConnectorImpl(connectionManager, sslConfig, socketIdleTimeout);
    }

    @Override
    public WebSocketClientConnector getWSClientConnector(Map<String, String> connectorConfig) {
        String subProtocol = connectorConfig.get(Constants.WEBSOCKET_SUBPROTOCOLS);
        String remoteUrl = connectorConfig.get(Constants.REMOTE_ADDRESS);
        String target = connectorConfig.get(Constants.TO);

        // TODO: Allow extensions when any type is supported.
        boolean allowExtensions = true;
        return new WebSocketClientConnectorImpl(remoteUrl, target, subProtocol, allowExtensions);
    }
}
