import ballerina.lang.message;

@BasePath ("/hello")
service helloWorld {

    @GET
    resource sayHello(message m) {

        message response;

        response = new message;
        message:setStringPayload(response, "Hello, World!");
//        message:setHeader(response, "Content-Encoding", "deflate");

        reply response;
    }
}
