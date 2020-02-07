import ballerina/http;

listener http:MockListener testEP = new(9090);

@http:ServiceConfig {
    basePath:"/hello"
}
service negativeTemplateURI on testEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/echo/{abc}/bar"
    }
    resource function echo1(http:Caller caller, http:Request req, string abc) {
        http:Response res = new;
        json responseJson = {"first":abc, "echo":"echo"};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/echo/{xyz}/bar"
    }
    resource function echo2(http:Caller caller, http:Request req, string xyz) {
        http:Response res = new;
        json responseJson = {"first":xyz, "echo":"echo"};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }
}
