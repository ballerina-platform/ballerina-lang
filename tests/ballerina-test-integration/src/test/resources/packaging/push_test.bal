import ballerina/http;
import ballerina/io;

function main (string ... args) {
    endpoint http:Client httpEndpoint {
        url: "https://api.staging-central.ballerina.io/packages/IntegrationTest/my.app/1.0.0",
        secureSocket:{
            trustStore:{
                path:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password:"ballerina"
            },
            verifyHostname:false,
            shareSession:true
        }
    };
    http:Request req = new;
    var result = httpEndpoint -> get("", request=req);
    http:Response httpResponse = new;
    match result {
        http:Response response => httpResponse = response;
        error e => {
            io:println("Connection to the remote host failed");
        }
    }
    json jsonResponse = null;
    string statusCode = <string> httpResponse.statusCode;
    if (statusCode == "302") {
        io:println("Package exists");
    } else {
       io:println("Package doesnot exist");
    }
}
