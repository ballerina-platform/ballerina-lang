import ballerina/http;
import ballerina/log;
import ballerina/swagger;

@swagger:ClientEndpoint
listener http:Listener helloEp = new(9090);
@swagger:ClientConfig {
    generate: true
}
@http:ServiceConfig {
    basePath: "/sample"
}
service Hello on new http:Listener(9090) {    
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/hello"
    }
    resource function hello(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello");
        var result = caller -> respond(res);
        if (result is error) {
            log:printError("Error when responding", err = result);
        }
   }
}

