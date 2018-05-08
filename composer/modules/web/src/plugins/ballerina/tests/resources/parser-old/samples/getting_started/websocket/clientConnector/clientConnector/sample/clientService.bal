
import ballerina/net.ws;
import ballerina/lang.system;
import ballerina/lang.messages;

@ws:ClientService {}
service<ws> clientService {

    @ws:OnTextMessage {}
    resource ontext(message m) {
        system:println("client service : " + messages:getStringPayload(m));
        ws:pushText("client service : " + messages:getStringPayload(m));
    }

    @ws:OnClose {}
    resource onClose(message m) {
        system:println("Closed client connection");
    }
}
