import ballerina/http;
import ballerina/io;
import ballerina/log;

endpoint http:Client clientEP {
    url: "http://httpstat.us"
};

@http:ServiceConfig { basePath: "/hello" }
service<http:Service> helloWorld bind { port: 9090 } {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        var resp = clientEP->forward("/200", req);
        match resp {
            http:Response response =>
                    caller->respond(response)
                            but { error e =>
                                    log:printError(
                                        "Failed to respond to caller", err = e) };

            error e => log:printError("Faild to fulfill request", err = e);
        }
    }
}
