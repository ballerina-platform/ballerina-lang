import ballerina/http;
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
            http:Response response => {
                var result = caller->respond(response);
                if (result is error) {
                    log:printError("Failed to respond to caller", err = result);
                }
            }

            error e => log:printError("Faild to fulfill request", err = e);
        }
    }
}
