import ballerina.net.http;

@BasePath ("/echo")
service echo {

    @GET
    @Path ("/message")
    resource echo (message m) {
        http:convertToResponse(m);
        reply m;
    }
}
