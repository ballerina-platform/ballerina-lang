import ballerina/http;
import ballerina/doc;

@doc:Description {value:"BasePath attribute associates a path to the service."}
@http:configuration {basePath:"/foo"}
service<http> echo {
    @doc:Description {value:"Post annotation constrains the resource only to accept post requests. Similarly, for each HTTP verb there are different annotations."}
    @doc:Description {value:"Path annotation associates a sub-path to resource."}
    @http:resourceConfig {
        methods:["POST"],
        path:"/bar"
    }
    resource echo (message m) {
        // A util method that can convert the request to a response.
        http:convertToResponse(m);
        // Send back the response to the client.
        response:send(m);
    }
}
