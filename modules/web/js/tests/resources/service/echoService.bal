import ballerina.net.http;
import ballerina.lang.messages;

@http:BasePath ("/echo")
service echo {

    string serviceLevelStr;

    @http:GET
    @http:Path ("/message")
    resource echo (message m) {
        reply m;
    }

    @http:POST
    @http:Path ("/setString")
    resource setString (message m) {
        serviceLevelStr = messages:getStringPayload(m);
        http:convertToResponse(m);
        reply m;
    }

    @http:GET
    @http:Path ("/getString")
    resource getString (message m) {
        message response = {};
        messages:setStringPayload(response, serviceLevelStr);
        reply response;
    }
}
