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

    @http:GET {}
    @http:Path {value:"/getServiceLevelString"}
    resource getServiceLevelString (message m) {
        message response = {};
        messages:setStringPayload(response, serviceLevelStringVar);
        reply response;
    }

    @http:GET {}
    @http:Path {value:constPath}
    resource constValueAsAttributeValue (message m) {
        message response = {};
        messages:setStringPayload(response, "constant path test");
        reply response;
    }
}

function getConstPath() (string) {
    return "/constantPath";
}
