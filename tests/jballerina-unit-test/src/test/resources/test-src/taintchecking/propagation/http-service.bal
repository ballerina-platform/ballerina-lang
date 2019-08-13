import ballerina/http;
import ballerina/log;

listener http:Listener helloWorldEP = new(9090);

service sample on helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    resource function params (http:Caller caller, http:Request req, string foo) {
        var bar = req.getQueryParamValue("bar");

        secureFunction(foo, foo);
        secureFunction(bar, bar);
    }

    resource function hi(http:Caller caller, http:Request request) {
        http:Response response = new;
        var req = request.getJsonPayload();
        if (req is json) {
            response.setJsonPayload(req);
        } else {
             log:printError("Invalid JSON!");
        }
        var result = caller->respond(response);
        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
}

public function secureFunction (@untainted any secureIn, any insecureIn) {

}

