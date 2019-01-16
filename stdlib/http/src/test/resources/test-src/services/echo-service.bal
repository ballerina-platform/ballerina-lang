import ballerina/mime;
import ballerina/http;
import ballerina/io;

final string constPath = getConstPath();

type Person record {
    string name;
    int age;
};

listener http:MockListener echoEP  = new(9090);

@http:ServiceConfig {basePath:"/echo"}
service echo on echoEP {

    string serviceLevelStr = "";
    string serviceLevelStringVar = "sample value";

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message"
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message_worker"
    }
    resource function message_worker(http:Caller caller, http:Request req) {
        worker w1 {
            http:Response res = new;
            _ = caller->respond(res);
        }
        worker w2 {
            int x = 0;
            int a = x + 1;
        }
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
        self.serviceLevelStr = untaint payloadData;
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/getString"
    }
    resource function getString(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload(self.serviceLevelStr);
        _ = caller -> respond(res);
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
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/getServiceLevelString"
    }
    resource function getServiceLevelString(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload(self.serviceLevelStringVar);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:constPath
    }
    resource function connstValueAsAttributeValue(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("constant path test");
        _ = caller->respond(res);
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
            string name = "";
            string team = "";
            if (params.hasKey("firstName")) {
                name = params.firstName;
            }
            if (params.hasKey("team")) {
                team = params.team;
            }
            json responseJson = {"Name":name , "Team":team};
            res.setJsonPayload(untaint responseJson);
        } else {
            string errMsg = <string> params.detail().message;
            res.setPayload(untaint errMsg);
        }
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["PATCH"],
        path:"/modify"
    }
    resource function modify11(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.statusCode = 204;
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/parseJSON"
    }
    resource function errorReturn(http:Caller caller, http:Request req) returns error? {
        json payload = check req.getJsonPayload();
        http:Response res = new;
        res.setPayload(untaint payload);
        res.statusCode = 200;
        _ = caller->respond(res);
    }
}

function getConstPath() returns(string) {
    return "/constantPath";
}

@http:ServiceConfig
service hello on echoEP {

    @http:ResourceConfig
    resource function echo(http:Caller caller, http:Request req) {
        _ = caller->respond("Uninitialized configs");
    }
}
