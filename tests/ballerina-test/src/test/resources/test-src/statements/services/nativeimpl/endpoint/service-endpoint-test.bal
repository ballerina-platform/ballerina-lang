import ballerina/http;
import ballerina/http;

endpoint http:NonListeningServiceEndpoint mockEP {
    port:9090
};

service<http:Service> hello bind mockEP {

    @http:ResourceConfig {
        path:"/protocol",
        methods:["GET"]
    }
    protocol (endpoint outboundEP, http:Request req) {
        http:Response res = {};
        json connectionJson = {protocol:outboundEP.protocol};
        res.statusCode = 200;
        res.setJsonPayload(connectionJson);
        _ = outboundEP -> respond(res);
    }

    @http:ResourceConfig {
        path:"/local",
        methods:["GET"]
    }
    local (endpoint outboundEP, http:Request req) {
        http:Response res = {};
        json connectionJson = {local:{host:outboundEP.local.host, port:outboundEP.local.port}};
        res.statusCode = 200;
        res.setJsonPayload(connectionJson);
        _ = outboundEP -> respond(res);
    }
}
