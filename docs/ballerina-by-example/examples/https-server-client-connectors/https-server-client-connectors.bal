import ballerina.io;
import ballerina.net.http;

endpoint<http:Service> helloWorldEP {
    port:9095,
    ssl : {
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        certPassword:"ballerina"
    }
}

@http:serviceConfig {
    endpoints:[helloWorldEP],
    basePath:"/hello"
}
service<http:Service> helloWorld {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }

    resource sayHello (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Successful");
        _ = conn -> respond(res);
    }
}

endpoint<http:Client> clientEP {
    serviceUri: "https://localhost:9095",
    ssl: {
        trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
        trustStorePassword:"ballerina"
    }
}

@Description {value:"Ballerina client connector can be used to connect to the created https server. You have to run the service before running this main function. As this is a 1-way ssl connection, client needs to provide trustStoreFile and trustStorePassword."}
function main (string[] args) {
    //Creates an outbound request.
    http:Request req = {};
    http:Response resp = {};
    resp, _ = clientEP -> get("/hello/", req);
    io:println("Response code: " + resp.statusCode);
    var jsonPayload, payloadError = resp.getStringPayload();
    if (payloadError == null) {
        io:println("Response: " + jsonPayload);
    } else {
        io:println("Response: " + payloadError.message);
    }

}

