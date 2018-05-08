
annotation GET attach resource {
}

annotation POST attach resource {
}

annotation Path attach resource {
    string value;
}

annotation config attach service {
    string host;
    int port;
    string basePath;
    string schema;
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