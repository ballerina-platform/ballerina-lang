import ballerina/file;
import ballerina/io;
import ballerina/mime;
import ballerina/net.http;

function pushPackage (string accessToken, string url, string dirPath) {
    endpoint http:ClientEndpoint httpEndpoint {
        targets: [
        {
            uri: url,
            secureSocket: {
                trustStore: {
                    filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                    password: "ballerina"
                },
                hostNameVerification:false
            }
        }
        ]
    };
    mime:Entity filePart = {};
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    file:File fileHandler = {path:dirPath};
    filePart.setFileAsEntityBody(fileHandler);

    mime:Entity[] bodyParts = [filePart];

    http:Request req = {};
    http:Response res = {};
    req.addHeader("Authorization", "Bearer " + accessToken);
    req.setMultiparts(bodyParts, mime:MULTIPART_FORM_DATA);
    var httpResponse = httpEndpoint -> post("", req);
    match httpResponse {
     error errRes => throw errRes;
     http:Response response => res = response;
    }
    if (res.statusCode != 200) {
        var jsonResponse = res.getJsonPayload();
        match jsonResponse {
            error errJson => throw errJson;
            json jsonObj => io:println(jsonObj.msg.toString());            
        }
    }
}

function main (string[] args) {
    pushPackage(args[0], args[1], args[2]);
}