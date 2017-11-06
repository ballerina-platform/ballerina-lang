package ballerina.net.ws;

@Description {value: "Configuration for a WebSocket service."}
public annotation configuration attach service<> {
    string basePath;
    string[] subProtocols;
    string host;
    int port;
    int wssPort;
    int idleTimeoutInSeconds;
    string keyStoreFile;
    string keyStorePassword;
    string certPassword;
}

@Description {value: "Service annotation to mark a WS service as a client service for a WS client connector."}
public annotation clientService attach service<> {
}