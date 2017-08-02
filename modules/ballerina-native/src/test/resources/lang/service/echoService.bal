import ballerina.net.http;
import ballerina.lang.messages;

@http:configuration {basePath:"/echo"}
service<http> echo {

    string serviceLevelStr;

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
}
