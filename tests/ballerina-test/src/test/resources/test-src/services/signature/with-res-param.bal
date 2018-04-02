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
    echo1 (http:ServerConnector conn, http:Response res) {
        http:Response resp = {};
    }
}
