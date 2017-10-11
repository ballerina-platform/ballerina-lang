import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> echo11 {

    @http:resourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    resource echo1 (http:Request req, http:Response res) {
        json responseJson = {"echo5":"echo5"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}-{xyz}"
    }
    resource echo2 (http:Request req, http:Response res, string abc, string xyz) {
        json responseJson = {"first":abc, "second":xyz};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}+{xyz}"
    }
    resource echo3 (http:Request req, http:Response res, string abc, string xyz) {
        json responseJson = {"first":xyz, "second":abc};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}"
    }
    resource echo4 (http:Request req, http:Response res, string abc) {
        json responseJson = {"echo3":abc};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}+{xyz}/bar"
    }
    resource echo5 (http:Request req, http:Response res, string abc, string xyz) {
        json responseJson = {"first":abc, "second":xyz, "echo4":"echo4"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}+{xyz}/{bar}"
    }
    resource echo6 (http:Request req, http:Response res, string abc, string xyz, string bar) {
        json responseJson = {"first":abc, "second":xyz, "echo4":bar};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    resource echo7 (http:Request req, http:Response res) {
        json responseJson = {"echo5":"any"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/echo2/{abc}+{xyz}/bar"
    }
    resource echo8 (http:Request req, http:Response res, string abc, string xyz) {
        json responseJson = {"first":abc, "second":xyz, "echo8":"echo8"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo3/{abc}+{xyz}"
    }
    resource echo9 (http:Request req, http:Response res, string abc, string xyz) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"first":abc, "second":xyz, "third":foo, "echo9":"echo9"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource echo10 (http:Request req, http:Response res) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"third":foo, "echo10":"echo10"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    resource echo11 (http:Request req, http:Response res) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"third":foo, "echo11":"echo11"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo12/{abc}/bar"
    }
    resource echo12 (http:Request req, http:Response res, string abc) {
        json responseJson = {"echo12":abc};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo13"
    }
    resource echo13 (http:Request req, http:Response res) {
        map params = req.getQueryParams();
        string bar;
        bar, _ = (string)params.foo;
        json responseJson = {"echo13":bar};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo14"
    }
    resource echo14 (http:Request req, http:Response res) {
        map params = req.getQueryParams();
        string bar;
        bar, _ = (string)params.foo;
        json responseJson = {"echo14":bar};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo15"
    }
    resource echo15 (http:Request req, http:Response res) {
        map params = req.getQueryParams();
        string barStr;
        boolean bar;
        barStr, _ = (string) params.foo;
        bar, _ = <boolean> barStr;
        json responseJson = {"echo15":bar};
        res.setJsonPayload(responseJson);
        res.send();
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
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    resource echo2 (http:Request req, http:Response res) {
        json responseJson = {"echo2":"echo2"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/foo/bar"
    }
    resource echo3 (http:Request req, http:Response res) {
        json responseJson = {"echo3":"echo3"};
        res.setJsonPayload(responseJson);
        res.send();
    }
}

@http:configuration {basePath:"/"}
service<http> echo33 {
    resource echo1 (http:Request req, http:Response res) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"third":foo, "echo33":"echo1"};
        res.setJsonPayload(responseJson);
        res.send();
    }
}

service<http> echo44 {


    @http:resourceConfig {
        path:"echo2"
    }
    resource echo221 (http:Request req, http:Response res) {
        json responseJson = {"first":"zzz"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    resource echo1 (http:Request req, http:Response res) {
        map params = req.getQueryParams();
        var foo, _ = (string)params.foo;
        json responseJson = {"first":foo, "echo44":"echo1"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    resource echo222 (http:Request req, http:Response res) {
        json responseJson = {"first":"bar"};
        res.setJsonPayload(responseJson);
        res.send();
    }
}

@http:configuration {basePath:"/echo55"}
service<http> echo55 {
    @http:resourceConfig {
        path:"/foo/bar"
    }
    resource echo1 (http:Request req, http:Response res) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"echo55":"echo55"};
        res.setJsonPayload(responseJson);
        res.send();
    }

    @http:resourceConfig {
        path:"/*"
    }
    resource echo2 (http:Request req, http:Response res) {
        json responseJson = {"echo55":"default"};
        res.setJsonPayload(responseJson);
        res.send();
    }
}