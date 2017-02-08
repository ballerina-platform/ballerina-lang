import ballerina.net.http;
import ballerina.lang.message;

@BasePath ("/echo")
service echo {

    string serviceLevelStr;

    @GET
    @Path ("/message")
    resource echo (message m) {
        reply m;
    }

    @POST
    @Path ("/setString")
    resource setString (message m) {
        serviceLevelStr = message:getStringPayload(m);
        http:convertToResponse(m);
        reply m;
    }

    @GET
    @Path ("/getString")
    resource getString (message m) {
        message response = {};
        message:setStringPayload(response, serviceLevelStr);
        reply response;
    }
}
