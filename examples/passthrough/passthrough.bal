import ballerina/http;
import ballerina/log;

http:Client clientEP = new("http://localhost:9092/hello");

service passthrough on new http:Listener(9090) {

    //The passthrough resource allows all HTTP methods since the resource configuration does not explicitly specify
    //which HTTP methods are allowed.
    @http:ResourceConfig {
        path: "/"
    }
    resource function passthrough(http:Caller caller, http:Request req) {
        // When `forward()` is called on the backend client endpoint, it forwards the request that the passthrough
        // resource received to the backend. When forwarding, the request is made using the same HTTP method that was
        // used to invoke the passthrough resource. The `forward()` function returns the response from the backend if
        // there are no errors.
        var clientResponse = clientEP->forward("/", req);
        // Since the `forward()` can return an error as well, a `match` is required to handle the respective scenarios.
        if (clientResponse is http:Response) {
            // If the request was successful, an HTTP response is returned.
            // Here, the received response is forwarded to the client through the outbound endpoint.
            var result = caller->respond(clientResponse);
            if (result is error) {
               log:printError("Error sending response", err = result);
            }
        } else {
            // If there was an error, the 500 error response is constructed and sent back to the client.
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(<string> clientResponse.detail().message);
            var result = caller->respond(res);
            if (result is error) {
               log:printError("Error sending response", err = result);
            }
        }
    }
}

//Sample hello world service.
service hello on new http:Listener(9092) {
    //The helloResource only accepts requests made using the specified HTTP methods.
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function helloResource(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello World!");
        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
}
