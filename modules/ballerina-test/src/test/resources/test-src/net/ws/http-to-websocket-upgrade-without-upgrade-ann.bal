import ballerina.net.http;
import ballerina.net.ws;

@http:configuration {
    basePath:"/test2",
    port:9090,
    webSocket:@http:webSocket {
                  upgradePath:"/ws",
                  serviceName:"wsService"
              }
}
service<http> httpService {

    @http:resourceConfig {
        path:"/world",
        methods:["POST","GET","PUT","My"]
    }
    resource testResource(http:Request req, http:Response resp) {
        string payload = req.getStringPayload();
        println(payload);
        resp.setStringPayload("I received");
        resp.send();
    }
}

service<ws> wsService  {

    resource onOpen(ws:Connection conn) {
        println("New WebSocket connection: " + conn.getID());
    }

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        println(frame.text);
        conn.pushText(frame.text);
    }
}