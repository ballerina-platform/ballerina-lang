import ballerina/http;
import ballerina/crypto;

listener http:MockListener mockEP = new(9090);

service hello on mockEP {
    @http:ResourceConfig {
        path:"/protocol",
        methods:["GET"]
    }
    resource function protocol (http:Caller caller, http:Request req) {
        http:Response res = new;
        json connectionJson = {protocol:caller.protocol};
        res.statusCode = 200;
        res.setJsonPayload(<json>crypto:unsafeMarkUntainted(connectionJson));
        _ = caller -> respond(res);
    }

    resource function local (http:Caller caller, http:Request req) {
        http:Response res = new;
        json connectionJson = {protocol:caller.protocol};
        res.statusCode = 200;
        res.setJsonPayload(<json>crypto:unsafeMarkUntainted(connectionJson));
        _ = caller -> respond(res);
    }
}