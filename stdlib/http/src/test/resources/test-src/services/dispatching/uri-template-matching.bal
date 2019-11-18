import ballerina/http;
import ballerina/lang.'float as langfloat;
import ballerina/lang.'int as langint;
import ballerina/stringutils;

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
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}"
    }
    resource function echo4(http:Caller caller, http:Request req, string abc) {
        http:Response res = new;
        json responseJson = {"echo3":abc};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}/bar"
    }
    resource function echo5(http:Caller caller, http:Request req, string abc) {
        http:Response res = new;
        json responseJson = {"first":abc, "echo4":"echo4"};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }


    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    resource function echo7(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo5":"any"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo3/{abc}"
    }
    resource function echo9(http:Caller caller, http:Request req, string abc) {
        map<string[]> params = req.getQueryParams();
        string[]? foo = params["foo"];
        json responseJson = {"first":abc, "second":(foo is string[] ? foo[0] : "go"), "echo9":"echo9"};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function echo10(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? foo = params["foo"];
        json responseJson = {"third":(foo is string[] ? foo[0] : "go"), "echo10":"echo10"};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    resource function echo11(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? foo = params["foo"];
        json responseJson = {"third":(foo is string[] ? foo[0] : ""), "echo11":"echo11"};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo12/{abc}/bar"
    }
    resource function echo12(http:Caller caller, http:Request req, string abc) {
        http:Response res = new;
        json responseJson = {"echo12":abc};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo125"
    }
    resource function echo125(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? bar = params["foo"];
        json responseJson = {"echo125":(bar is string[] ? bar[0] : "")};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/paramNeg"
    }
    resource function paramNeg(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? bar = params["foo"] ?: [""];
        json responseJson = {"echo125":(bar is string[] ? bar[0] : "")};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo13"
    }
    resource function echo13(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? barStr = params["foo"];
        var result = langint:fromString(barStr is string[] ? barStr[0] : "0");
        int bar = (result is int) ? result : 0;
        json responseJson = {"echo13":bar};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo14"
    }
    resource function echo14(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? barStr = params["foo"];
        var result = langfloat:fromString(barStr is string[] ? barStr[0] : "0.0");
        float bar = (result is float) ? result : 0.0;
        json responseJson = {"echo14":bar};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo15"
    }
    resource function echo15(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? barStr = params["foo"];
        string val = barStr is string[] ? barStr[0] : "";
        boolean bar = stringutils:toBoolean(val);
        json responseJson = {"echo15":bar};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo155"
    }
    resource function sameName(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? foo = params["foo"];
        string[]? bar = params["bar"];
        string name1 = foo is string[] ? foo[0] : "";
        string name2 = foo is string[] ? foo[1] : "";
        string name3 = bar is string[] ? bar[0] : "";
        string name4 = foo is string[] ? foo[2] : "";
        json responseJson = {"name1":name1 , "name2":name2, "name3":(name3 != "" ? name3 : ()),
                                "name4":name4};
        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo156/{key}"
    }
    resource function allApis(http:Caller caller, http:Request req, string key) {
        map<string[]> paramMap = req.getQueryParams();
        string[] valueArray = req.getQueryParamValues(<@untainted> key) ?: ["array not found"];
        string value = req.getQueryParamValue(<@untainted> key) ?: "value not found";
        string[]? paramVals = paramMap[key];
        string mapVal = paramVals is string[] ? paramVals[0] : "";
        string[]? paramVals2 = paramMap["foo"];
        string mapVal2 = paramVals2 is string[] ? paramVals2[0] : "";
        json responseJson = {"map":mapVal , "array":valueArray[0], "value":value,
                                "map_":mapVal2, "array_":valueArray[1] };
        //http:Response res = new;
        //res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(<@untainted> responseJson);
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
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/*"
    }
    resource function echo2(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo2":"echo2"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/foo/bar"
    }
    resource function echo3(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo3":"echo3"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/"
}
service echo33 on testEP {
    resource function echo1(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? foo = params["foo"];
        json responseJson = {"third":(foo is string[] ? foo[0] : ""), "echo33":"echo1"};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
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
        checkpanic caller->respond(res);
    }

    resource function echo1(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? foo = params["foo"];
        json responseJson = {"first":(foo is string[] ? foo[0] : ""), "echo44":"echo1"};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"echo2"
    }
    resource function echo222(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"first":"bar"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }
}


service echo55 on testEP {
    @http:ResourceConfig {
        path:"/foo/bar"
    }
    resource function echo1(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? foo = params["foo"];
        json responseJson = {"echo55":"echo55"};

        http:Response res = new;
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/*"
    }
    resource function echo2(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo55":"default"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/foo/*"
    }
    resource function echo5(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[] foo = params["foo"] ?: [];
        json responseJson = {"echo55":"/foo/*"};

        http:Response res = new;
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }
}

service echo66 on testEP {
    @http:ResourceConfig {
        path:"/a/*"
    }
    resource function echo1(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo66":req.extraPathInfo};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
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
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
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
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/*"
    }
    resource function wildcardResource(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {message:"Wildcard Params Resource is invoked."};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/go/{aaa}/{bbb}/{ccc}"
    }
    resource function threePathParams(http:Caller caller, http:Request req, string aaa, string bbb, string ccc) {
        http:Response res = new;
        json responseJson = {aaa:aaa, bbb:bbb, ccc:ccc};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/go/{xxx}/{yyy}"
    }
    resource function twoPathParams(http:Caller caller, http:Request req, string xxx, string yyy) {
        http:Response res = new;
        json responseJson = {xxx:xxx, yyy:yyy};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/Go"
    }
    resource function CapitalizedPathParams(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {value:"capitalized"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/twisted/{age}/{name}"
    }
    resource function twistedPathParams(http:Caller caller, http:Request req, string name, string age) {
        http:Response res = new;
        json responseJson = { Name:name, Age:age };
        checkpanic caller->respond(<@untainted> responseJson);
    }

    @http:ResourceConfig {
        path:"/type/{age}/{name}/{status}/{weight}"
    }
    resource function MultiTypedPathParams(http:Caller caller, http:Request req, string name, int age,
                                            float weight, boolean status) {
        http:Response res = new;
        int balAge = age + 1;
        float balWeight = weight + 2.95;
        string balName = name + " false";
        if (status) {
            balName = name;
        }
        json responseJson = { Name:name, Age:balAge, Weight:balWeight, Status:status, Lang: balName};
        checkpanic caller->respond(<@untainted> responseJson);
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
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }
}
