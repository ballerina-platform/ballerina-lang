import ballerina.lang.message;

@http:BasePath ("/hello")
service helloWorld {

    @http:GET
    resource sayHello(message m) {
        message response = {};
        message:setStringPayload(response, "Hello, World!");
        reply response;
    }
}
