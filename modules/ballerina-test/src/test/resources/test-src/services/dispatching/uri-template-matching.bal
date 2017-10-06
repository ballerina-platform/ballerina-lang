import ballerina.net.http;
import ballerina.net.http.request;
import ballerina.net.http.response;

@http:configuration {basePath:"/hello"}
service<http> echo11 {

    @http:resourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    resource echo1 (http:Request req, http:Response res) {
        json responseJson = {"echo5":"echo5"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}-{xyz}"
    }
    resource echo2 (http:Request req, http:Response res, string abc, string xyz) {
        json responseJson = {"first":abc, "second":xyz};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}+{xyz}"
    }
    resource echo3 (http:Request req, http:Response res, string abc, string xyz) {
        json responseJson = {"first":xyz, "second":abc};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}"
    }
    resource echo4 (http:Request req, http:Response res, string abc) {
        json responseJson = {"echo3":abc};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}+{xyz}/bar"
    }
    resource echo5 (http:Request req, http:Response res, string abc, string xyz) {
        json responseJson = {"first":abc, "second":xyz, "echo4":"echo4"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}+{xyz}/{bar}"
    }
    resource echo6 (http:Request req, http:Response res, string abc, string xyz, string bar) {
        json responseJson = {"first":abc, "second":xyz, "echo4":bar};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    resource echo7 (http:Request req, http:Response res) {
        json responseJson = {"echo5":"any"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/echo2/{abc}+{xyz}/bar"
    }
    resource echo8 (http:Request req, http:Response res, string abc, string xyz) {
        json responseJson = {"first":abc, "second":xyz, "echo8":"echo8"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo3/{abc}+{xyz}"
    }
    resource echo9 (http:Request req, http:Response res, string abc, string xyz) {
        map params = request:getQueryParams(req);
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"first":abc, "second":xyz, "third":foo, "echo9":"echo9"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource echo10 (http:Request req, http:Response res) {
        map params = request:getQueryParams(req);
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"third":foo, "echo10":"echo10"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    resource echo11 (http:Request req, http:Response res) {
        map params = request:getQueryParams(req);
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"third":foo, "echo11":"echo11"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo12/{abc}/bar"
    }
    resource echo12 (http:Request req, http:Response res, string abc) {
        json responseJson = {"echo12":abc};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo13"
    }
    resource echo13 (http:Request req, http:Response res) {
        map params = request:getQueryParams(req);
        string bar;
        bar, _ = (string)params.foo;
        json responseJson = {"echo13":bar};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo14"
    }
    resource echo14 (http:Request req, http:Response res) {
        map params = request:getQueryParams(req);
        string bar;
        bar, _ = (string)params.foo;
        json responseJson = {"echo14":bar};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo15"
    }
    resource echo15 (http:Request req, http:Response res) {
        map params = request:getQueryParams(req);
        boolean bar;
        bar, _ = (boolean) params.foo;
        json responseJson = {"echo15":bar};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/so2"
    }
    resource echo (http:Request req, http:Response res) {
        //http:convertToResponse(req);
        //reply m;

    }
}

@http:configuration {basePath:"/hello/world"}
service<http> echo22 {

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2"
    }
    resource echo1 (http:Request req, http:Response res) {
        json responseJson = {"echo1":"echo1"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    resource echo2 (http:Request req, http:Response res) {
        json responseJson = {"echo2":"echo2"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/foo/bar"
    }
    resource echo3 (http:Request req, http:Response res) {
        json responseJson = {"echo3":"echo3"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}

@http:configuration {basePath:"/"}
service<http> echo33 {
    resource echo1 (http:Request req, http:Response res) {
        map params = request:getQueryParams(req);
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"third":foo, "echo33":"echo1"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}

service<http> echo44 {


    @http:resourceConfig {
        path:"echo2"
    }
    resource echo221 (http:Request req, http:Response res) {
        json responseJson = {"first":"zzz"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    resource echo1 (http:Request req, http:Response res) {
        map params = request:getQueryParams(req);
        var foo, _ = (string)params.foo;
        json responseJson = {"first":foo, "echo44":"echo1"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    resource echo222 (http:Request req, http:Response res) {
        json responseJson = {"first":"bar"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}

@http:configuration {basePath:"/echo55"}
service<http> echo55 {
    @http:resourceConfig {
        path:"/foo/bar"
    }
    resource echo1 (http:Request req, http:Response res) {
        map params = request:getQueryParams(req);
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"echo55":"echo55"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        path:"/*"
    }
    resource echo2 (http:Request req, http:Response res) {
        json responseJson = {"echo55":"default"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}