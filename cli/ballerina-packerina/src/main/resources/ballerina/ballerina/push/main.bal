package ballerina.push;

import ballerina.compression;
import ballerina.mime;
import ballerina.net.http;
import ballerina.io;

function main (string[] args) {

    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[0], getConnectorConfigs(args));
    }
    mime:Entity topLevelEntity = {};
    mime:MediaType mediaType = mime:getMediaType(mime:MULTIPART_FORM_DATA);
    topLevelEntity.contentType = mediaType;

    blob compressedContent = compression:zipToBytes(args[1]);
    mime:Entity filePart = {};
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    filePart.setBlob(compressedContent);
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

function getConnectorConfigs(string [] args) (http:Options) {
    string proxyHost = args[2];
    string proxyPort = args[3];
    string proxyUsername = args[4];
    string proxyPassword = args[5];
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