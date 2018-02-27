package ballerina.push;

import ballerina.compression;
import ballerina.mime;
import ballerina.net.http;
import ballerina.io;

function main (string[] args) {

    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[1], getConnectorConfigs(args[3], args[4], args[5], args[6]));
    }
    // Validate the access token generated
    var clientResponse, errResp = validateToken(args[0]);
    if (errResp != null) {
        error validationErr = {message: errResp.message};
        throw validationErr;
    } else {
        json jsonResp = clientResponse.getJsonPayload();
        string accessTokenIsActive = jsonResp.active.toString();
        if (accessTokenIsActive.equalsIgnoreCase("true")) {
            mime:Entity topLevelEntity = {};
            mime:MediaType mediaType = mime:getMediaType(mime:MULTIPART_FORM_DATA);
            topLevelEntity.contentType = mediaType;

            blob compressedContent = compression:zipToBytes(args[2]);
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
            io:println(resp1);
            if (resp1.statusCode != 200) {
                io:println("Internal server error occured when pushing the package to the central repository");
            } else {
                io:println("Ballerina package pushed successfully");
            }
        } else {
            if (args[0] == null) {
                io:println("You have not specified an access-token for the central in your Settings.toml. Please login to central if you are already registered using 'central.ballerina.io/login' to get a valid access-token. If you are new to the site please register using 'central.ballerina.io/register'");
            } else {
                io:println("The access-token provided in Settings.toml has expired. Please login again from the central using 'central.ballerina.io/login' to get a valid access-token.");
            }
        }
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

function validateToken(string accessToken) (http:InResponse , http:HttpConnectorError ) {
    endpoint<http:HttpClient> validateEndpoint {
        create http:HttpClient("https://localhost:9443" , {});
    }
    string strPayload = "token=" +accessToken;
    http:OutRequest outReq = {};
    outReq.setStringPayload(strPayload);
    outReq.setHeader("Content-Type", "application/x-www-form-urlencoded");
    outReq.addHeader("Authorization", "Basic YWRtaW46YWRtaW4=");
    var clientResponse, err = validateEndpoint.post("/oauth2/introspect", outReq);
    return clientResponse, err;
}