package org.ballerinalang.net.grpc;

/**
 * Created by daneshk on 3/10/18.
 */
public class EndpointConstants {
    public static final String CHANNEL_KEY = "channel";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String PKCS_STORE_TYPE = "PKCS12";
    public static final String HTTP_DEFAULT_HOST = "0.0.0.0";
    public static final String ENDPOINT_CONFIG = "config";

    //Service Endpoint Config
    public static final String ENDPOINT_CONFIG_HOST = "host";
    public static final String ENDPOINT_CONFIG_TRANSFER_ENCODING = "transferEncoding";
    public static final String ENDPOINT_CONFIG_PORT = "port";
    public static final String ENDPOINT_CONFIG_KEEP_ALIVE = "keepAlive";
    public static final String ENDPOINT_CONFIG_CHUNKING = "chunking";
    public static final String ENDPOINT_CONFIG_SSL = "ssl";

    //SslConfiguration indexes
    public static final String SSL_CONFIG_KEY_STORE_FILE = "keyStoreFile";
    public static final String SSL_CONFIG_KEY_STORE_PASSWORD = "keyStorePassword";
    public static final String SSL_CONFIG_STRUST_STORE_FILE = "trustStoreFile";
    public static final String SSL_CONFIG_STRUST_STORE_PASSWORD = "trustStorePassword";
    public static final String SSL_CONFIG_SSL_VERIFY_CLIENT = "sslVerifyClient";
    public static final String SSL_CONFIG_CERT_PASSWORD = "certPassword";
    public static final String SSL_CONFIG_SSL_ENABLED_PROTOCOLS = "sslEnabledProtocols";
    public static final String SSL_CONFIG_CIPHERS = "ciphers";
    public static final String SSL_CONFIG_SSL_PROTOCOL = "sslProtocol";
    public static final String SSL_CONFIG_CACHE_SIZE = "cacheSize";
    public static final String SSL_CONFIG_CACHE_VALIDITY_PERIOD = "cacheValidityPeriod";
    public static final String SSL_CONFIG_VALIDATE_CERT_ENABLED = "validateCertEnabled";

    public static final String ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS = "sslEnabledProtocols";
    public static final String ANN_CONFIG_ATTR_CIPHERS = "ciphers";

}
