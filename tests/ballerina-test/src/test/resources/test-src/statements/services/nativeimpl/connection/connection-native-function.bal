import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningServiceEndpoint mockEP {
    port:9090
};

service<http:Service> hello bind mockEP {
    @http:ResourceConfig {
        path:"/redirect",
        methods:["GET"]
    }
    redirect (endpoint conn, http:Request req) {
        http:Response res = {};
        _ = conn -> redirect(res, http:RedirectCode.MOVED_PERMANENTLY_301, ["location1"]);
    }

    @http:ResourceConfig {
        path:"/protocol",
        methods:["GET"]
    }
    protocol (endpoint outboundEP, http:Request req) {
        http:Response res = {};
        json connectionJson = {protocol:outboundEP.conn.protocol};
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
        json connectionJson = {local:{host:outboundEP.conn.local.host, port:outboundEP.conn.local.port}};
        res.statusCode = 200;
        res.setJsonPayload(connectionJson);
        _ = outboundEP -> respond(res);
    }
}
