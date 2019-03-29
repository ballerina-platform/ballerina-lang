import ballerina/http;
import ballerina/log;
import ballerina/openapi;

@openapi:ClientEndpoint
listener http:Listener helloEp = new(9090);

@openapi:ClientConfig {
    generate: true
}
@http:ServiceConfig {
    basePath: "/sample"
}
service Hello on helloEp {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/hello"
    }
    resource function hello(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello");
        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error when responding", err = result);
        }
    }
}

