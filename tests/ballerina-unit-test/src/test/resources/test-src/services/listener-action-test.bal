import ballerina/http;

listener http:MockServer echoEP  = new(9090);

@http:ServiceConfig {basePath:"/listener"}
service echo on echoEP {

    string serviceLevelStringVar = "sample value";

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
