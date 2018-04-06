import ballerina/lang.messages;
import ballerina/http;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (message m) {
        message response = {};
        messages:setStringPayload(response, "Hello, World!");
        response:send(response);
    
    }
    
}
