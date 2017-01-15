package ballerina.net.http;

native function getParameter(message m, string key)(string);

native function getContentType(message m)(string);

native function setContentType(message m, string contentType);

native function getStatusCode(message m)(string);

native function setStatusCode(message m, int statusCode);

native function getStatusString(message m)(string);

native function setStatusString(message m, string statusString);

native function convertToResponse(message m, int statusCode);

native function convertToResponse(message m, int statusCode, string protocolVersion);

native function convertToRequest(message m);

native function convertToRequest(message m, string protocolVersion);

native function acceptAndReturn(int statusCode);



