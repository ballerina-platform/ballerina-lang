import ballerina.net.http;
import ballerina.lang.messages;

@http:BasePath {value:"/hello"}
service HelloService {

    @http:GET {}
    resource sayHello (message m) {
        message response = {};
        string payload = messages:getStringPayload(m);
        if (payload == "") {
            messages:setStringPayload(response, "Hello " + payload + " !!!");
            rep<caret>ly response;
        } else {
            messages:setStringPayload(response, "Hello World !!!");
            reply response;
        }
    }
}
