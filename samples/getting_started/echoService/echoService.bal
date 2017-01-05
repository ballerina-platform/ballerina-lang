import ballerina.net.http;

@BasePath ("/echo")
service EchoService {

    @POST
    resource echo (message m) {
        http:convertToResponse(m);
        reply m;

    }

}