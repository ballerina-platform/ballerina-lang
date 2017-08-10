package org.wso2.carbon.transport.http.netty.message;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Header;
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

        httpCarbonMessage.addMessageBody(carbonMessage.getMessageBody());

        while (true) {
            if (carbonMessage.isEndOfMsgAdded() || carbonMessage.isEmpty()) {
                break;
            }
            httpCarbonMessage.addMessageBody(carbonMessage.getMessageBody());
        }

        httpCarbonMessage.setEndOfMsgAdded(true);

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
}
