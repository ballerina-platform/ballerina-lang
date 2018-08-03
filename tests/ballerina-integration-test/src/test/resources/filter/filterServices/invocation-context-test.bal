import ballerina/http;
import ballerina/log;
import ballerina/runtime;

@final string attributeName = "attribute";
@final string attributeValue = "value";

public type Filter15 object {
    public function filterRequest(http:Listener listener, http:Request request, http:FilterContext context)
                        returns boolean {
        log:printInfo("Add attribute to invocation context from filter");
        runtime:getInvocationContext().attributes[attributeName] = attributeValue;
        return true;
    }

    public function filterResponse(http:Response response, http:FilterContext context) returns boolean {
        return true;
    }
};

Filter15 filter15;

endpoint http:Listener echoEP08 {
    port:9098,
    filters:[filter15]
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo08 bind echoEP08 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        if (attributeValue == runtime:getInvocationContext().attributes[attributeName]) {
            _ = caller->respond(res);
        } else {
            res.statusCode = 500;
            _ = caller->respond(res);
        }
    }
}

