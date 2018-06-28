import ballerina/io;
import ballerina/http;
import ballerina/swagger;
import ballerina/log;

// Defines this endpoint as a selected ednpoint for client generation
@swagger:ClientEndpoint
endpoint http:Listener helloEp {
    port: 9090
};

// Enable the client code generation for this service
@swagger:ClientConfig {
    generate: true
}
@http:ServiceConfig {
    basePath: "/sample"
}
service Hello bind helloEp {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/hello"
    }
    hello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello");
        caller->respond(res) but {
            error e => log:printError("Error when responding", err = e)
        };
    }
}
