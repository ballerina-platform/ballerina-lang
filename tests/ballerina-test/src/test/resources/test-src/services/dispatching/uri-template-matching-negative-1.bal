import ballerina/net.http;
import ballerina/net.http.mock;

endpoint<mock:NonListeningService> testEP {
    port:9090
}

@http:serviceConfig {
    basePath:"/hello",
    endpoints:[testEP]
}
service<http:Service> negativeTemplateURI {

    @http:resourceConfig {
        methods:["POST"],
        path:"/echo/{abc}/bar"
    }
    resource echo1 (http:ServerConnector conn, http:Request req, string abc) {
        http:Response res = {};
        json responseJson = {"first":abc, "echo":"echo"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/echo/{xyz}/bar"
    }
    resource echo2 (http:ServerConnector conn, http:Request req, string xyz) {
        http:Response res = {};
        json responseJson = {"first":xyz, "echo":"echo"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}