package ballerina.push;

import ballerina.file;
import ballerina.io;
import ballerina.mime;
import ballerina.net.http;

function pushPackage (string accessToken, string url, string baloFilePath, string proxyHost, string proxyPort, string proxyUsername,
                      string proxyPassword) (boolean) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(url, getConnectorConfigs(proxyHost, proxyPort, proxyUsername, proxyPassword));
    }
    mime:Entity filePart = {};
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    file:File fileHandler = {path:baloFilePath};
    filePart.setFileAsEntityBody(fileHandler);
    mime:Entity[] bodyParts = [filePart];

    http:OutRequest request = {};
    request.addHeader("Authorization", "Bearer " + accessToken);
    request.setMultiparts(bodyParts, mime:MULTIPART_FORM_DATA);
    var resp, errRes = httpEndpoint.post("", request);
    if (errRes != null) {
        error err = {message:errRes.message};
        throw err;
        return false;
    }
    if (resp.statusCode != 200) {
        var jsonResponse, errJson = resp.getJsonPayload();
        if (errJson != null) {
            error err = {message: errJson.message};
            throw err;
        } else {
            io:println(jsonResponse.msg.toString());
        }
        return false;
    } else {
        io:println("Ballerina package pushed successfully");
        return true;
    }
}

function push (string[] args) (boolean) {
    return pushPackage(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
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