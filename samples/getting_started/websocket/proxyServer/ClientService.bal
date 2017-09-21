package samples.websocket.proxyServer;

import ballerina.net.ws;

@ws:clientService {}
service<ws> ClientService {

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        ws:Connection parentCon = ws:getParentConnection(conn);
        ws:pushText(parentCon, frame.text);
    }

    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        ws:Connection parentCon = ws:getParentConnection(conn);
        ws:closeConnection(parentCon, 1001, "Server closing connection");
    }

}