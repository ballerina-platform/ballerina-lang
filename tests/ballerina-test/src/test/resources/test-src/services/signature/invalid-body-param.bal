import ballerina/http;
import ballerina/http;

endpoint http:NonListeningService echoEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/signature",
    endpoints:[echoEP]
}
service<http:Service> echo {

    @http:ResourceConfig {
        methods:["POST"],
        body:"person"
    }
    echo1 (http:ServerConnector conn, http:Request req, string key, int person) {
        http:Response res = {};
        _ = conn -> respond(res);
    }
}
