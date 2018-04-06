import ballerina/http;

endpoint http:NonListeningService echoEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/signature"
}
service<http:Service> echo bind echoEP{

    @http:ResourceConfig {
        methods:["POST"],
        body:"person"
    }
    echo1 (http:ServerConnector conn, http:Request req, string key, json ballerina) {
        http:Response res = new;
        _ = conn -> respond(res);
    }
}
