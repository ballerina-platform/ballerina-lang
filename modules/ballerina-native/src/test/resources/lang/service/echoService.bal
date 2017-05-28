import ballerina.net.http;
import ballerina.lang.messages;

@http:BasePath {value:"/echo"}
service echo {

    string serviceLevelStr;

    @http:GET {}
    @http:Path {value : "/message"}
    resource echo (message m, @http:QueryParam {value : "msg" } string msg) {
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
        messages:setStringPayload(response, serviceLevelStr);
        reply response;
    }
}
