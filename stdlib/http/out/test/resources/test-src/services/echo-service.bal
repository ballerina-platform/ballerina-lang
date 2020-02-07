import ballerina/http;

final string constPath = getConstPath();

type Person record {
    string name;
    int age;
};

listener http:MockListener echoEP  = new(9090);

string globalLevelStr = "";

@http:ServiceConfig {basePath:"/echo"}
service echo on echoEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message"
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message_worker"
    }
    resource function message_worker(http:Caller caller, http:Request req) {
        //worker w1 {
            http:Response res = new;
            checkpanic caller->respond(res);
        //}
        //worker w2 {
        //    int x = 0;
        //    int a = x + 1;
        //}
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/setString"
    }
    resource function setString(http:Caller caller, http:Request req) {
        http:Response res = new;
        string payloadData = "";
        var payload = req.getTextPayload();
        if (payload is error) {
            return;
        } else {
            payloadData = payload;
        }
        globalLevelStr = <@untainted string> payloadData;
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/getString"
    }
    resource function getString(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload(<@untainted> globalLevelStr);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"]
    }
    resource function removeHeaders(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setHeader("header1", "wso2");
        res.setHeader("header2", "ballerina");
        res.setHeader("header3", "hello");
        res.removeAllHeaders();
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:constPath
    }
    resource function connstValueAsAttributeValue(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("constant path test");
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/testEmptyResourceBody"
    }
    resource function testEmptyResourceBody(http:Caller caller, http:Request req) {
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/getFormParams"
    }
    resource function getFormParams(http:Caller caller, http:Request req) {
        var params = req.getFormParams();
        http:Response res = new;
        if (params is map<string>) {
            string? name = params["firstName"];
            string? team = params["team"];
            json responseJson = {"Name":(name is string ? name : "") , "Team":(team is string ? team : "")};
            res.setJsonPayload(<@untainted json> responseJson);
        } else {
            if (params is http:GenericClientError) {
                error? cause = params.detail()?.cause;
                string? errorMsg;
                if (cause is error) {
                    errorMsg = cause.detail()?.message;
                } else {
                    errorMsg = params.detail()?.message;
                }
                if (errorMsg is string) {
                    res.setPayload(<@untainted string> errorMsg);
                } else {
                    res.setPayload("Error occrred");
                }
            } else {
                error err = params;
                string? errMsg = <string> err.detail()?.message;
                res.setPayload(errMsg is string ? <@untainted string> errMsg : "Error in parsing form params");
            }
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["PATCH"],
        path:"/modify"
    }
    resource function modify11(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.statusCode = 204;
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/parseJSON"
    }
    resource function errorReturn(http:Caller caller, http:Request req) returns @tainted error? {
        json payload = check req.getJsonPayload();
        http:Response res = new;
        res.setPayload(<@untainted json> payload);
        res.statusCode = 200;
        checkpanic caller->respond(res);
    }
}

function getConstPath() returns(string) {
    return "/constantPath";
}

@http:ServiceConfig {}
service hello on echoEP {

    @http:ResourceConfig {}
    resource function echo(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Uninitialized configs");
    }

    resource function testFunctionCall(http:Caller caller, http:Request req) {
        checkpanic caller->respond(<@untained> self.nonRemoteFunctionCall());
    }

    function nonRemoteFunctionCall() returns string {
        return "Non remote function invoked";
    }
}
