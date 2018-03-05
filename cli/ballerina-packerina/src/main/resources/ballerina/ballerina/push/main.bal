package ballerina.push;

import ballerina.file;
import ballerina.io;
import ballerina.mime;
import ballerina.net.http;

function pushPackage (string accessToken, string url, string baloFilePath, string proxyHost, string proxyPort, string proxyUsername,
                      string proxyPassword) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(url, getConnectorConfigs(proxyHost, proxyPort, proxyUsername, proxyPassword));
    }
    // Validate the access token generated
    var clientResponse, errResp = validateToken(accessToken);
    if (errResp != null) {
        error validationErr = {message:errResp.message};
        throw validationErr;
    } else {
        json jsonResp = clientResponse.getJsonPayload();
        string accessTokenIsActive = jsonResp.active.toString();
        if (accessTokenIsActive.equalsIgnoreCase("true")) {
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
                error err = {message:errRes.message};
                throw err;
            }
            if (resp1.statusCode != 200) {
                json jsonResponse = resp1.getJsonPayload();
                io:println(jsonResponse.msg.toString());
            } else {
                io:println("Ballerina package pushed successfully");
            }
        } else {
            if (accessToken == null) {
                io:println("You have not specified an access-token for the central in your Settings.toml. Please login to central if you are already registered using 'central.ballerina.io/login' to get a valid access-token. If you are new to the site please register using 'central.ballerina.io/register'");
            } else {
                io:println("The access-token provided in Settings.toml has expired. Please login again from the central using 'central.ballerina.io/login' to get a valid access-token.");
            }
        }
    }
}

function main (string[] args) {
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

function validateToken (string accessToken) (http:InResponse, http:HttpConnectorError) {
    endpoint<http:HttpClient> validateEndpoint {
        create http:HttpClient("https://localhost:9443", {});
    }
    string strPayload = "token=" + accessToken;
    http:OutRequest outReq = {};
    outReq.setStringPayload(strPayload);
    outReq.setHeader("Content-Type", "application/x-www-form-urlencoded");
    outReq.addHeader("Authorization", "Basic YWRtaW46YWRtaW4=");
    var clientResponse, err = validateEndpoint.post("/oauth2/introspect", outReq);
    return clientResponse, err;
}