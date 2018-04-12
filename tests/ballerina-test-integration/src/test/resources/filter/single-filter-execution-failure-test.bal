import ballerina/http;
import ballerina/log;

public type Filter1 object {
    public function requestFilter (http:Request request, http:FilterContext context) returns http:FilterResult {
        log:printInfo("Intercepting request for filter 1");
        http:FilterResult filterResponse = {canProceed:false, statusCode:401, message:"Authentication failure"};
        return filterResponse;
    }

    public function responseFilter(http:Response response, http:FilterContext context) returns http:FilterResult {
        log:printInfo("Intercepting response for filter 1");
        http:FilterResult filterResponse = {canProceed:true, statusCode:200, message:"successful"};
        return filterResponse;
    }

    public function init () {
        log:printInfo("Initializing filter 1");
    }

    public function terminate () {
        log:printInfo("Stopping filter 1");
    }
};

Filter1 filter1;

endpoint http:Listener echoEP {
    port:9090,
    filters:[filter1]
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    echo (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> respond(res);
    }
}
