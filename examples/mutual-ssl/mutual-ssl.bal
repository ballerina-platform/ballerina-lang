import ballerina.io;
import ballerina.net.http;

endpoint<http:Service> helloWorldEP {
    port:9095,
    ssl : {
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        certPassword:"ballerina",
        sslVerifyClient:"require",
        trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
        trustStorePassword:"ballerina",
        ciphers:"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
        sslEnabledProtocols:"TLSv1.2,TLSv1.1"
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
        //Set response payload.
        res.setStringPayload("Successful");
        //Send response to client.
        _ = conn -> respond(res);
    }
}

endpoint<http:Client> clientEP {
    serviceUri: "https://localhost:9095",
    ssl: {
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
        trustStorePassword:"ballerina",
        ciphers:"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
        sslEnabledProtocols:"TLSv1.2,TLSv1.1"
    }
}

@Description {value:"Ballerina client connector can be used to connect to the created https server. You have to run the service before running this main function. As this is a mutual ssl connection, client also needs to provide keyStoreFile, keyStorePassword, trustStoreFile and trustStorePassword."}
function main (string[] args) {
    //Creates a request.
    http:Request req = {};
    http:Response resp = {};
    resp, _ = clientEP -> get("/hello/", req);
    io:println("Response code: " + resp.statusCode);
    var payload, _ = resp.getStringPayload();
    io:println("Response: " + payload);
}
