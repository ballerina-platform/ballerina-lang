import ballerina/io;
import ballerina/http;
import ballerina/mime;
import ballerina/log;

endpoint http:Listener servicEp {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> httpService bind servicEp {

    @http:ResourceConfig {
        path:"/world",
        methods:["POST", "GET", "PUT", "My"]
    }
    httpResource(endpoint conn, http:Request req) {
        http:Response resp = new;
        var payload = req.getStringPayload();
        match payload {
            http:PayloadError payloadError => {
                io:println(payloadError.message);
                resp.setStringPayload(payloadError.message);
                resp.statusCode = 500;
            }
            string val => {
                io:println(payload);
                resp.setStringPayload("I received");
            }
        }

        conn -> respond(resp) but { error e => log:printError("Error sending message", err=e) };
    }


    @http:ResourceConfig {
        webSocketUpgrade:{
            upgradePath:"/ws",
            upgradeService:wsService
        }
    }
    upgrader(endpoint ep, http:Request req) {
    }
}


//Note: When a WebSocket upgrade path is defined in HTTP resource configuration
//- Without service configuration for WebSocketService default values are taken for sub protocols, idle timeout etc...
//- If  WebSocketServiceConfig is defined without the path, sub protocols, idle timeout etc... can be configured
//- If path is defined in the WebSocketServiceConfig it shall be ignored
//- This service can also be bound to a different endpoint in which case the path configuration will become useful
@http:WebSocketServiceConfig {
    subProtocols:["xml, json"],
    idleTimeoutInSeconds:20
}
service<http:WebSocketService> wsService {

    onOpen(endpoint ep) {
        io:println("New WebSocket connection: " + ep.id);
    }

    onText(endpoint ep, string text) {
        io:println(text);
        ep -> pushText(text) but { error e => log:printError("Error sending message", err=e) };
    }

    onIdleTimeout(endpoint ep) {
        io:println("Idle timeout: " + ep.id);
    }
}
