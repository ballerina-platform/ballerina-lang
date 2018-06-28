import ballerina/http;
import ballerina/log;

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
        caller->respond(res) but {
            error e => log:printError("Failed to respond", err = e) };
    }
}

endpoint http:Client clientEP {
    url: "https://localhost:9095",
    secureSocket: {
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};
// The Ballerina client can be used to connect to the created HTTPS listener.
// You have to run the service before running this main function. As this is a
// 1-way SSL connection, the client needs to provide values for
// `trustStoreFile` and `trustStorePassword`.
function main(string... args) {
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
