import ballerina/http;
import ballerina/log;

@http:ServiceConfig {
    basePath: "/hello"
}
service helloService on new http:Listener(9095) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function hello(http:Caller caller, http:Request request) {
        // Respond with the message "Successful" for each request.
        var result = caller->respond("Successful");
        // Log the `error` in case of a failure.
        if (result is error) {
            log:printError("Error occurred while responding", result);
        }
    }
}
