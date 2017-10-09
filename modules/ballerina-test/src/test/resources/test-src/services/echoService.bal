import ballerina.net.http;
import ballerina.net.http.request;
import ballerina.net.http.response;

const string constPath = getConstPath();

@http:configuration {basePath:"/echo"}
service<http> echo {

    string serviceLevelStr;

    string serviceLevelStringVar = "sample value";

    @http:resourceConfig {
        methods:["GET"],
        path:"/message"
    }
    resource echo (http:Request req, http:Response res) {
        response:send(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/setString"
    }
    resource setString (http:Request req, http:Response res) {
        serviceLevelStr = request:getStringPayload(req);
        //response:setStringPayload(res, serviceLevelStr);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/getString"
    }
    resource getString (http:Request req, http:Response res) {
        response:setStringPayload(res, serviceLevelStr);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource removeHeaders (http:Request req, http:Response res) {
        request:removeAllHeaders(req);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/getServiceLevelString"
    }
    resource getServiceLevelString (http:Request req, http:Response res) {
        response:setStringPayload(res, serviceLevelStringVar);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:constPath
    }
    resource constValueAsAttributeValue (http:Request req, http:Response res) {
        response:setStringPayload(res, "constant path test");
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/testEmptyResourceBody"
    }
    resource testEmptyResourceBody (http:Request req, http:Response res) {
    }

    @http:resourceConfig {
        methods:["POST"]
    }
    resource getFormParams (http:Request req, http:Response res) {
        map params = request:getFormParams(req);
        string name;
        name,_ = (string)params.firstName;
        string team;
        team,_ = (string)params.team;
        json responseJson = {"Name":name , "Team":team};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}

function getConstPath() (string) {
    return "/constantPath";
}
