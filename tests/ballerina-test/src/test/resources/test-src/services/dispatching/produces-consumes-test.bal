import ballerina/net.http;
import ballerina/net.http.mock;

endpoint<mock:NonListeningService> testEP {
    port:9090
}

@http:ServiceConfig {
    endpoints:[testEP]
}
service<http:Service> echo66 {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/test1",
        consumes:["application/xml"]
    }
    resource echo1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"msg":"wso2"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test2",
        produces:["text/xml","application/xml "]
    }
    resource echo2 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"msg":"wso22"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/test3",
        consumes:["application/xhtml+xml","text/plain","text/json"],
        produces:["text/css","application/json"]
    }
    resource echo3 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"msg":"wso222"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {
    endpoints:[testEP]
}
service<http:Service> echo67 {
    resource echo1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo33": "echo1"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}