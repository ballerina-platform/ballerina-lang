import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningService echoEP {
    port:9090
};

@http:serviceConfig {
    basePath:"/signature",
    endpoints:[echoEP]
}
service<http:Service> echo {

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    echo1 (http:ServerConnector conn, http:Request req, string key, json ballerina) {
        http:Response res = {};
        _ = conn -> respond(res);
    }
}
