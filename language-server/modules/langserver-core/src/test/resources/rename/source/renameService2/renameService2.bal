import ballerina/http;

listener http:MockListener mockEP = new(9090);

service hello on mockEP {
    resource function protocol(http:Caller caller, http:Request request) {
        http:Response res = new;
        json connectionJson = {protocol:caller.protocol};
        res.statusCode = 200;
        res.setJsonPayload(crypto:unsafeMarkUntainted(connectionJson));
        _ = caller -> respond(res);
    }
}