package sample.websocket.client;

import ballerina.net.ws;

@ws:clientService {}
service<ws> ClientService {

    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        println("Receive messsage from remote server: " + frame.text);
    }
}
