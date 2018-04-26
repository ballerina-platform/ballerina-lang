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
                res.setPayload(err.message);
            }
            json value => {
                res.setJsonPayload(value);
            }
        }
        // Reply to the client with the response.
        caller->respond(res) but {
            error e => log:printError("Error in responding", err = e)
        };
    }
}
