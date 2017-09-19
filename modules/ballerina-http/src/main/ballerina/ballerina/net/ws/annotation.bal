package ballerina.net.ws;

annotation configuration attach service {
    string basePath;
    string[] subProtocols;
    string host;
    int port;
    int wssPort;
    int idleTimeOutSeconds;
    string keyStoreFile;
    string keyStorePass;
    string certPass;
}

annotation ClientService attach service {
}