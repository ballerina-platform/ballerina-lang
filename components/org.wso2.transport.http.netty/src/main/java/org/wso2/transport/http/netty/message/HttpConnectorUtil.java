package org.wso2.transport.http.netty.message;

import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.config.TransportProperty;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Util class that provides common functionality.
 */
public class HttpConnectorUtil {

    /**
     * Extract sender configuration from transport configuration.
     *
     * @param transportsConfiguration {@link TransportsConfiguration} which sender configurations should be extracted.
     * @param scheme scheme of the transport.
     * @return extracted {@link SenderConfiguration}.
     */
    public static SenderConfiguration getSenderConfiguration(TransportsConfiguration transportsConfiguration,
                                                             String scheme) {
        Map<String, SenderConfiguration> senderConfigurations =
                transportsConfiguration.getSenderConfigurations().stream().collect(Collectors
                        .toMap(senderConf ->
                                senderConf.getScheme().toLowerCase(Locale.getDefault()), config -> config));

        return Constants.HTTPS_SCHEME.equals(scheme) ?
                senderConfigurations.get(Constants.HTTPS_SCHEME) : senderConfigurations.get(Constants.HTTP_SCHEME);
    }

    /**
     * Extract transport properties from transport configurations.
     *
     * @param transportsConfiguration transportsConfiguration {@link TransportsConfiguration} which transport
     *                                properties should be extracted.
     * @return Map of transport properties.
     */
    public static Map<String, Object> getTransportProperties(TransportsConfiguration transportsConfiguration) {
        Map<String, Object> transportProperties = new HashMap<>();
        Set<TransportProperty> transportPropertiesSet = transportsConfiguration.getTransportProperties();
        if (transportPropertiesSet != null && !transportPropertiesSet.isEmpty()) {
            transportProperties = transportPropertiesSet.stream().collect(
                    Collectors.toMap(TransportProperty::getName, TransportProperty::getValue));

        }
        return transportProperties;
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
        return new ServerBootstrapConfiguration(transportProperties);
    }

    private HttpConnectorUtil() {
    }
}
