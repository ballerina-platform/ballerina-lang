package ballerina.net.http;

@Description { value:"Configuration for HTTP service"}
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

@Description {value: "Annotation to upgrade connection from http to ws in the same base path."}
public annotation webSocket attach service<> {
    string upgradePath;
    string serviceName;
}

@Description { value:"Configuration for HTTP resource"}
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