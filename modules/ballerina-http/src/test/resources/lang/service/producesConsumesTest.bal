import ballerina.net.http;
import ballerina.net.http.response;

@http:configuration {basePath:"/echo66"}
service<http> echo66 {
    @http:resourceConfig {
        methods:["POST"],
        path:"/test1",
        consumes:["application/xml"]
    }
    resource echo1 (http:Request req, http:Response res) {
        json responseJson = {"msg":"wso2"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test2",
        produces:["text/xml","application/xml "]
    }
    resource echo2 (http:Request req, http:Response res) {
        json responseJson = {"msg":"wso22"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/test3",
        consumes:["application/xhtml+xml","text/plain","text/json"],
        produces:["text/css","application/json"]
    }
    resource echo3 (http:Request req, http:Response res) {
        json responseJson = {"msg":"wso222"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}

@http:configuration {basePath:"/echo67"}
service<http> echo67 {
    resource echo1 (http:Request req, http:Response res) {
        json responseJson = {"echo33": "echo1"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}