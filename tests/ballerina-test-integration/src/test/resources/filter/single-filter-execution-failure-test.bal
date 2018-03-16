import ballerina.net.http;
import ballerina.log;

public struct Filter1 {
    function (http:Request request, http:FilterContext context) returns (http:FilterResult) filterRequest;
    function (http:Response response, http:FilterContext context) returns (http:FilterResult) filterResponse;
}

public function <Filter1 filter> init () {
    log:printInfo("Initializing filter 1");
}

public function <Filter1 filter> terminate () {
    log:printInfo("Stopping filter 1");
}

public function interceptRequest1 (http:Request request, http:FilterContext context) (http:FilterResult) {
    log:printInfo("Intercepting request for filter 1");
    http:FilterResult filterResponse = {canProceed:false, statusCode:401, message:"Authentication failure"};
    return filterResponse;
}

public function interceptResponse1 (http:Response response, http:FilterContext context) (http:FilterResult) {
    log:printInfo("Intercepting response for filter 1");
    http:FilterResult filterResponse = {canProceed:true, statusCode:200, message:"successful"};
    return filterResponse;
}

Filter1 filter1 = {filterRequest:interceptRequest1, filterResponse:interceptResponse1};

endpoint http:ServiceEndpoint echoEP {
    port:9090,
    filters:[filter1]
};

@http:serviceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP {
    @http:resourceConfig {
        methods:["GET"],
        path:"/test"
    }
    echo (endpoint client, http:Request req) {
    http:Response res = {};
    _ = client -> respond(res);
    }
}
