import ballerina/http;

import ballerina/log;

// Filter1
FilterDto dto = { authenticated: true, username: "abcde" };

public type Filter1 object {
    public function filterRequest(http:Request request, http:FilterContext context) returns http:FilterResult {
        log:printInfo("Intercepting request for filter 1");
        http:FilterResult filterResponse = { canProceed: true, statusCode: 200, message: "successful" };
        context.attributes["attribute1"] = "attribute1";
        context.attributes["attribute2"] = dto;
        return filterResponse;
    }
};

Filter1 filter1;

// Filter2

public type Filter2 object {
    public function filterRequest(http:Request request, http:FilterContext context) returns http:FilterResult {
        log:printInfo("Intercepting request for filter 2");
        boolean status = true;
        if (context.attributes.hasKey("attribute1")){
            if (context.attributes["attribute1"] == "attribute1"){
                status = status && true;
            } else {
                status = status && false;
            }
        } else {
            status = status && false;
        }
        if (context.attributes.hasKey("attribute2")){
            FilterDto returnedDto = check <FilterDto>context.attributes["attribute2"];
            if (returnedDto.authenticated == dto.authenticated && returnedDto.username == dto.username){
                status = status && true;
            } else {
                status = status && false;
            }
        } else {
            status = status && false;
        }
        http:FilterResult filterResponseSuccess = { canProceed: true, statusCode: 200, message: "successful" };
        http:FilterResult filterResponseFailure = { canProceed: false, statusCode: 401, message:
        "attribute missing in context" };
        if (status){
            return filterResponseSuccess;
        } else {
            return filterResponseFailure;
        }
    }
};

Filter2 filter2;

endpoint http:Listener echoEP {
    port: 9090,
    filters: [filter1, filter2]
};

@http:ServiceConfig {
    basePath: "/echo"
}
service<http:Service> echo bind echoEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test"
    }
    echo(endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->respond(res);
    }
}

public type FilterDto {
    boolean authenticated;
    string username;
};