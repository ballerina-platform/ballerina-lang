import ballerina.net.http;
import ballerina.lang.messages;

@BasePath {value:"/echo"}
service echo {

    string serviceLevelStr;

    @GET {}
    @Path ("/message")
    resource echo (message m) {
        reply m;
    }

    @POST {}
    @Path {value:"/setString"}
    resource setString (message m) {
        serviceLevelStr = messages:getStringPayload(m);
        http:convertToResponse(m);
        reply m;
    }

    @GET {}
    @Path {value:"/getString"}
    resource getString (message m) {
        message response = {};
        messages:setStringPayload(response, serviceLevelStr);
        reply response;
    }
}
