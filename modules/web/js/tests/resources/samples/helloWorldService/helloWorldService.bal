import ballerina.lang.messages;
@http:BasePath ("/hello")
service helloWorld {
    
    @http:GET
    resource sayHello (message m) {
        message response = {};
        messages:setStringPayload(response, "Hello, World!");
        reply response;
    
    }
    
}
