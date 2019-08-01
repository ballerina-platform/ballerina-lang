import ballerina/http;

listener http:Listener helloWorldEP = new (19090);
const JOB = "job";
final string SS = "job2";
const EMPTY_STRING = "";
const FORWARD_SLASH = "/";

any globalLevelVariable = "";
service sample on helloWorldEP {
    any serviceLevelVariable = "";
    @tainted any taintedServiceVar = "";
    http:Client cl = new("http://postman-echo.com");

    resource function params (http:Caller caller, http:Request req) {
        var bar = req.getQueryParamValue("bar");
        self.taintedServiceVar = bar;

        self.serviceLevelVariable = "static";
        globalLevelVariable = "static";
        modifyParamVar([JOB, SS], taintedVal());
        var response = self.cl->get(prepareUrl([<@untainted> JOB]));
    }
}

function modifyParamVar(string[] s, string tainted) {
    s[0] = tainted;
}

function taintedVal() returns @tainted string {
    return "val";
}

function prepareUrl(string[] paths) returns string {
    string url = EMPTY_STRING;

    if (paths.length() > 0) {
        foreach var path in paths {
            if (!path.startsWith(FORWARD_SLASH)) {
                url = url + FORWARD_SLASH;
            }
            url = url + path;
        }
    }
    return url;
}
