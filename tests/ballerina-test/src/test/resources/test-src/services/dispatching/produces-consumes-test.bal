import ballerina/http;

endpoint http:NonListeningServiceEndpoint testEP {
    port:9090
};

service<http:Service> echo66 bind testEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/test1",
        consumes:["application/xml"]
    }
    echo1 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"msg":"wso2"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test2",
        produces:["text/xml","application/xml "]
    }
    echo2 (endpoint conn, http:Request req) {
        http:Response res = new;
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
    echo3 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"msg":"wso222"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

service<http:Service> echo67 bind testEP {
    echo1 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo33": "echo1"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}