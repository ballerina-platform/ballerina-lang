import ballerina/http;

listener http:MockListener testEP = new(9090);

@http:ServiceConfig {
    basePath:"/hello"
}
service echo11 on testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    resource function echo1(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo5":"echo5"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}"
    }
    resource function echo4(http:Caller caller, http:Request req, string abc) {
        http:Response res = new;
        json responseJson = {"echo3":abc};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}/bar"
    }
    resource function echo5(http:Caller caller, http:Request req, string abc) {
        http:Response res = new;
        json responseJson = {"first":abc, "echo4":"echo4"};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{xyz}.id"
    }
    resource function echo6(http:Caller caller, http:Request req, string xyz) {
        http:Response res = new;
        json responseJson = {"echo6":xyz};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/literal.id"
    }
    resource function echo6_1(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo6":"literal invoked"};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{zz}.id/foo"
    }
    resource function echo6_2(http:Caller caller, http:Request req, string zz) {
        http:Response res = new;
        json responseJson = {"echo6":"specific path invoked"};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{xyz}.identity"
    }
    resource function echo6_3(http:Caller caller, http:Request req, string xyz) {
        http:Response res = new;
        json responseJson = {"echo6":"identity"};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    resource function echo7(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo5":"any"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo3/{abc}"
    }
    resource function echo9(http:Caller caller, http:Request req, string abc) {
        string foo = req.getQueryParams().foo;
        json responseJson = {"first":abc, "second":foo, "echo9":"echo9"};

        http:Response res = new;
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function echo10(http:Caller caller, http:Request req) {
        string foo = req.getQueryParams().foo;
        json responseJson = {"third":foo, "echo10":"echo10"};

        http:Response res = new;
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    resource function echo11(http:Caller caller, http:Request req) {
        string foo = req.getQueryParams().foo;
        json responseJson = {"third":foo, "echo11":"echo11"};

        http:Response res = new;
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo12/{abc}/bar"
    }
    resource function echo12(http:Caller caller, http:Request req, string abc) {
        http:Response res = new;
        json responseJson = {"echo12":abc};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo125"
    }
    resource function echo125(http:Caller caller, http:Request req) {
        string bar = req.getQueryParams().foo;
        json responseJson = {"echo125":bar};

        http:Response res = new;
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/paramNeg"
    }
    resource function paramNeg(http:Caller caller, http:Request req) {
        map<string> params = req.getQueryParams();
        string bar = params["foo"] ?: "";
        json responseJson = {"echo125":bar};

        http:Response res = new;
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo13"
    }
    resource function echo13(http:Caller caller, http:Request req) {
        string barStr = req.getQueryParams().foo;
        var result = int.convert(barStr);
        int bar = (result is int) ? result : 0;
        json responseJson = {"echo13":bar};

        http:Response res = new;
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo14"
    }
    resource function echo14(http:Caller caller, http:Request req) {
        string barStr = req.getQueryParams().foo;
        var result = float.convert(barStr);
        float bar = (result is float) ? result : 0.0;
        json responseJson = {"echo14":bar};

        http:Response res = new;
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo15"
    }
    resource function echo15(http:Caller caller, http:Request req) {
        string barStr = req.getQueryParams().foo;
        boolean bar = boolean.convert(barStr);
        json responseJson = {"echo15":bar};

        http:Response res = new;
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/so2"
    }
    resource function echo(http:Caller caller, http:Request req) {
    }
}

@http:ServiceConfig {
    basePath:"/hello/world"
}
service echo22 on testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2"
    }
    resource function echo1(http:Caller caller, http:Request req) {
        json responseJson = {"echo1":"echo1"};
        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    resource function echo2(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo2":"echo2"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/foo/bar"
    }
    resource function echo3(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo3":"echo3"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/"
}
service echo33 on testEP {
    resource function echo1(http:Caller caller, http:Request req) {
        string foo = req.getQueryParams().foo;
        json responseJson = {"third":foo, "echo33":"echo1"};

        http:Response res = new;
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }
}

service echo44 on testEP {

    @http:ResourceConfig {
        path:"echo2"
    }
    resource function echo221(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"first":"zzz"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }

    resource function echo1(http:Caller caller, http:Request req) {
        string foo = req.getQueryParams().foo;
        json responseJson = {"first":foo, "echo44":"echo1"};

        http:Response res = new;
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    resource function echo222(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"first":"bar"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }
}

service echo55 on testEP {
    @http:ResourceConfig {
        path:"/foo/bar"
    }
    resource function echo1(http:Caller caller, http:Request req) {
        string foo = req.getQueryParams().foo;
        json responseJson = {"echo55":"echo55"};

        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/*"
    }
    resource function echo2(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo55":"default"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/foo/*"
    }
    resource function echo5(http:Caller caller, http:Request req) {
        map<string> params = req.getQueryParams();
        string foo = params["foo"] ?: "";
        json responseJson = {"echo55":"/foo/*"};

        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }
}

service echo66 on testEP {
    @http:ResourceConfig {
        path:"/a/*"
    }
    resource function echo1(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo66":req.extraPathInfo};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/a"
    }
    resource function echo2(http:Caller caller, http:Request req) {
        http:Response res = new;
        if (req.extraPathInfo == "") {
            req.extraPathInfo = "empty";
        }
        json responseJson = {"echo66":req.extraPathInfo};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/uri"
}
service WildcardService on testEP {

    @http:ResourceConfig {
        path:"/{id}",
        methods:["POST"]
    }
    resource function pathParamResource(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {message:"Path Params Resource is invoked."};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/*"
    }
    resource function wildcardResource(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {message:"Wildcard Params Resource is invoked."};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/go/{aaa}/{bbb}/{ccc}"
    }
    resource function threePathParams(http:Caller caller, http:Request req, string aaa, string bbb, string ccc) {
        http:Response res = new;
        json responseJson = {aaa:aaa, bbb:bbb, ccc:ccc};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/go/{xxx}/{yyy}"
    }
    resource function twoPathParams(http:Caller caller, http:Request req, string xxx, string yyy) {
        http:Response res = new;
        json responseJson = {xxx:xxx, yyy:yyy};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/Go"
    }
    resource function CapitalizedPathParams(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {value:"capitalized"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/encodedUri"
}
service URLEncodeService on testEP {
    @http:ResourceConfig {
        path:"/test/{aaa}/{bbb}/{ccc}"
    }
    resource function encodedPath(http:Caller caller, http:Request req, string aaa, string bbb, string ccc) {
        http:Response res = new;
        json responseJson = {aaa:aaa, bbb:bbb, ccc:ccc};
        res.setJsonPayload(untaint responseJson);
        _ = caller->respond(res);
    }
}
