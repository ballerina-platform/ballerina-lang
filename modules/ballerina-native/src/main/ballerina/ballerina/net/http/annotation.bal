package ballerina.net.http;

import ballerina.net.ws;

annotation configuration attach service<>, service<ws> {
    string host;
    int port;
    int httpsPort;
    string basePath;
    string keyStoreFile;
    string keyStorePass;
    string certPass;
}

annotation resourceConfig attach resource {
    string[] methods;
    string path;
    string[] consumes;
    string[] produces;
}

annotation PathParam attach parameter {
    string value;
}

annotation QueryParam attach parameter {
    string value;
}