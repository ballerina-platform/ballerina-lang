import ballerina.io;
import ballerina.net.http;
import ballerina.net.ws;

@http:configuration {
    basePath:"/hello",
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
    resource testResource(http:Connection conn, http:InRequest req) {
        http:OutResponse resp = {};
        string payload = req.getStringPayload();
        io:println(payload);
        resp.setStringPayload("I received");
        _ = conn.respond(resp);
    }
}

@Description{value:"Note: When a WebSocket upgrade path is defined in HTTP configuration in WebSocket configuration there can be \n - Full service configuration: There will be two base paths for the same service from either HTTP or WebSocket \n - Without service configuration: WebSocket service will be a slave service of HTTP service. Then only the upgrade path can be there. \n - Configuration without basePath: It will act as a slave service but can configure sub protocols, idle timeout etc..."}
@ws:configuration {
    basePath:"world/ws",
    subProtocols:["xml, json"],
    idleTimeoutInSeconds:5
}
service<ws> wsService  {

    resource onOpen(ws:Connection conn) {
        io:println("New WebSocket connection: " + conn.getID());
    }

    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        io:println(frame.text);
        conn.pushText(frame.text);
    }

    resource onIdleTimeout(ws:Connection conn) {
        io:println("Idle timeout: " + conn.getID());
    }
}