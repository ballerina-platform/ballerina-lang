import ballerina/http;
import ballerina/log;

// Create a new service endpoint to accept new connections
//that are secured via mutual SSL.
endpoint http:Listener helloWorldEP {
    port: 9095,
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        },
        protocol: {
            name: "TLS",
            versions: ["TLSv1.2", "TLSv1.1"]
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"],
        // Enable mutual SSL.
        sslVerifyClient: "require"
    }
};

@http:ServiceConfig {
    endpoints: [helloWorldEP],
    basePath: "/hello"
}

// Bind the service to the endpoint that you declared above.
service helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }

    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;
        // Set the response payload.
        res.setPayload("Successful");
        // Send response to client.
        caller->respond(res) but {
            error e => log:printError("Error in responding", err = e) };
    }
}

// Create a new client endpoint to connect to the service endpoint you created
//above via mutual SSL. The Ballerina client can be used to connect to the
//created HTTPS listener. Provide the `keyStoreFile`, `keyStorePassword`,
//`trustStoreFile` and `trustStorePassword` in the client.
endpoint http:Client clientEP {
    url: "https://localhost:9095",
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        },
        protocol: {
            name: "TLS"
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    }
};
function main(string... args) {
    // Create a request.
    var resp = clientEP->get("/hello");
    match resp {
        http:Response response => {
            match (response.getTextPayload()) {
                string res => log:printInfo(res);
                error err => log:printError(err.message);
            }
        }
        error err => log:printError(err.message);
    }
}
