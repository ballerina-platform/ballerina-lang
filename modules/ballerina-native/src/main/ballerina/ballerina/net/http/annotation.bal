package ballerina.net.http;

annotation GET attach resource {
}

annotation POST attach resource {
}

annotation PUT attach resource {
}

annotation DELETE attach resource {
}

annotation HEAD attach resource {
}

annotation Path attach resource {
    string value;
}

annotation configuration attach service<> {
    string host;
    int port;
    int httpsPort;
    string basePath;
    string keyStoreFile;
    string keyStorePass;
    string certPass;
}

annotation PathParam attach parameter {
    string value;
}

annotation QueryParam attach parameter {
    string value;
}

annotation Consumes attach resource {
    string[] value;
}

annotation Produces attach resource {
    string[] value;
}