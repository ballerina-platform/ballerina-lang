import ballerina/http;

endpoint http:Listener passthroughEP {
    port:9090
};

endpoint http:Client clientEP {
    url:"http://localhost:9092/hello"
};

service<http:Service> passthrough bind passthroughEP {

    @Description {value:"The passthrough resource allows all HTTP methods since the resource configuration does not explicitly specify which HTTP methods are allowed."}
    @http:ResourceConfig {
        path:"/"
    }
    passthrough (endpoint caller, http:Request req) {
        // When `forward()` is called on the backend client endpoint, it forwards the request that the passthrough
        // resource received to the backend. When forwarding, the request is made using the same HTTP method that was
        // used to invoke the passthrough resource. The `forward()` function returns the response from the backend if
        // there are no errors.
        var clientResponse = clientEP -> forward("/", req);

        // Since the `forward()` can return an error as well, a `match` is required to handle the respective scenarios.
        match clientResponse {
            http:Response res => {
                // If the request was successful, an HTTP response is returned.
                // Here, the received response is forwarded to the client through the outbound endpoint.
                _ = caller -> respond(res);
            }
            http:HttpConnectorError err => {
                // If there was an error, the 500 error response is constructed and sent back to the client.
                http:Response res = new;
                res.statusCode = 500;
                res.setStringPayload(err.message);
                _ = caller -> respond(res);
            }
        }
    }
}

endpoint http:Listener helloEP {
    port:9092
};

@Description {value:"Sample hello world service."}
service<http:Service> hello bind helloEP {

    @Description {value:"The helloResource only accepts requests made using the specified HTTP methods."}
    @http:ResourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    helloResource (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Hello World!");
        _ = caller -> respond(res);
    }
}
