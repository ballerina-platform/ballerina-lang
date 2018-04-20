import ballerina/io;
import ballerina/http;
import ballerina/mime;

//Create a new service endpoint to accept new connections that are secured via mutual SSL.
endpoint http:Listener helloWorldEP {
    port:9095,
    secureSocket: {
        keyStore: {
            filePath: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        },
        protocol: {
            name: "TLS",
            versions: ["TLSv1.2","TLSv1.1"]
        },
        ciphers:["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"],
        //Enable mutual SSL.
        sslVerifyClient:"require"
    }
};

@http:ServiceConfig {
     endpoints:[helloWorldEP],
     basePath:"/hello"
}

//Bind the service to the endpoint that you declared above.
service<http:Service> helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }

    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        //Set the response payload.
        res.setStringPayload("Successful");
        //Send response to client.
        _ = conn -> respond(res);
    }
}

//Create a new client endpoint to connect to the service endpoint you created above via mutual SSL.
endpoint http:Client clientEP {
    url: "https://localhost:9095",
    secureSocket: {
        keyStore: {
            filePath: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        },
        protocol: {
            name: "TLS"
        },
        ciphers:["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    }
};
@Description {value:"The Ballerina client connector can be used to connect to the created HTTPS server. You have to run
the service before running this main function. As this is a mutual ssl connection, the client needs to provide the
keyStoreFile, keyStorePassword, trustStoreFile, and trustStorePassword."}
function main (string... args) {
    //Create a request.
    http:Request req = new;
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