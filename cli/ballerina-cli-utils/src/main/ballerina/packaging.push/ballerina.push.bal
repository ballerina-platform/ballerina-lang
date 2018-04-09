package packaging.push;

import ballerina/file;
import ballerina/io;
import ballerina/mime;
import ballerina/http;

function pushPackage (string accessToken, string url, string dirPath, string msg) {
    endpoint http:Client httpEndpoint {
        targets: [
        {
            url: url,
            secureSocket: {
                trustStore: {
                    filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                    password: "ballerina"
                },
                hostNameVerification:false,
                sessionCreation: true
            }
        }
        ]
    };
    mime:Entity filePart = {};
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    file:Path filePath = file:getPath(dirPath);
    filePart.setFileAsEntityBody(filePath);
    mime:Entity[] bodyParts = [filePart];

    http:Request req = {};
    http:Response res = {};
    req.addHeader("Authorization", "Bearer " + accessToken);
    req.setMultiparts(bodyParts, mime:MULTIPART_FORM_DATA);
    
    var httpResponse = httpEndpoint -> post("", req);
    match httpResponse {
     http:HttpConnectorError errRes => {
         var errorResp = <error> errRes;
         match errorResp {
             error err =>  throw err;
         }
     }
     http:Response response => res = response;
    }
    if (res.statusCode != 200) {
        var jsonResponse = res.getJsonPayload();
        match jsonResponse {
            mime:EntityError errRes => {
                var errorResp = <error> errRes;
                match errorResp {
                    error err =>  throw err;
                }
            }  
            json jsonObj => io:println(jsonObj.msg.toString());            
        }
    } else {
        io:println(msg);
    }
}

function main (string[] args) {
    pushPackage(args[0], args[1], args[2], args[3]);
}
