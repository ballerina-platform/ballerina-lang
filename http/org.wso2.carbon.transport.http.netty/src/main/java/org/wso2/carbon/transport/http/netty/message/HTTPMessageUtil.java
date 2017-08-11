package org.wso2.carbon.transport.http.netty.message;

import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.carbon.transport.http.netty.contractimpl.websocket.WebSocketMessageContextImpl;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Util class that provides common functionality.
 */
public class HTTPMessageUtil {

    /**
     * Convert {@link CarbonMessage} to a {@link HTTPCarbonMessage}.
     *
     * @param carbonMessage {@link CarbonMessage} which should be converted to a {@link HTTPCarbonMessage}.
     * @return converted {@link HTTPCarbonMessage}.
     */
    public static HTTPCarbonMessage convertCarbonMessage(CarbonMessage carbonMessage) {
        HTTPCarbonMessage httpCarbonMessage = new HTTPCarbonMessage();

        List<Header> transportHeaders = carbonMessage.getHeaders().getClone();
        httpCarbonMessage.setHeaders(transportHeaders);

        Map<String, Object> propertiesMap = carbonMessage.getProperties();
        propertiesMap.forEach((key, value) -> httpCarbonMessage.setProperty(key, value));

        httpCarbonMessage.setWriter(carbonMessage.getWriter());
        httpCarbonMessage.setFaultHandlerStack(carbonMessage.getFaultHandlerStack());

        carbonMessage.getFullMessageBody().forEach((buffer) -> httpCarbonMessage.addMessageBody(buffer));

        httpCarbonMessage.setEndOfMsgAdded(carbonMessage.isEndOfMsgAdded());

        return httpCarbonMessage;
    }

    public static CarbonMessage convertWebSocketInitMessage(WebSocketInitMessage initMessage) {
        StatusCarbonMessage carbonMessage =
                new StatusCarbonMessage(org.wso2.carbon.messaging.Constants.STATUS_OPEN, 0, null);
        if (initMessage instanceof WebSocketMessageContextImpl) {
            WebSocketMessageContextImpl webSocketInitMessage = (WebSocketMessageContextImpl) initMessage;
            return setWebSocketCommonProperties(carbonMessage, webSocketInitMessage);
        }
        throw new UnsupportedOperationException("Message conversion is not supported");
    }

    public static CarbonMessage convertWebSocketTextMessage(WebSocketTextMessage textMessage) {
        TextCarbonMessage carbonMessage = new TextCarbonMessage(textMessage.getText());
        if (textMessage instanceof WebSocketMessageContextImpl) {
            WebSocketMessageContextImpl webSocketInitMessage = (WebSocketMessageContextImpl) textMessage;
            return setWebSocketCommonProperties(carbonMessage, webSocketInitMessage);
        }
        throw new UnsupportedOperationException("Message conversion is not supported");
    }

    public static CarbonMessage convertWebSocketBinaryMessage(WebSocketBinaryMessage binaryMessage) {
        BinaryCarbonMessage carbonMessage =
                new BinaryCarbonMessage(binaryMessage.getByteBuffer(), binaryMessage.isFinalFragment());
        if (binaryMessage instanceof WebSocketMessageContextImpl) {
            WebSocketMessageContextImpl webSocketInitMessage = (WebSocketMessageContextImpl) binaryMessage;
            return setWebSocketCommonProperties(carbonMessage, webSocketInitMessage);
        }
        throw new UnsupportedOperationException("Message conversion is not supported");
    }

    public static CarbonMessage convertWebSocketCloseMessage(WebSocketCloseMessage closeMessage) {
        StatusCarbonMessage carbonMessage =
                new StatusCarbonMessage(org.wso2.carbon.messaging.Constants.STATUS_OPEN,
                                        closeMessage.getCloseCode(), closeMessage.getCloseReason());
        if (closeMessage instanceof WebSocketMessageContextImpl) {
            WebSocketMessageContextImpl webSocketInitMessage = (WebSocketMessageContextImpl) closeMessage;
            return setWebSocketCommonProperties(carbonMessage, webSocketInitMessage);
        }
        throw new UnsupportedOperationException("Message conversion is not supported");
    }

    private static CarbonMessage setWebSocketCommonProperties(CarbonMessage cMsg,
                                                              WebSocketMessageContextImpl messageContext) {
        cMsg.setProperty(Constants.TO, messageContext.getTarget());
        cMsg.setProperty(Constants.SRC_HANDLER, messageContext.getProperty(Constants.SRC_HANDLER));
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT,
                         messageContext.getProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT));
        cMsg.setProperty(Constants.IS_SECURED_CONNECTION, messageContext.isConnectionSecured());
        cMsg.setProperty(Constants.LOCAL_ADDRESS, messageContext.getProperty(Constants.LOCAL_ADDRESS));
        cMsg.setProperty(Constants.LOCAL_NAME, messageContext.getProperty(Constants.LOCAL_NAME));
        cMsg.setProperty(Constants.CHANNEL_ID, messageContext.getProperty(Constants.CHANNEL_ID));
        cMsg.setProperty(Constants.PROTOCOL, Constants.WEBSOCKET_PROTOCOL);
        cMsg.setProperty(Constants.IS_WEBSOCKET_SERVER, true);
        cMsg.setProperty(Constants.WEBSOCKET_SERVER_SESSION, messageContext.getServerSession());
        cMsg.setProperty(Constants.WEBSOCKET_CLIENT_SESSIONS_LIST, messageContext.getClientSessions());
        cMsg.setProperty(Constants.WEBSOCKET_CLIENT_SESSION, messageContext.getChannelSession());
        cMsg.setProperty(Constants.WEBSOCKET_MESSAGE_CONTEXT, messageContext);
        return cMsg;
    }

    /**
     * Extract sender configuration from transport configuration.
     *
     * @param transportsConfiguration {@link TransportsConfiguration} which sender configurations should be extracted.
     * @return extracted {@link SenderConfiguration}.
     */
    public static SenderConfiguration getSenderConfiguration(TransportsConfiguration transportsConfiguration) {
        Map<String, SenderConfiguration> senderConfigurations =
                transportsConfiguration.getSenderConfigurations().stream().collect(Collectors
                        .toMap(senderConf ->
                                senderConf.getScheme().toLowerCase(Locale.getDefault()), config -> config));
        return senderConfigurations.get("http");
    }

    /**
     * Create server bootstrap configuration from given transport property set.
     *
     * @param transportPropertiesSet Set of transport properties which should be converted
     *                               to {@link ServerBootstrapConfiguration}.
     * @return ServerBootstrapConfiguration which is created from given Set of transport properties.
     */
    public static ServerBootstrapConfiguration getServerBootstrapConfiguration(Set<TransportProperty>
            transportPropertiesSet) {
        Map<String, Object> transportProperties = new HashMap<>();

        if (transportPropertiesSet != null && !transportPropertiesSet.isEmpty()) {
            transportProperties = transportPropertiesSet.stream().collect(
                    Collectors.toMap(TransportProperty::getName, TransportProperty::getValue));
        }
        // Create Bootstrap Configuration from listener parameters
        ServerBootstrapConfiguration.createBootStrapConfiguration(transportProperties);
        return ServerBootstrapConfiguration.getInstance();
    }

    /**
     * Method to build listener configuration using provided properties map.
     *
     * @param id            HTTPConnectorListener id
     * @param properties    Property map
     * @return              listener config
     */
    public static ListenerConfiguration buildListenerConfig(String id, Map<String, String> properties) {
        String host = properties.get(Constants.HTTP_HOST) != null ?
                properties.get(Constants.HTTP_HOST) : Constants.HTTP_DEFAULT_HOST;
        int port = Integer.parseInt(properties.get(Constants.HTTP_PORT));
        ListenerConfiguration config = new ListenerConfiguration(id, host, port);
        String schema = properties.get(Constants.HTTP_SCHEME);
        if (schema != null && schema.equals("https")) {
            config.setScheme(schema);
            config.setKeyStoreFile(properties.get(Constants.HTTP_KEY_STORE_FILE));
            config.setKeyStorePass(properties.get(Constants.HTTP_KEY_STORE_PASS));
            config.setCertPass(properties.get(Constants.HTTP_CERT_PASS));
            //todo fill truststore stuff
        }
        return config;
    }
}
