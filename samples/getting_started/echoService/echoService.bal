import ballerina.net.http;

@BasePath ("/echo")
service echo {

    @POST
    resource echo (message m) {
        http:convertToResponse(m);
        reply m;
    }
}
