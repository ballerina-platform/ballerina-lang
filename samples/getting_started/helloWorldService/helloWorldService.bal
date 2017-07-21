import ballerina.lang.messages;
import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:GET{}
    @http:Path {value:"/"}
    resource sayHello (message m) {
        message response = {};
        messages:setStringPayload(response, "Hello, World!");
        reply response;
    
    }
    
}
