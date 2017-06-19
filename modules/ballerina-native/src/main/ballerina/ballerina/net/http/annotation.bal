package ballerina.net.http;

annotation GET attach resource {
}

annotation POST attach resource {
}

annotation Path attach resource {
    string value;
}

annotation Host attach service {
    string value;
}

annotation Port attach service {
    string value;
}

annotation Schema attach service {
    string value;
}

annotation Keystore attach service {
    string keyStoreFile;
    string keyStorePass;
    string certPass;
}

annotation BasePath attach service {
    string value;
}

annotation PathParam attach parameter {
    string value;
}

annotation QueryParam attach parameter {
    string value;
}