import ballerina/io;
import ballerina/http;
import ballerina/mime;

endpoint http:ServiceEndpoint helloWorldEP {
    port:9095,
    secureSocket: {
        keyStore: {
            filePath: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};


@http:ServiceConfig {
    endpoints:[helloWorldEP],
    basePath:"/hello"
}

service<http:Service> helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }

    sayHello (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Successful");
        _ = conn -> respond(res);
    }
}

endpoint http:ClientEndpoint clientEP {
    targets: [{
        url: "https://localhost:9095",
        secureSocket: {
            trustStore: {
                filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            }
        }
    }]
};
@Description {value:"Ballerina client connector can be used to connect to the created https server. You have to run the service before running this main function. As this is a 1-way ssl connection, client needs to provide trustStoreFile and trustStorePassword."}
function main (string[] args) {
    //Creates an outbound request.
    http:Request req = {};
    var resp = clientEP -> get("/hello/", req);
    match resp {
        http:HttpConnectorError err => io:println(err.message);
        http:Response response => {
            match (response.getStringPayload()) {
                http:PayloadError payloadError => io:println(payloadError.message);
                string res => io:println(res);
            }
        }
    }
}
