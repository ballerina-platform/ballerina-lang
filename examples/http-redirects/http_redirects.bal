import ballerina/http;
import ballerina/log;

// Create an HTTP client to interact with a remote endpoint.
http:Client clientEndpoint = new("http://localhost:9090", config = {
        followRedirects: { enabled: true, maxCount: 5 }
    });

public function main() {
    // Send a `GET` request to the specified endpoint.
    var returnResult = clientEndpoint->get("/redirect1");
    if (returnResult is http:Response) {
        // Retrieve the text payload from the response.
        var payload = returnResult.getTextPayload();
        if (payload is string) {
            log:printInfo("Response received : " + payload);
        } else {
            log:printError("Error in payload", err = payload);
        }
    } else {
        log:printError("Error in connection", err = returnResult);
    }
}

@http:ServiceConfig {
    basePath:"/redirect1"
}
service redirect1 on new http:Listener(9090) {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function redirect1(http:Caller caller, http:Request req) {
        http:Response res = new;
        // Send a redirect response with a location.
        _ = caller->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307,
                                    ["http://localhost:9093/redirect2"]);

    }
}

@http:ServiceConfig {
    basePath:"/redirect2"
}
service redirect2 on new http:Listener(9093) {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function redirect2(http:Caller caller, http:Request req) {
         // Send a response to the caller.
        var result = caller->respond("Hello World!");
        if (result is error) {
           log:printError("Error in responding", err = result);
        }
    }
}
