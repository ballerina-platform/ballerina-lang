import ballerina.file;
import ballerina.io;
import ballerina.mime;
import ballerina.net.http;

function pushPackage (string accessToken, string url, string dirPath) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(url, getConnectorConfigs());
    }
    mime:Entity filePart = {};
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    file:File fileHandler = {path:dirPath};
    filePart.setFileAsEntityBody(fileHandler);

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
    }
}

function main (string[] args) {
    pushPackage(args[0], args[1], args[2]);
}

function getConnectorConfigs() (http:Options) {
    http:Options option = {
                              ssl: {
                                       trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
                                       trustStorePassword:"ballerina"
                                   },
                              followRedirects: {}
                          };
    return option;
}