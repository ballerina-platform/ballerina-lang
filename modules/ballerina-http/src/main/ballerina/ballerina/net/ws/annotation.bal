package ballerina.net.ws;

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

public annotation clientService attach service<> {
}