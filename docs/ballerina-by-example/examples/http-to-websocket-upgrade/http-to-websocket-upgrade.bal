import ballerina/io;
import ballerina/net.http;
import ballerina/mime;

endpoint http:ServiceEndpoint servicEp {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello",
    webSocketUpgrade:{
                         upgradePath:"/ws",
                         upgradeService:typeof wsService
                     }
}
service<http:Service> httpService bind servicEp {

    @http:ResourceConfig {
        path:"/world",
        methods:["POST", "GET", "PUT", "My"]
    }
    httpResource (endpoint conn, http:Request req) {
        http:Response resp = {};
        var payload = req.getStringPayload();
        match payload {
            string val => {
                io:println(payload);
                resp.setStringPayload("I received");
            }
            mime:EntityError payloadError => {
                io:println(payloadError.message);
                resp.setStringPayload(payloadError.message);
                resp.statusCode = 500;
            }
            any => {
                io:println(payload);
                resp.setStringPayload("I received");
            }
        }

        _ = conn -> respond(resp);
    }
}

@Description {value:"Note: When a WebSocket upgrade path is defined in HTTP configuration in WebSocket configuration there can be \n - Full service configuration: There will be two base paths for the same service from either HTTP or WebSocket \n - Without service configuration: WebSocket service will be a slave service of HTTP service. Then only the upgrade path can be there. \n - Configuration without basePath: It will act as a slave service but can configure sub protocols, idle timeout etc..."}
@http:WebSocketServiceConfig {
    subProtocols:["xml, json"],
    idleTimeoutInSeconds:5
}
service<http:WebSocketService> wsService {

    onOpen (endpoint ep) {
        var conn = ep.getClient();
        io:println("New WebSocket connection: " + conn.id);
    }

    onTextMessage (endpoint ep, http:TextFrame frame) {
        io:println(frame.text);
        ep -> pushText(frame.text);
    }

    onIdleTimeout (endpoint ep) {
        var conn = ep.getClient();
        io:println("Idle timeout: " + conn.id);
    }
}