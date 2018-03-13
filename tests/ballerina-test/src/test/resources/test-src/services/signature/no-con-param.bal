import ballerina.net.http;
import ballerina.net.http.mock;

endpoint<mock:NonListeningService> echoEP {
    port:9090
}

@http:serviceConfig {
    basePath:"/signature",
    endpoints:[echoEP]
}
service<http:Service> echo {
    resource echo1 (http:Request req, http:Response res) {
        http:Response resp = {};
    }
}
