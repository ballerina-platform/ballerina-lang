package ballerina.net.http;

public annotation configuration attach service<> {
    string host;
    int port;
    int httpsPort;
    string basePath;
    string keyStoreFile;
    string keyStorePassword;
    string trustStoreFile;
    string trustStorePassword;
    string sslVerifyClient;
    string certPassword;
    string sslEnabledProtocols;
    string ciphers;
    string sslProtocol;
    string[] allowOrigins;
    boolean allowCredentials;
    string[] allowMethods;
    string[] allowHeaders;
    int maxAge;
    string[] exposeHeaders;
    webSocket webSocket;
}

public annotation webSocket {
    string upgradePath;
    string serviceName;
}

public annotation resourceConfig attach resource {
    string[] methods;
    string path;
    string[] consumes;
    string[] produces;
    string[] allowOrigins;
    boolean allowCredentials;
    string[] allowMethods;
    string[] allowHeaders;
    int maxAge;
    string[] exposeHeaders;
}