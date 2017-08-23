import ballerina.net.http;
import ballerina.lang.messages;

const string constPath = getConstPath();

@http:configuration {basePath:"/echo"}
service<http> echo {

    string serviceLevelStr;

    string serviceLevelStringVar = "sample value";

    @http:resourceConfig {
        methods:["GET"],
        path:"/message"
    }
    resource echo (message m) {
        reply m;
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/setString"
    }
    resource setString (message m) {
        serviceLevelStr = messages:getStringPayload(m);
        http:convertToResponse(m);
        reply m;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/getString"
    }
    resource getString (message m) {
        message response = {};
        // TODO : Fix bellow line
        // messages:setStringPayload(response, serviceLevelStr);
        messages:setStringPayload(response, "hello");
        reply response;
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource removeHeaders (message m) {
        messages:removeAllHeaders(m);
        reply m;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/getServiceLevelString"
    }
    resource getServiceLevelString (message m) {
        message response = {};
        messages:setStringPayload(response, serviceLevelStringVar);
        reply response;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:constPath
    }
    resource constValueAsAttributeValue (message m) {
        message response = {};
        messages:setStringPayload(response, "constant path test");
        reply response;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/testEmptyResourceBody"
    }
    resource testEmptyResourceBody (message m) {
    }

    @http:resourceConfig {
        methods:["POST"]
    }
    resource getFormParams (message m) {
        message response = {};
        map params = http:getFormParams(m);
        var name,_ = (string)params.firstName;
        var team,_ = (string)params.team;
        json responseJson = {"Name":name , "Team":team};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }
}

function getConstPath() (string) {
    return "/constantPath";
}
