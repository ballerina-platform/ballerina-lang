import ballerina/http;
import ballerina/http;

endpoint http:NonListeningServiceEndpoint testEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> echo11 bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    echo1 (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo5":"echo5"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}"
    }
    echo4 (endpoint conn, http:Request req, string abc) {
        http:Response res = {};
        json responseJson = {"echo3":abc};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}/bar"
    }
    echo5 (endpoint conn, http:Request req, string abc) {
        http:Response res = {};
        json responseJson = {"first":abc, "echo4":"echo4"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }


    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    echo7 (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo5":"any"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo3/{abc}"
    }
    echo9 (endpoint conn, http:Request req, string abc) {
        map params = req.getQueryParams();
        string foo;
        foo = <string>params.foo;
        json responseJson = {"first":abc, "second":foo, "echo9":"echo9"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    echo10 (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        string foo;
        foo = <string>params.foo;
        json responseJson = {"third":foo, "echo10":"echo10"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    echo11 (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        string foo;
        foo = <string>params.foo;
        json responseJson = {"third":foo, "echo11":"echo11"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo12/{abc}/bar"
    }
    echo12 (endpoint conn, http:Request req, string abc) {
        http:Response res = {};
        json responseJson = {"echo12":abc};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo125"
    }
    echo125 (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        string bar;
        bar = <string>params.foo;
        json responseJson = {"echo125":bar};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/paramNeg"
    }
    paramNeg (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        string bar = <string>params.foo;
        json responseJson = {"echo125":bar};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo13"
    }
    echo13 (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        string barStr;
        int bar;
        barStr = <string>params.foo;
        error conversionError = {};
        match <int>barStr {
            int val => bar = val;
            error e=> conversionError = e;
        }
        json responseJson = {"echo13":bar};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo14"
    }
    echo14 (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        string barStr;
        float bar;
        barStr = <string>params.foo;
        error conversionError = {};
        match <float>barStr {
            float flo => bar = flo;
            error e=> conversionError = e;
        }
        json responseJson = {"echo14":bar};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo15"
    }
    echo15 (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        string barStr;
        boolean bar;
        barStr = <string>params.foo;
        bar = <boolean>barStr;
        json responseJson = {"echo15":bar};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/so2"
    }
    echo (endpoint conn, http:Request req) {
    }
}

@http:ServiceConfig {
    basePath:"/hello/world"
}
service<http:Service> echo22 bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2"
    }
    echo1 (endpoint conn, http:Request req) {
        json responseJson = {"echo1":"echo1"};
        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    echo2 (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo2":"echo2"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/foo/bar"
    }
    echo3 (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo3":"echo3"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/"
}
service<http:Service> echo33 bind testEP {
    echo1 (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        string foo;
        foo = <string>params.foo;
        json responseJson = {"third":foo, "echo33":"echo1"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

service<http:Service> echo44 bind testEP {

    @http:ResourceConfig {
        path:"echo2"
    }
    echo221 (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"first":"zzz"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    echo1 (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        //string foo;
        string foo = <string>params.foo;
        json responseJson = {"first":foo, "echo44":"echo1"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    echo222 (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"first":"bar"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}


service<http:Service> echo55 bind testEP {
    @http:ResourceConfig {
        path:"/foo/bar"
    }
    echo1 (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        string foo;
        foo = <string>params.foo;
        json responseJson = {"echo55":"echo55"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/*"
    }
    echo2 (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo55":"default"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/foo/*"
    }
    echo5 (endpoint conn, http:Request req) {
        map params = req.getQueryParams();
        string foo;
        foo = <string>params.foo;
        json responseJson = {"echo55":"/foo/*"};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

service<http:Service> echo66 bind testEP {
    @http:ResourceConfig {
        path:"/a/*"
    }
    echo1 (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo66":req.extraPathInfo};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/a"
    }
    echo2 (endpoint conn, http:Request req) {
        http:Response res = {};
        if (req.extraPathInfo == null) {
            req.extraPathInfo = "empty";
        }
        json responseJson = {"echo66":req.extraPathInfo};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/uri"
}
service<http:Service> WildcardService bind testEP {

    @http:ResourceConfig {
        path:"/{id}",
        methods:["POST"]
    }
    pathParamResource (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {message:"Path Params Resource is invoked."};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/*"
    }
    wildcardResource (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {message:"Wildcard Params Resource is invoked."};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/go/{aaa}/{bbb}/{ccc}"
    }
    threePathParams (endpoint conn, http:Request req, string aaa, string bbb, string ccc) {
        http:Response res = {};
        json responseJson = {aaa:aaa, bbb:bbb, ccc:ccc};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/go/{xxx}/{yyy}"
    }
    twoPathParams (endpoint conn, http:Request req, string xxx, string yyy) {
        http:Response res = {};
        json responseJson = {xxx:xxx, yyy:yyy};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/Go"
    }
    CapitalizedPathParams (endpoint conn, http:Request req) {
        http:Response res = {};
        json responseJson = {value:"capitalized"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}
