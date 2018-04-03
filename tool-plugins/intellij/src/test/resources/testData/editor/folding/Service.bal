import <fold text='...'>ballerina.lang.messages;
import ballerina/http;</fold>

@http:BasePath {value:"/hello"}
service<http> helloWorld <fold text='{...}'>{
    //comment
    @http:GET {}
    resource sayHello (message m) <fold text='{...}'>{
        //comment
        message response = {};
        messages:setStringPayload(response, "Hello, World!");
        reply response;
        //comment
    }</fold>
    //comment
}</fold>
