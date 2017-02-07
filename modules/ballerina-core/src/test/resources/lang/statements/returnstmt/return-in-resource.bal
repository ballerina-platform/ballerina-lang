@BasePath ("/hello")
service helloWorld {

    @GET
    resource sayHello(message m) {
        message response = {};
        return response;
    }
}
