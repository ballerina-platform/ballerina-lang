import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningService echoEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/signature",
    endpoints:[echoEP]
}
service<http:Service> echo {
    echo1 (http:ServerConnector conn, http:Request req, boolean key) {
        http:Response res = {};
    }
}
