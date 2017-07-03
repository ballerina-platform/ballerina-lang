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

annotation config attach service {
    string host;
    int port;
    string basePath;
    string scheme;
    string keyStoreFile;
    string keyStorePass;
    string certPass;
    string version;
}

annotation PathParam attach parameter {
    string value;
}

annotation QueryParam attach parameter {
    string value;
}