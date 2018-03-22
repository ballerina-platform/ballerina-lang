import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningServiceEndpoint testEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> negativeTemplateURI bind testEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/echo/{abc}/bar"
    }
     echo1 (endpoint client, http:Request req, string abc) {
        http:Response res = {};
        json responseJson = {"first":abc, "echo":"echo"};
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/echo/{xyz}/bar"
    }
     echo2 (endpoint client, http:Request req, string xyz) {
        http:Response res = {};
        json responseJson = {"first":xyz, "echo":"echo"};
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }
}