import ballerina/http;
import ballerina/log;

// Create a new service endpoint to accept new connections
//that are secured via mutual SSL.
http:ServiceEndpointConfiguration helloWorldEPConfig = {
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

listener http:Listener helloWorldEP = new(9095, config = helloWorldEPConfig);

@http:ServiceConfig {
    basePath: "/hello"
}

// Bind the service to the endpoint that you declared above.
service helloWorld on helloWorldEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }

    resource function sayHello(http:Caller caller, http:Request req) {
        http:Response res = new;
        // Set the response payload.
        res.setPayload("Successful");
        // Send response to client.
        var result = caller->respond(res);

        if (result is error) {
            log:printError("Error in responding", err = result);
        }
    }
}

// Create a new client endpoint to connect to the service endpoint you created
//above via mutual SSL. The Ballerina client can be used to connect to the
//created HTTPS listener. Provide the `keyStoreFile`, `keyStorePassword`,
//`trustStoreFile` and `trustStorePassword` in the client.
http:ClientEndpointConfig clientEPConfig = {
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
public function main() {
    http:Client clientEP = new("https://localhost:9095",
                                config = clientEPConfig);
    // Create a request.
    var resp = clientEP->get("/hello");
    if (resp is http:Response) {
        var payload = resp.getTextPayload();
        if (payload is string) {
            log:printInfo(payload);
        } else {
            log:printError(<string> payload.detail().message);
        }
    } else {
        log:printError(<string> resp.detail().message);
    }
}
