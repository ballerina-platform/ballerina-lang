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
    echo1 (http:ServerConnector conn, http:Response res) {
        http:Response resp = {};
    }
}
