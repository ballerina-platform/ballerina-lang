import ballerina/http;
import ballerina/log;

// helloWorldEP listener endpoint is configured to communicate through https.
// It is configured to listen on port 9095. As this is an https Listener,
// it is required to give the PKCS12 keystore file location and it's password.
endpoint http:Listener helloWorldEP {
    port: 9095,
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorld bind helloWorldEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello World!");
        var result = caller->respond(res);
        if (result is error) {
           log:printError("Failed to respond", err = result);
        }
    }
}

// This is a client endpoint configured to connect to the above https service.
// As this is a 1-way SSL connection, the client needs to provide
// trust store file path and it's password.
endpoint http:Client clientEP {
    url: "https://localhost:9095",
    secureSocket: {
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};
// You have to run the service before running this main function.
public function main(string... args) {
    // Sends an outbound request.
    var resp = clientEP->get("/hello/");

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
