package org.ballerinalang.net.grpc.nativeimpl;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.net.grpc.EndpointConstants;
import org.ballerinalang.net.grpc.config.EndpointConfiguration;
import org.ballerinalang.net.grpc.ssl.SSLConfig;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static org.wso2.carbon.launcher.utils.Utils.getSystemVariableValue;

/**
 * Endpoint Configuration generation Util.
 */
public class EndpointUtils {
    public static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]*)}");
    
    /**
     * Generate server endpoint object from service endpoint configuration struct.
     * @param endpointConfig  service endpoint configuration struct.
     * @return  server endpoint object
     */
    public static EndpointConfiguration getEndpointConfiguration(Struct endpointConfig) {
        String host = endpointConfig.getStringField(EndpointConstants.ENDPOINT_CONFIG_HOST);
        long port = endpointConfig.getIntField(EndpointConstants.ENDPOINT_CONFIG_PORT);
        Struct sslConfig = endpointConfig.getStructField(EndpointConstants.ENDPOINT_CONFIG_SSL);
        
        EndpointConfiguration endpointConfiguration = new EndpointConfiguration();
        
        if (host == null || host.isEmpty()) {
            endpointConfiguration.setHost(EndpointConstants.HTTP_DEFAULT_HOST);
        } else {
            endpointConfiguration.setHost(host);
        }
        endpointConfiguration.setPort(Math.toIntExact(port));
        
        if (sslConfig != null) {
            endpointConfiguration.setScheme(EndpointConstants.PROTOCOL_HTTPS);
            endpointConfiguration.setSslConfig(getSslConfig(sslConfig));
            return endpointConfiguration;
        }
        return endpointConfiguration;
    }
    
    /**
     * Generate SSL configs.
     * @param sslConfig service endpoint configuration struct.
     * @return SSL beans.
     */
    private static SSLConfig getSslConfig(Struct sslConfig) {
        String keyStoreFile = sslConfig.getStringField(EndpointConstants.SSL_CONFIG_KEY_STORE_FILE);
        String keyStorePassword = sslConfig.getStringField(EndpointConstants.SSL_CONFIG_KEY_STORE_PASSWORD);
        String trustStoreFile = sslConfig.getStringField(EndpointConstants.SSL_CONFIG_STRUST_STORE_FILE);
        String trustStorePassword = sslConfig.getStringField(EndpointConstants.SSL_CONFIG_STRUST_STORE_PASSWORD);
        String sslVerifyClient = sslConfig.getStringField(EndpointConstants.SSL_CONFIG_SSL_VERIFY_CLIENT);
        String certPassword = sslConfig.getStringField(EndpointConstants.SSL_CONFIG_CERT_PASSWORD);
        String sslProtocol = sslConfig.getStringField(EndpointConstants.SSL_CONFIG_SSL_PROTOCOL);
        boolean validateCertificateEnabled = sslConfig.getBooleanField(EndpointConstants
                .SSL_CONFIG_VALIDATE_CERT_ENABLED);
        long cacheSize = sslConfig.getIntField(EndpointConstants.SSL_CONFIG_CACHE_SIZE);
        long cacheValidationPeriod = sslConfig.getIntField(EndpointConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
        
        if (keyStoreFile == null) {
            //TODO get from language pack, and add location
            throw new BallerinaConnectorException("Keystore location must be provided for secure connection");
        }
        if (keyStorePassword == null) {
            //TODO get from language pack, and add location
            throw new BallerinaConnectorException("Keystore password value must be provided for secure connection");
        }
        if (certPassword == null) {
            //TODO get from language pack, and add location
            throw new BallerinaConnectorException("Certificate password value must be provided for secure connection");
        }
        if ((trustStoreFile == null) && sslVerifyClient != null) {
            //TODO get from language pack, and add location
            throw new BallerinaException("Truststore location must be provided to enable Mutual SSL");
        }
        if ((trustStorePassword == null) && sslVerifyClient != null) {
            //TODO get from language pack, and add location
            throw new BallerinaException("Truststore password value must be provided to enable Mutual SSL");
        }
        
        SSLConfig config = new SSLConfig();
        config.setTLSStoreType(EndpointConstants.PKCS_STORE_TYPE);
        if(keyStoreFile != null) {
            config.setKeyStore(new File(substituteVariables(keyStoreFile)));
            config.setKeyStorePass(keyStorePassword);
        }
        config.setCertPass(certPassword);
        
        config.setSslVerifyClient(sslVerifyClient);
        if(trustStoreFile != null) {
            config.setTrustStore(new File(substituteVariables(trustStoreFile)));
            config.setTrustStorePass(trustStorePassword);
        }
        config.setValidateCertificateEnabled(validateCertificateEnabled);
        if (validateCertificateEnabled) {
            config.setCacheSize(Math.toIntExact(cacheSize));
            config.setCacheValidityPeriod(Math.toIntExact(cacheValidationPeriod));
        }
        
        if (sslProtocol != null) {
            config.setSslProtocol(sslProtocol);
        }
        
        return config;
    }
    
    /**
     * Resolve and Validate file path.
     * @param filePath file path
     * @return resolved file path
     */
    private static String substituteVariables(String filePath) {
        Matcher matcher = VAR_PATTERN.matcher(filePath);
        boolean found = matcher.find();
        if (!found) {
            return filePath;
        }
        StringBuffer sb = new StringBuffer();
        do {
            String sysPropKey = matcher.group(1);
            String sysPropValue = getSystemVariableValue(sysPropKey, null);
            if (sysPropValue == null || sysPropValue.length() == 0) {
                throw new RuntimeException("System property " + sysPropKey + " is not specified");
            }
            // Due to reported bug under CARBON-14746
            sysPropValue = sysPropValue.replace("\\", "\\\\");
            matcher.appendReplacement(sb, sysPropValue);
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }
}
