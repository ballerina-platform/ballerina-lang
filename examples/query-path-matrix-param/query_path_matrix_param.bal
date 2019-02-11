import ballerina/http;
import ballerina/log;

service sample on new http:Listener(9090) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/path/{foo}"
    }
    // The `PathParam` and `QueryParam` parameters extract values from the request URI.
    resource function params(http:Caller caller, http:Request req,
                                string foo) {
        // Get `QueryParams`.
        var params = req.getQueryParams();
        var bar = <string>params.bar;

        // Get `MatrixParams`. 
        map<any> pathMParams = req.getMatrixParams("/sample/path");
        var a = <string>pathMParams.a;
        var b = <string>pathMParams.b;
        string pathMatrixStr = string `a={{a}}, b={{b}}`;
        map<any> fooMParams = req.getMatrixParams("/sample/path/" + foo);
        var x = <string>fooMParams.x;
        var y = <string>fooMParams.y;
        string fooMatrixStr = string `x={{x}}, y={{y}}`;
        json matrixJson = { "path": pathMatrixStr, "foo": fooMatrixStr };

        // Create a JSON payload with the extracted values.
        json responseJson = { "pathParam": foo, "queryParam": bar,
                                "matrix": matrixJson };
        http:Response res = new;
        // A util method to set the JSON payload to the response message.
        res.setJsonPayload(untaint responseJson);
        // Send a response to the client.
        var result = caller->respond(res);

        if (result is error) {
            log:printError("Error when responding", err = result);
        }
    }
}
