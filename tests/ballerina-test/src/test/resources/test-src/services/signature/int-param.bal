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
    echo1 (http:ServerConnector conn, int key, http:Request req) {
        http:Response resp = {};
    }
}
