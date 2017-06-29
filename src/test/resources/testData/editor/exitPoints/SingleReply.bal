import ballerina.net.http;
import ballerina.lang.messages;

@http:BasePath {value:"/hello"}
service<http> HelloService {

    @http:GET {}
    resource sayHello (message m) {
        message response = {};
        messages:setStringPayload(response, "Hello World !!!");
        rep<caret>ly response;
    }
}
