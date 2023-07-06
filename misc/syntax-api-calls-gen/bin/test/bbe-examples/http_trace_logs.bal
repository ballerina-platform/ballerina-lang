import ballerina/http;
import ballerina/log;

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorld on new http:Listener(9090) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        // Create a new [http:Client](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/clients/Client.html).
        http:Client clientEP = new ("http://httpstat.us");
        // Forward incoming requests to the remote backend.
        var resp = clientEP->forward("/200", req);
        if (resp is http:Response) {
            // Respond to the caller.
            var result = caller->respond(<@untainted>resp);
            // Log the error in case of a failure.
            if (result is error) {
                log:printError("Failed to respond to caller", result);
            }
        } else {
            log:printError("Failed to fulfill request", <error>resp);
        }
    }
}
