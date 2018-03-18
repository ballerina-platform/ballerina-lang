package ballerina.push;

import ballerina.io;
import ballerina.mime;
import ballerina.net.http;
import ballerina.compression;

function pushPackage (string accessToken, string url, string dirPath, string proxyHost, string proxyPort, string proxyUsername,
                      string proxyPassword) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(url, getConnectorConfigs(proxyHost, proxyPort, proxyUsername, proxyPassword));
    }
    mime:Entity filePart = {};
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    blob compressedContent = compression:zipToBytes(dirPath);
    filePart.setBlob(compressedContent);
    mime:Entity[] bodyParts = [filePart];

    http:OutRequest request = {};
    request.addHeader("Authorization", "Bearer " + accessToken);
    request.setMultiparts(bodyParts, mime:MULTIPART_FORM_DATA);
    var resp, errRes = httpEndpoint.post("", request);
    if (errRes != null) {
        error err = {message:errRes.message};
        throw err;
    }
    if (resp.statusCode != 200) {
        var jsonResponse, errJson = resp.getJsonPayload();
        if (errJson != null) {
            error err = {message: errJson.message};
            throw err;
        } else {
            io:println(jsonResponse.msg.toString());
        }
    } else {
        io:println("Ballerina package pushed successfully");
    }
}

function push (string[] args) {
    pushPackage(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
}

function getConnectorConfigs (string proxyHost, string proxyPort, string proxyUsername, string proxyPassword)
(http:Options) {
    http:Options option;
    int portInt = 0;
    if (proxyHost != "") {
        if (proxyPort != "") {
            var portI, _ = <int>proxyPort;
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