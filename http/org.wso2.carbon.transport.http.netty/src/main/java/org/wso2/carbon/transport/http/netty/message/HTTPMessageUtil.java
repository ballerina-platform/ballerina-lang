package org.wso2.carbon.transport.http.netty.message;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
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

    public static SenderConfiguration getSenderConfiguration(TransportsConfiguration transportsConfiguration) {
        Map<String, SenderConfiguration> senderConfigurations =
                transportsConfiguration.getSenderConfigurations().stream().collect(Collectors
                        .toMap(senderConf ->
                                senderConf.getScheme().toLowerCase(Locale.getDefault()), config -> config));
        return senderConfigurations.get("http");
    }

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
    public ListenerConfiguration buildListenerConfig(String id, Map<String, String> properties) {
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
