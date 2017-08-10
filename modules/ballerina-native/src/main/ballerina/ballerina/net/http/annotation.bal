package ballerina.net.http;

import ballerina.net.ws;

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

annotation CORS attach resource {
    string[] allowOrigin;
    string allowCredentials;
    string[] allowMethods;
    string[] allowHeaders;
    string maxAge;
    string[] exposeHeaders;
}

annotation Path attach resource {
    string value;
}

annotation configuration attach service<>, service<ws> {
    string host;
    int port;
    int httpsPort;
    string basePath;
    string keyStoreFile;
    string keyStorePass;
    string certPass;
    string[] allowOrigin;
    string allowCredentials;
    string[] allowMethods;
    string[] allowHeaders;
    string maxAge;
    string[] exposeHeaders;
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