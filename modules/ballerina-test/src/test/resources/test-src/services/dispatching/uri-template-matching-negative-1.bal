import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> negativeTemplateURI {

    @http:resourceConfig {
        methods:["POST"],
        path:"/echo/{abc}/bar"
    }
    resource echo1 (http:Request req, http:Response res, string abc) {
        json responseJson = {"first":abc, "echo":"echo"};
        res.setJsonPayload(responseJson);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/echo/{xyz}/bar"
    }
    resource echo2 (http:Request req, http:Response res, string xyz) {
        json responseJson = {"first":xyz, "echo":"echo"};
        res.setJsonPayload(responseJson);
        _ = res.send();
    }
}