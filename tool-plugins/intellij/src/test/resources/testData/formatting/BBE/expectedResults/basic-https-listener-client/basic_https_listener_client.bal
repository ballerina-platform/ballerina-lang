import ballerina/http;
import ballerina/log;

// `helloWorldEP` listener endpoint is configured to communicate through HTTPS.
// It is configured to listen on port 9095. As this is an HTTPS Listener,
// it is required to give the PKCS12 keystore file location and its password.
http:ServiceEndpointConfiguration helloWorldEPConfig = {
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

listener http:Listener helloWorldEP = new(9095, config = helloWorldEPConfig);

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorld on helloWorldEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        // Send the response back to the `caller`.
        var result = caller->respond("Hello World!");
        if (result is error) {
            log:printError("Failed to respond", err = result);
        }
    }
}

// This is a client endpoint configured to connect to the above HTTPS service.
// As this is a 1-way SSL connection, the client needs to provide
// trust store file path and its password.
http:ClientEndpointConfig clientEPConfig = {
    secureSocket: {
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};

public function main() {
    // Create an HTTP client to interact with the created listener endpoint.
    http:Client clientEP = new("https://localhost:9095",
        config = clientEPConfig);
    // Sends an outbound request.
    var resp = clientEP->get("/hello/");
    if (resp is http:Response) {
        // If the request is successful, retrieve the text payload from the
        // response.
        var payload = resp.getTextPayload();
        if (payload is string) {
            // Log the retrieved text paylod.
            log:printInfo(payload);
        } else {
            // If an error occurs when retrieving the text payload, log the error.
            log:printError(<string>payload.detail().message);
        }
    } else {
        // If an error occurs when getting the response, log the error.
        log:printError(<string>resp.detail().message);
    }
}
