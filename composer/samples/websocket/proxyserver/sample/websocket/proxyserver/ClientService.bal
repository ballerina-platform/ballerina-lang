
import ballerina/net.ws;

@ws:clientService {}
service<ws> ClientService {

    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        ws:Connection parentCon = conn.getParentConnection();
        parentCon.pushText(frame.text);
    }

    resource onClose (ws:Connection conn, ws:CloseFrame frame) {
        ws:Connection parentCon = conn.getParentConnection();
        parentCon.closeConnection(frame.statusCode, frame.reason);
    }
}
