import ballerina.lang.messages;
import ballerina.net.http;

@http:BasePath {value:"/hello"}
service helloWorld {
    
    @http:GET{}
    @http:Path {value:"/"}
    resource sayHello (message m) {
        //a new message object is created and used in client response creation.
        message response = {};
        messages:setStringPayload(response, "Hello, World!");
        reply response;
    
    }
    
}
