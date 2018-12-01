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
        var result = caller->respond("Successful");
        if (result is error) {
            log:printError("Error occurred while responding", err = result);
        }
    }
}
