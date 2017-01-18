import ballerina.net.http;
import ballerina.lang.system;

@BasePath ("/var")
service echo {

    int int_value;

    @GET
    @Path ("/message")
    resource echo (message m) {
        int_value = 10;
        system:println(int_value);
        http:convertToResponse(m);
        reply m;
    }
}
