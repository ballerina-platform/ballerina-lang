import ballerina/http;
import ballerina/log;

@http:ServiceConfig
service<http:Service> sample bind { port: 9090 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/path/{foo}"
    }
    // The `PathParam` and `QueryParam` parameters extract values from the request URI.
    params(endpoint caller, http:Request req, string foo) {
        // Get `QueryParams`.
        var params = req.getQueryParams();
        var bar = <string>params.bar;

        // Get `MatrixParams`. 
        map pathMParams = req.getMatrixParams("/sample/path");
        var a = <string>pathMParams.a;
        var b = <string>pathMParams.b;
        string pathMatrixStr = string `a={{a}}, b={{b}}`;
        map fooMParams = req.getMatrixParams("/sample/path/" + foo);
        var x = <string>fooMParams.x;
        var y = <string>fooMParams.y;
        string fooMatrixStr = string `x={{x}}, y={{y}}`;
        json matrixJson = { "path": pathMatrixStr, "foo": fooMatrixStr };

        // Create a JSON payload with the extracted values.
        json responseJson = { "pathParam": foo, "queryParam": bar, "matrix": matrixJson };
        http:Response res = new;
        // A util method to set the JSON payload to the response message.
        res.setJsonPayload(untaint responseJson);
        // Send a response to the client.
        caller->respond(res) but { error e => log:printError("Error when responding", err = e) };
    }
}
