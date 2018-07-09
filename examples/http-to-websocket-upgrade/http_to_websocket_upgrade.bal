import ballerina/io;
import ballerina/http;
import ballerina/mime;
import ballerina/log;

@http:ServiceConfig {
    basePath: "/hello"
}
service<http:Service> httpService bind { port: 9090 } {

    @http:ResourceConfig {
        path: "/world",
        methods: ["POST"]
    }
    httpResource(endpoint caller, http:Request req) {
        http:Response resp = new;
        var payload = req.getTextPayload();
        match payload {
            error err => {
                log:printError("Error sending message", err = err);
                resp.setPayload(untaint err.message);
                resp.statusCode = 500;
            }
            string val => {
                io:println(val);
                resp.setPayload(string `Http POST received: {{untaint val}}\n`);
            }
        }

        caller->respond(resp) but {
            error e => log:printError("Error in responding", err = e)
        };
    }


    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/ws",
            upgradeService: wsService
        }
    }
    upgrader(endpoint caller, http:Request req) {

    }
}


// Note: When a WebSocket upgrade path is defined in HTTP resource configuration.
// - Without service configuration for WebSocketService default values are taken for sub protocols, idle timeout etc...
// - If  WebSocketServiceConfig is defined without the path, sub protocols, idle timeout etc... can be configured.
// - If path is defined in the WebSocketServiceConfig it shall be ignored.
// - This service can also be bound to a different endpoint in which case the path configuration becomes useful.
@http:WebSocketServiceConfig {
    subProtocols: ["xml, json"],
    idleTimeoutInSeconds: 20
}
service<http:WebSocketService> wsService {

    onOpen(endpoint caller) {
        io:println("New WebSocket connection: " + caller.id);
    }

    onText(endpoint caller, string text) {
        io:println(text);
        caller->pushText(text) but {
            error e => log:printError("Error sending message", err = e)
        };
    }

    onIdleTimeout(endpoint caller) {
        io:println("Idle timeout: " + caller.id);
    }
}
