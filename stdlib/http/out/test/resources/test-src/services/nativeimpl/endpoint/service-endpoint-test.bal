import ballerina/http;

listener http:MockListener mockEP = new(9090);

service hello on mockEP {

    @http:ResourceConfig {
        path:"/protocol",
        methods:["GET"]
    }
    resource function protocol(http:Caller caller, http:Request req) {
        http:Response res = new;
        json connectionJson = {protocol:caller.protocol};
        res.statusCode = 200;
        res.setJsonPayload(<@untainted json> connectionJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/local",
        methods:["GET"]
    }
    resource function local(http:Caller caller, http:Request req) {
        http:Response res = new;
        json connectionJson = {local:{host:caller.localAddress.host, port:caller.localAddress.port}};
        res.statusCode = 200;
        res.setJsonPayload(<@untainted json> connectionJson);
        checkpanic caller->respond(res);
    }
}
