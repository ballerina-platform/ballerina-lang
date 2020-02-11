import ballerina/http;
import ballerina/log;

// Creates an HTTP client to interact with a remote endpoint.
http:Client clientEndpoint = new ("http://localhost:9092", {
    followRedirects: {enabled: true, maxCount: 5}
});

service hello on new http:Listener(9090) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function myResource(http:Caller caller, http:Request req) {
        // Sends a `GET` request to the specified endpoint.
        var returnResult = clientEndpoint->get("/redirect1");
        if (returnResult is http:Response) {
            // Retrieves the text payload from the response.
            var payload = returnResult.getTextPayload();
            if (payload is string) {
                var result = caller->respond("Response received : " 
                                                + <@untained>payload);
                if (result is error) {
                    log:printError("Error in responding", result);
                }
            } else {
                var result = caller->respond("Error in payload : " 
                        + <@untained>payload.detail()?.message.toString());
                if (result is error) {
                    log:printError("Error in responding", result);
                }
            }
        } else {
            var result = caller->respond("Error in connection : " 
                                + returnResult.detail()?.message.toString());
            if (result is error) {
                log:printError("Error in responding", result);
            }
        }
    }
}

@http:ServiceConfig {
    basePath: "/redirect1"
}
service redirect1 on new http:Listener(9092) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function redirect1(http:Caller caller, http:Request req) {
        http:Response res = new;
        // Sends a redirect response with a location.
        error? result = caller->redirect(res,
            http:REDIRECT_TEMPORARY_REDIRECT_307,
            ["http://localhost:9093/redirect2"]);
        if (result is error) {
            log:printError("Error in sending redirect response to caller",
                err = result);
        }
    }
}

@http:ServiceConfig {
    basePath: "/redirect2"
}
service redirect2 on new http:Listener(9093) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function redirect2(http:Caller caller, http:Request req) {
        // Sends a response to the caller.
        var result = caller->respond("Hello World!");
        if (result is error) {
            log:printError("Error in responding", result);
        }
    }
}
