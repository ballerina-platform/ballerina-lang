import ballerina.net.http;

@doc:description{value : "BasePath annotation can be used to associate a path to service"}
@http:BasePath {value:"/foo"}
service echo {
    @doc:description{value : "Post annotation constrains the resource only to accept post requests. Similarly, for each HTTP verb there are different annotations."}
    @http:POST{}
    @doc:description{value : "Path annotation can be used to associate a sub-path to resource"}
    @http:Path {value:"/bar"}
    resource echo (message m) {
	    // A util method that can convert the request to a response.
        http:convertToResponse(m);
        // Send back the response to the client.
        reply m;
    }
}
