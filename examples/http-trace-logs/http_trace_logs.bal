import ballerina/http;
import ballerina/log;

@http:ServiceConfig { basePath: "/hello" }
service helloWorld on new http:Listener(9090) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        http:Client clientEP = new("http://httpstat.us");
        var resp = clientEP->forward("/200", req);
        if (resp is http:Response) {
            var result = caller->respond(resp);
            if (result is error) {
                log:printError("Failed to respond to caller", err = result);
            }
        } else if (resp is error) {
            log:printError("Faild to fulfill request", err = resp);
        }
    }
}
