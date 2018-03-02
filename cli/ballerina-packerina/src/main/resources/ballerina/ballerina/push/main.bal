package ballerina.push;

import ballerina.mime;
import ballerina.net.http;
import ballerina.io;
import ballerina.file;

function pushPackage (string url, string baloFilePath, string proxyHost, string proxyPort, string proxyUsername,
                      string proxyPassword) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(url, getConnectorConfigs(proxyHost, proxyPort, proxyUsername, proxyPassword));
    }
    mime:Entity topLevelEntity = {};
    mime:MediaType mediaType = mime:getMediaType(mime:MULTIPART_FORM_DATA);
    topLevelEntity.contentType = mediaType;

    mime:Entity filePart = {};
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    file:File fileHandler = {path:baloFilePath};
    filePart.setFileAsEntityBody(fileHandler);
    mime:Entity[] bodyParts = [filePart];

    topLevelEntity.multipartData = bodyParts;
    http:OutRequest request = {};
    request.setEntity(topLevelEntity);
    var resp1, errRes = httpEndpoint.post("", request);
    if (errRes != null) {
        error err = {message: errRes.message};
        throw err;
    }
    if (resp1.statusCode != 200) {
        json jsonResponse = resp1.getJsonPayload();
        io:println(jsonResponse.msg.toString());
    } else {
        io:println("Ballerina package pushed successfully");
    }
}

function main (string[] args) {
    pushPackage(args[0], args[1], args[2], args[3], args[4], args[5]);
}

function getConnectorConfigs(string proxyHost, string proxyPort, string proxyUsername, string proxyPassword)
(http:Options) {
    http:Options option;
    int portInt = 0;
    if (proxyHost != ""){
        if (proxyPort != ""){
            var portI, _ = <int> proxyPort;
            portInt = portI;
        }
        option = {
                     proxy:{
                               host:proxyHost,
                               port:portInt,
                               userName:proxyUsername,
                               password:proxyPassword
                           }
                 };
    } else {
        option = {};
    }
    return option;
}