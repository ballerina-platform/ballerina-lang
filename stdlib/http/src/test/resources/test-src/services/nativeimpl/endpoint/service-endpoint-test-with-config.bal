import ballerina/http;

listener http:MockListener mockEP = new(9090, config= {server:"Apache"});

service hello on mockEP {

    @http:ResourceConfig {
        path:"/echo",
        methods:["GET"]
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        json connectionJson = {protocol:caller.protocol};
        res.statusCode = 200;
        res.setJsonPayload(untaint connectionJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/echo1",
        methods:["GET"]
    }
    resource function echo1(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setHeader("server", "Mysql");
        json connectionJson = {protocol:caller.protocol};
        res.setJsonPayload(untaint connectionJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/echo8",
        methods:["GET"]
    }
    resource function echo8(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setHeader("content-type", "jhdkaf");
        json connectionJson = {protocol:caller.protocol};
        res.setJsonPayload(untaint connectionJson);
        checkpanic caller->respond(res);
    }

}
