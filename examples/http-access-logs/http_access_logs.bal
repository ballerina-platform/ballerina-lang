import ballerina/http;
import ballerina/log;

@http:ServiceConfig {
    basePath: "/hello"
}
service<http:Service> helloService bind { port: 9095 } {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    hello(endpoint caller, http:Request request) {
        http:Response response = new;
        response.setPayload("Successful");
        caller->respond(response) but {
            error e => log:printError("Error when responding", err = e)
        };
    }
}
