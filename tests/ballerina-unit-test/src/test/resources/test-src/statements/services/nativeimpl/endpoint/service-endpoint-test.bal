import ballerina/http;

listener http:MockServer mockEP = new(9090);

service hello on mockEP {

    @http:ResourceConfig {
        path:"/protocol",
        methods:["GET"]
    }
    resource function protocol(http:Caller caller, http:Request req) {
        http:Response res = new;
        //TODO:Added temporarily. To be fixed
        //json connectionJson = {protocol:caller.protocol};
        json connectionJson = {protocol:"test"};
        res.statusCode = 200;
        res.setJsonPayload(untaint connectionJson);
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        path:"/local",
        methods:["GET"]
    }
    resource function local(http:Caller caller, http:Request req) {
        http:Response res = new;
        //TODO:Added temporarily. To be fixed
        //json connectionJson = {local:{host:caller.local.host, port:caller.local.port}};
        json connectionJson = {protocol:"test"};
        res.statusCode = 200;
        res.setJsonPayload(untaint connectionJson);
        _ = caller -> respond(res);
    }
}
