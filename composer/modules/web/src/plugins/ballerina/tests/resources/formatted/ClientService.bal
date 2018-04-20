
import ballerina/lang.system;
import ballerina/net.ws;

@ws:clientService {}
service<ws> ClientService {

    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        system:println("Received text from remote server: " + frame.text);
    }

}