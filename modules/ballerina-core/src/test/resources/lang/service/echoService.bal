import ballerina.net.http;
import ballerina.lang.message;

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
        serviceLevelStr = message:getStringPayload(m);
        http:convertToResponse(m);
        reply m;
    }

    @http:GET
    @http:Path ("/getString")
    resource getString (message m) {
        message response = {};
        message:setStringPayload(response, serviceLevelStr);
        reply response;
    }
}
