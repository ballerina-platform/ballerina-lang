import ballerina/http;
import ballerina/http.mock;

endpoint mock:NonListeningService echoEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/signature",
    endpoints:[echoEP]
}
service<http:Service> echo {
    echo1 (http:ServerConnector conn, int key, http:Request req) {
        http:Response resp = {};
    }
}
