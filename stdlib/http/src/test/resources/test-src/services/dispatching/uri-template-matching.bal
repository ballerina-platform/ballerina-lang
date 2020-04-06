import ballerina/http;
import ballerina/lang.'float as langfloat;
import ballerina/lang.'int as langint;
import ballerina/stringutils;

listener http:MockListener testEP = new(9090);

const string pathAsConst = "/{foo}/info/{prodId}";

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
    resource function echo4(http:Caller caller, http:Request req, @http:PathParam string abc) {
        http:Response res = new;
        json responseJson = {"echo3":abc};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{abc}/bar"
    }
    resource function echo5(http:Caller caller, http:Request req, @http:PathParam string abc) {
        http:Response res = new;
        json responseJson = {"first":abc, "echo4":"echo4"};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{xyz}.id"
    }
    resource function echo6(http:Caller caller, http:Request req, @http:PathParam string xyz) {
        http:Response res = new;
        json responseJson = {"echo6":xyz};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/literal.id"
    }
    resource function echo6_1(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo6":"literal invoked"};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{zz}.id/foo"
    }
    resource function echo6_2(http:Caller caller, http:Request req, @http:PathParam string zz) {
        http:Response res = new;
        json responseJson = {"echo6":"specific path invoked"};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2/{xyz}.identity"
    }
    resource function echo6_3(http:Caller caller, http:Request req, @http:PathParam string xyz) {
        http:Response res = new;
        json responseJson = {"echo6":"identity"};
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
    resource function echo9(http:Caller caller, http:Request req, @http:PathParam string abc,
                            @http:QueryParam string foo) {
        http:Response res = new;
        json responseJson = {first:abc, second:foo, echo9:"echo9"};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function echo10SafeCoding(http:Caller caller, http:Request req) {
        map<string[]> params = req.getQueryParams();
        string[]? foo = params["foo"];
        json responseJson = {"third":(foo is string[] ? foo[0] : "go"), "echo10":"echo10"};

        http:Response res = new;
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/unsafe"
    }
    resource function echo10UnsafeCoding(http:Caller caller, http:Request req, @http:QueryParam string foo) {
        http:Response res = new;
        json responseJson = {third: foo, echo10: "echo10UnsafeCoding"};
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
    resource function echo12(http:Caller caller, http:Request req, @http:PathParam string abc) {
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
        path:"/echo155unsafe"
    }
    resource function sameNameUnsafe(http:Caller caller, http:Request req, @http:QueryParam string[] foo,
                               @http:QueryParam string[] bar) {
        http:Response res = new;
        json responseJson = {name1: foo[0] , name2: foo[1] , name3: bar[0], name4: foo[2]};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo156/{key}"
    }
    resource function allApis(http:Caller caller, http:Request req, @http:PathParam string key) {
        map<string[]> paramMap = req.getQueryParams();

        string[]? paramVals = paramMap[key];
        string mapVal = paramVals is string[] ? paramVals[0] : "";

        string[]? paramVals2 = paramMap["foo"];
        string mapVal2 = paramVals2 is string[] ? paramVals2[0] : "";

        string mapVal3 = paramVals is string[] ? paramVals[1] : "";
        json responseJson = {"map": mapVal , "map_":mapVal2, "array_":mapVal3 };
        checkpanic caller->respond(<@untainted> responseJson);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo156unsafe/{key}"
    }
    resource function allApisUnsafe(http:Caller caller, http:Request req, @http:PathParam string key,
                              @http:QueryParam string[] bar, @http:QueryParam string foo) {
        json responseJson = {"arr_0": bar[0] , "arr_1":bar[1], "string":foo };
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
    resource function threePathParams(http:Caller caller, http:Request req, @http:PathParam string aaa,
                                      @http:PathParam string bbb, @http:PathParam string ccc) {
        http:Response res = new;
        json responseJson = {aaa:aaa, bbb:bbb, ccc:ccc};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/go/{xxx}/{yyy}"
    }
    resource function twoPathParams(http:Caller caller, http:Request req, @http:PathParam string xxx,
                                    @http:PathParam string yyy) {
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
    resource function twistedPathParams(http:Caller caller, http:Request req, @http:PathParam string name,
                                        @http:PathParam string age) {
        http:Response res = new;
        json responseJson = { Name:name, Age:age };
        checkpanic caller->respond(<@untainted> responseJson);
    }

    @http:ResourceConfig {
        path:"/type/{age}/{name}/{status}/{weight}"
    }
    resource function MultiTypedPathParams(http:Caller caller, http:Request req, @http:PathParam string name,
                                           @http:PathParam int age, @http:PathParam float weight, @http:PathParam
                                           boolean status) {
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

    @http:ResourceConfig {
        path: pathAsConst
    }
    resource function allin1(http:Caller caller, http:Request req, @http:PathParam string foo,
                             @http:BodyParam string payload, @http:QueryParam string bar, @http:QueryParam string[] baz,
                             @http:PathParam int prodId) {
        json responseJson = { path1 : foo, path2 : prodId, body : payload, query1 : bar, query2: baz[0], query3 : baz[0] };
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
    resource function encodedPath(http:Caller caller, http:Request req, @http:PathParam string aaa,
                                  @http:PathParam string bbb, @http:PathParam string ccc) {
        http:Response res = new;
        json responseJson = {aaa:aaa, bbb:bbb, ccc:ccc};
        res.setJsonPayload(<@untainted json> responseJson);
        checkpanic caller->respond(res);
    }
}
