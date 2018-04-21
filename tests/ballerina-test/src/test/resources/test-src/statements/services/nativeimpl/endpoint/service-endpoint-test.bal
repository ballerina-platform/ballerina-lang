import ballerina/http;
import ballerina/http;

endpoint http:NonListener mockEP {
    port:9090
};

service<http:Service> hello bind mockEP {

    @http:ResourceConfig {
        path:"/protocol",
        methods:["GET"]
    }
    protocol (endpoint caller, http:Request req) {
        http:Response res = {};
        json connectionJson = {protocol:caller.protocol};
        res.statusCode = 200;
        res.setJsonPayload(connectionJson);
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        path:"/local",
        methods:["GET"]
    }
    local (endpoint caller, http:Request req) {
        http:Response res = {};
        json connectionJson = {local:{host:caller.local.host, port:caller.local.port}};
        res.statusCode = 200;
        res.setJsonPayload(connectionJson);
        _ = caller -> respond(res);
    }
}
