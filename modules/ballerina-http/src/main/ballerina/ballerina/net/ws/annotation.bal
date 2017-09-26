package ballerina.net.ws;

annotation configuration attach service {
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

annotation clientService attach service {
}