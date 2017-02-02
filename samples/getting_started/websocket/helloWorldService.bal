import ballerina.lang.message;
import ballerina.lang.system;
import ballerina.net.http;
import ballerina.net.ws;

@BasePath ("/hello")
    service helloWorld {

    @GET
    resource sayHello(message m) {

        message response;

        response = new message;
        message:setStringPayload(response, "Hello, World!");

        reply response;
    }

    @OnOpen
    resource onOpenMessage(message m) {
        system:println("new websocket connection");
    }

    @OnTextMessage
    resource onTextMessage(message m) {
        ws:sendText(m, "You sent : " + message:getStringPayload(m));
        system:println("Received text message: " + message:getStringPayload(m));
    }

    @OnClose
    resource onCloseMessage(message m) {
        system:println("closed connection");
    }
}
