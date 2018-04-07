import ballerina/http;

endpoint http:NonListeningService echoEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/signature"
}
service<http:Service> echo bind echoEP {
    echo1 (http:ServerConnector conn, int key, http:Request req) {
        http:Response resp = new;
    }
}
