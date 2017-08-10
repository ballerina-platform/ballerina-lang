package org.wso2.carbon.transport.http.netty.contractimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnectorFuture;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

/**
 * HTTPClientConnectorImpl
 */
public class HTTPClientConnectorImpl implements HTTPClientConnector {

    private static final Logger log = LoggerFactory.getLogger(HTTPClientConnector.class);

    private ConnectionManager connectionManager;
    private SSLConfig sslConfig;
    private int socketIdleTimeout;

    public HTTPClientConnectorImpl(ConnectionManager connectionManager, SSLConfig sslConfig, int socketIdleTimeout) {
        this.connectionManager = connectionManager;
        this.sslConfig = sslConfig;
        this.socketIdleTimeout = socketIdleTimeout;
    }

    @Override
    public HTTPClientConnectorFuture connect() {
        return null;
    }

    @Override
    public HTTPClientConnectorFuture send(HTTPCarbonMessage httpCarbonMessage) throws Exception {
        HTTPClientConnectorFuture httpClientConnectorFuture = new HTTPClientConnectorFutureImpl();

        // Fetch Host
        String host;
        Object hostProperty = httpCarbonMessage.getProperty(Constants.HOST);
        if (hostProperty != null && hostProperty instanceof String) {
            host = (String) hostProperty;
        } else {
            host = Constants.LOCALHOST;
            httpCarbonMessage.setProperty(Constants.HOST, Constants.LOCALHOST);
            log.debug("Cannot find property HOST of type string, hence using localhost as the host");
        }

        // Fetch Port
        int port;
        Object intProperty = httpCarbonMessage.getProperty(Constants.PORT);
        if (intProperty != null && intProperty instanceof Integer) {
            port = (int) intProperty;
        } else {
            port = sslConfig != null ?
                    Constants.DEFAULT_HTTPS_PORT : Constants.DEFAULT_HTTP_PORT;
            httpCarbonMessage.setProperty(Constants.PORT, port);
            log.debug("Cannot find property PORT of type integer, hence using " + port);
        }

        final HttpRoute route = new HttpRoute(host, port);

        SourceHandler srcHandler = (SourceHandler) httpCarbonMessage.getProperty(Constants.SRC_HANDLER);
        if (srcHandler == null) {
            log.debug("SRC_HANDLER property not found in the message." +
                    " Message is not originated from the HTTP Server connector");
        }

        try {
            connectionManager.executeTargetChannel(route, srcHandler, sslConfig,
                            httpCarbonMessage, socketIdleTimeout, httpClientConnectorFuture);
        } catch (Exception failedCause) {
            throw new ClientConnectorException(failedCause.getMessage(), failedCause);
        }

        return httpClientConnectorFuture;
    }

    @Override
    public boolean stop() {
        return false;
    }
}
