import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> negativeTemplateURI {

    @http:resourceConfig {
        methods:["POST"],
        path:"/echo/{abc}/bar"
    }
    resource echo1 (http:Connection conn, http:Request req, string abc) {
        http:Response res = {};
        json responseJson = {"first":abc, "echo":"echo"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/echo/{xyz}/bar"
    }
    resource echo2 (http:Connection conn, http:Request req, string xyz) {
        http:Response res = {};
        json responseJson = {"first":xyz, "echo":"echo"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}