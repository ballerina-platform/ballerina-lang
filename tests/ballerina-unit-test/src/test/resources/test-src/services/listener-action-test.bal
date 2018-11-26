import ballerina/http;

listener http:MockListener echoEP  = new(9090);

string serviceLevelStringVar = "sample value";

@http:ServiceConfig {basePath:"/listener"}
service echo on echoEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message"
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload(serviceLevelStringVar);
        _ = caller->respond(res);
        serviceLevelStringVar = "done";
    }
}
