package org.wso2.carbon.transport.http.netty.message;

import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.Parameter;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Util class that provides common functionality.
 */
public class HTTPConnectorUtil {

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
        ServerBootstrapConfiguration.createBootStrapConfiguration(transportProperties);
        return ServerBootstrapConfiguration.getInstance();
    }

    /**
     * Method to build listener configuration using provided properties map.
     *
     * @param id            HttpConnectorListener id
     * @param properties    Property map
     * @return              listener config
     */
    public static ListenerConfiguration buildListenerConfig(String id, Map<String, String> properties) {
        String host = properties.get(Constants.HTTP_HOST) != null ?
                properties.get(Constants.HTTP_HOST) : Constants.HTTP_DEFAULT_HOST;
        int port = Integer.parseInt(properties.get(Constants.HTTP_PORT));
        ListenerConfiguration config = new ListenerConfiguration(id, host, port);
        String scheme = properties.get(Constants.SCHEME);
        if (scheme != null && scheme.equals(Constants.HTTPS_SCHEME)) {
            config.setScheme(scheme);
            config.setKeyStoreFile(properties.get(Constants.HTTP_KEY_STORE_FILE));
            config.setKeyStorePass(properties.get(Constants.HTTP_KEY_STORE_PASS));
            config.setCertPass(properties.get(Constants.HTTP_CERT_PASS));
            if (properties.get(Constants.HTTP_TRUST_STORE_FILE) != null) {
                config.setTrustStoreFile(properties.get(Constants.HTTP_TRUST_STORE_FILE));
            }
            if (properties.get(Constants.HTTP_TRUST_STORE_PASS) != null) {
                config.setTrustStorePass(properties.get(Constants.HTTP_TRUST_STORE_PASS));
            }
            if (properties.get(Constants.SSL_VERIFY_CLIENT) != null) {
                config.setVerifyClient(properties.get(Constants.SSL_VERIFY_CLIENT));
            }
            if (properties.get(Constants.TLS_STORE_TYPE) != null) {
                config.setTlsStoreType(properties.get(Constants.TLS_STORE_TYPE));
            }
            List<Parameter> serverParams = new ArrayList<>();
            if (properties.get(Constants.SERVER_SUPPORT_SSL_PROTOCOLS) != null) {
                Parameter serverProtocols = new Parameter(Constants.SERVER_SUPPORT_SSL_PROTOCOLS,
                        properties.get(Constants.SERVER_SUPPORT_SSL_PROTOCOLS));
                serverParams.add(serverProtocols);
            }
            if (properties.get(Constants.SERVER_SUPPORT_CIPHERS) != null) {
                Parameter serverCiphers = new Parameter(Constants.SERVER_SUPPORT_CIPHERS,
                        properties.get(Constants.SERVER_SUPPORT_CIPHERS));
                serverParams.add(serverCiphers);
            }
            if (!serverParams.isEmpty()) {
                config.setParameters(serverParams);
            }
            if (properties.get(Constants.SSL_PROTOCOL) != null) {
                config.setSslProtocol(properties.get(Constants.SSL_PROTOCOL));
            }
        }
        return config;
    }

    public static String getListenerInterface(Map<String, String> parameters) {
        String host = parameters.get("host") != null ? parameters.get("host") : "0.0.0.0";
        int port = Integer.parseInt(parameters.get("port"));
        return host + ":" + port;
    }
}
