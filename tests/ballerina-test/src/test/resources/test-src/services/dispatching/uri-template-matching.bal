import ballerina.net.http;
import ballerina.net.http.mock;

endpoint<mock:NonListeningService> testEP {
    port:9090
}

@http:serviceConfig {
    basePath:"/hello",
    endpoints:[testEP]
}
service<http:Service> echo11 {

    @http:resourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    resource echo1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo5":"echo5"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}-{xyz}"
    }
    resource echo2 (http:ServerConnector conn, http:Request req, string abc, string xyz) {
        http:Response res = {};
        json responseJson = {"first":abc, "second":xyz};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}+{xyz}"
    }
    resource echo3 (http:ServerConnector conn, http:Request req, string abc, string xyz) {
        http:Response res = {};
        json responseJson = {"first":xyz, "second":abc};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}"
    }
    resource echo4 (http:ServerConnector conn, http:Request req, string abc) {
        http:Response res = {};
        json responseJson = {"echo3":abc};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}+{xyz}/bar"
    }
    resource echo5 (http:ServerConnector conn, http:Request req, string abc, string xyz) {
        http:Response res = {};
        json responseJson = {"first":abc, "second":xyz, "echo4":"echo4"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}+{xyz}/{bar}"
    }
    resource echo6 (http:ServerConnector conn, http:Request req, string abc, string xyz, string bar) {
        http:Response res = {};
        json responseJson = {"first":abc, "second":xyz, "echo4":bar};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    resource echo7 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo5":"any"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/echo2/{abc}+{xyz}/bar"
    }
    resource echo8 (http:ServerConnector conn, http:Request req, string abc, string xyz) {
        http:Response res = {};
        json responseJson = {"first":abc, "second":xyz, "echo8":"echo8"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo3/{abc}+{xyz}"
    }
    resource echo9 (http:ServerConnector conn, http:Request req, string abc, string xyz) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"first":abc, "second":xyz, "third":foo, "echo9":"echo9"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource echo10 (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"third":foo, "echo10":"echo10"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    resource echo11 (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"third":foo, "echo11":"echo11"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo12/{abc}/bar"
    }
    resource echo12 (http:ServerConnector conn, http:Request req, string abc) {
        http:Response res = {};
        json responseJson = {"echo12":abc};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo125"
    }
    resource echo125 (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        string bar;
        bar, _ = (string)params.foo;
        json responseJson = {"echo125":bar};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/paramNeg"
    }
    resource paramNeg (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        error err;
        string bar;
        bar, err = (string)params.foo;
        json responseJson = {"echo125":bar, "error":err.message};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo13"
    }
    resource echo13 (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        string barStr;
        int bar;
        barStr, _ = (string)params.foo;
        bar, _ = <int>barStr;
        json responseJson = {"echo13":bar};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo14"
    }
    resource echo14 (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        string barStr;
        float bar;
        barStr, _ = (string)params.foo;
        bar, _ = <float>barStr;
        json responseJson = {"echo14":bar};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo15"
    }
    resource echo15 (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        string barStr;
        boolean bar;
        barStr, _ = (string) params.foo;
        bar, _ = <boolean> barStr;
        json responseJson = {"echo15":bar};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/so2"
    }
    resource echo (http:ServerConnector conn, http:Request req) {
    }
}

@http:serviceConfig {
    basePath:"/hello/world",
    endpoints:[testEP]
}
service<http:Service> echo22 {

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2"
    }
    resource echo1 (http:ServerConnector conn, http:Request req) {
        json responseJson = {"echo1":"echo1"};
        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    resource echo2 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo2":"echo2"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2/foo/bar"
    }
    resource echo3 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo3":"echo3"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/",
    endpoints:[testEP]
}
service<http:Service> echo33 {
    resource echo1 (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"third":foo, "echo33":"echo1"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    endpoints:[testEP]
}
service<http:Service> echo44 {

    @http:resourceConfig {
        path:"echo2"
    }
    resource echo221 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"first":"zzz"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    resource echo1 (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        var foo, _ = (string)params.foo;
        json responseJson = {"first":foo, "echo44":"echo1"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    resource echo222 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"first":"bar"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    endpoints:[testEP]
}
service<http:Service> echo55 {
    @http:resourceConfig {
        path:"/foo/bar"
    }
    resource echo1 (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"echo55":"echo55"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/*"
    }
    resource echo2 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo55":"default"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/foo/*"
    }
    resource echo5 (http:ServerConnector conn, http:Request req) {
        map params = req.getQueryParams();
        string foo;
        foo, _ = (string)params.foo;
        json responseJson = {"echo55":"/foo/*"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    endpoints:[testEP]
}
service<http:Service> echo66 {
    @http:resourceConfig {
        path:"/a/*"
    }
    resource echo1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo66": req.extraPathInfo};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/a"
    }
    resource echo2 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        if (req.extraPathInfo == null) {
            req.extraPathInfo = "empty";
        }
        json responseJson = {"echo66": req.extraPathInfo};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/uri",
    endpoints: [testEP]
}
service<http:Service> WildcardService {

    @http:resourceConfig {
        path:"/{id}",
        methods:["POST"]
    }
    resource pathParamResource (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {message: "Path Params Resource is invoked."};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/*"
    }
    resource wildcardResource (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {message: "Wildcard Params Resource is invoked."};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}
