import ballerina.net.http;
import ballerina.lang.messages;

@http:configuration {basePath:"/echo"}
service<http> echo {

    string serviceLevelStr;

    @http:GET {}
    @http:Path {value : "/message"}
    resource echo (message m) {
        reply m;
    }

    @http:POST {}
    @http:Path {value:"/setString"}
    resource setString (message m) {
        serviceLevelStr = messages:getStringPayload(m);
        http:convertToResponse(m);
        reply m;
    }

    @http:GET {}
    @http:Path {value:"/getString"}
    resource getString (message m) {
        message response = {};
        // TODO : Fix bellow line
        // messages:setStringPayload(response, serviceLevelStr);
        messages:setStringPayload(response, "hello");
        reply response;
    }

    @http:GET {}
    resource removeHeaders (message m) {
        messages:removeAllHeaders(m);
        reply m;
    }
}
