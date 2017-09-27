package ballerina.net.ws;

annotation configuration attach service {
    string basePath;
    string[] subProtocols;
    string host;
    int port;
    int wssPort;
    int idleTimeOutSeconds;
    string keyStoreFile;
    string keyStorePassword;
    string certPassword;
}

annotation ClientService attach service {
}