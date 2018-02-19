package ballerina.push;

import ballerina.compression;
import ballerina.mime;
import ballerina.net.http;

function main (string[] args) {

    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[0], getConnectorConfigs(args[2], args[3], args[4], args[5]));
    }
    mime:Entity topLevelEntity = {};
    mime:MediaType mediaType = mime:getMediaType(mime:MULTIPART_FORM_DATA);
    topLevelEntity.contentType = mediaType;

    blob compressedContent = compression:zipToBytes(args[1]);
    mime:Entity filePart = {};
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    filePart.byteData = compressedContent;
    mime:Entity[] bodyParts = [filePart];

    topLevelEntity.multipartData = bodyParts;
    http:OutRequest request = {};
    request.setEntity(topLevelEntity);
    http:InResponse resp1 = {};
    resp1, _ = httpEndpoint.post("", request);
    if (resp1.statusCode != 200) {
        println("Internal server error occured when pushing the package to the central repository");
    } else {
        println("Ballerina package pushed successfully");
    }
}

function getConnectorConfigs(string host, string port, string username, string password) (http:Options) {
    http:Options option;
    int portInt = 0;
    if (host != ""){
        if (port != ""){
            var portI, _ = <int> port;
            portInt = portI;
        }
        option = {
                     proxy:{
                               host:host,
                               port:portInt,
                               userName:username,
                               password:password
                           }
                 };
    } else {
        option = {};
    }
    return option;
}