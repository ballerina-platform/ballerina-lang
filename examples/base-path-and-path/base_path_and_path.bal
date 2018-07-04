import ballerina/http;
import ballerina/log;
import ballerina/mime;

// The `BasePath` attribute associates a path to the service.
// It binds the service endpoint to the service.
@http:ServiceConfig { basePath: "/foo" }
service<http:Service> echo bind { port: 9090 } {
    // The `POST` annotation restricts the resource to accept `POST` requests only. Similarly, there are different annotations for each HTTP verb.
    // The `Path` attribute associates a subpath to the resource.
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/bar"
    }
    echo(endpoint caller, http:Request req) {
        // This method gets the request payload.
        var result = req.getJsonPayload();
        http:Response res = new;
        match result {
            error err => {
                res.statusCode = 500;
                res.setPayload(untaint err.message);
            }
            json value => {
                // Perform validation of JSON data before setting it to the response to prevent security vulnerabilities.
                if (value.hello != null && check value.hello.toString().matches("[a-zA-Z]+")) {
                    // Since JSON data is known to be valid, `untaint` the data denoting that the data is trusted and set the JSON to response.
                    res.setJsonPayload(untaint value);
                } else {
                    res.statusCode = 400;
                    res.setPayload("JSON containted invalid data");
                }
            }
        }
        // Reply to the client with the response.
        caller->respond(res) but {
            error e => log:printError("Error in responding", err = e)
        };
    }
}
