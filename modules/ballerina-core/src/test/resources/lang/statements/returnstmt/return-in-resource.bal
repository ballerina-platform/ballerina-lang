import ballerina.lang.message;

@BasePath ("/hello")
service helloWorld {

    @GET
    resource sayHello(message m) {
        message response;
        response = new message;
        return response;
    }
}
