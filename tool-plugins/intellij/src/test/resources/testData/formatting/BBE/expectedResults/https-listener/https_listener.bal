import ballerina/http;
import ballerina/log;

// An HTTP endpoint can be configured to communicate through HTTPS as well.
// To secure an endpoint using HTTPS, the endpoint needs to be configured with
// a keystore a certificate and private key for the endpoint.
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
            error e => log:printError("Error in responding ", err = e) };
    }
}
