package packaging.push;

import ballerina/file;
import ballerina/io;
import ballerina/mime;
import ballerina/http;

function pushPackage (string accessToken, string url, string dirPath, string msg) {
    endpoint http:ClientEndpoint httpEndpoint {
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
    mime:Entity filePart = new;
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    file:Path filePath = file:getPath(dirPath);
    filePart.setFileAsEntityBody(filePath);
    mime:Entity[] bodyParts = [filePart];

    http:Request req = new;
    // http:Response res = new;
    req.addHeader("Authorization", "Bearer " + accessToken);
    req.setMultiparts(bodyParts, mime:MULTIPART_FORM_DATA);
    
    // http:Response httpResponse = check (httpEndpoint -> post("", req));
    var result = httpEndpoint -> post("", req);
    http:Response httpResponse = check result;

    // match httpResponse {
    //  http:HttpConnectorError errRes => {
    //      var errorResp = <error> errRes;
    //      match errorResp {
    //          error err =>  throw err;
    //      }
    //  }
    //  http:Response response => res = response;
    // }
    if (httpResponse.statusCode != 200) {
        json jsonResponse = check (httpResponse.getJsonPayload());
        string message = (jsonResponse.msg.toString() but {()=> "error occurred when pushing the package"});
        io:println(message);
        // match jsonResponse {
        //     mime:EntityError errRes => {
        //         var errorResp = <error> errRes;
        //         match errorResp {
        //             error err =>  throw err;
        //         }
        //     }  
        //     json jsonObj => io:println(jsonObj.msg.toString());            
        // }
    } else {
        io:println(msg);
    }
}

function main (string[] args) {
    // pushPackage(args[0], args[1], args[2], args[3]);
    pushPackage("", "https://api.staging-central.ballerina.io/packages/natasha/my.app/1.0.0", 
    "/home/natasha/Documents/workspace/mysimpleproj/.ballerina/repo/$anon/my.app/0.0.0/my.app.zip","Successful");

}
