import ballerina/http;
import ballerina/log;

// Filter1

public type Filter1 object {
    public function filterRequest (http:Request request, http:FilterContext context) returns http:FilterResult {
        log:printInfo("Intercepting request for filter 1");
        http:FilterResult filterResponse = {canProceed:true, statusCode:200, message:"successful"};
        return filterResponse;
    }
};

Filter1 filter1;

// Filter2

public type Filter2 object {
    public function filterRequest (http:Request request, http:FilterContext context) returns http:FilterResult {
        log:printInfo("Intercepting request for filter 2");
        http:FilterResult filterResponse = {canProceed:false, statusCode:405, message:"Not Allowed"};
        return filterResponse;
    }
};

Filter2 filter2;

// Filter3

public type Filter3 object {
    public function filterRequest (http:Request request, http:FilterContext context) returns http:FilterResult {
        log:printInfo("Intercepting request for filter 3");
        http:FilterResult filterResponse = {canProceed:true, statusCode:200, message:"successful"};
        return filterResponse;
    }
};

Filter3 filter3;

endpoint http:Listener echoEP {
    port:9090,
    filters:[filter1,filter2,filter3]
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
