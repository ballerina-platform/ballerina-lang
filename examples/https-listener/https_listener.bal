import ballerina/http;
import ballerina/log;

// An HTTP endpoint can be configured to communicate through HTTPS as well.
// To secure an endpoint using HTTPS, the endpoint needs to be configured with
// a keystore a certificate and private key for the endpoint.
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
        http:Response res = new;
        res.setPayload("Hello World!");
        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error in responding ", err = result);
        }
    }
}
