import ballerina/io;
import ballerina/http;
import ballerina/mime;

endpoint http:ServiceEndpoint helloWorldEP {
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
        sslVerifyClient:"require"
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
        http:Response res = new;
        //Set response payload.
        res.setStringPayload("Successful");
        //Send response to client.
        _ = conn -> respond(res);
    }
}

endpoint http:Client clientEP {
    targets: [{
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
    }]
};
@Description {value:"Ballerina client connector can be used to connect to the created https server. You have to run the service before running this main function. As this is a mutual ssl connection, client also needs to provide keyStoreFile, keyStorePassword, trustStoreFile and trustStorePassword."}
function main (string[] args) {
    //Creates a request.
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