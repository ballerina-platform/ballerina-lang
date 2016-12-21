package ballerina.net.http;

native function getParameter(message m, string key)(string);

native function getContentType(message m)(string);

native function getStatusCode(message m)(string);

