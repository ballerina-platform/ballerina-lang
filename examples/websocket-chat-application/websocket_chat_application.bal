import ballerina/http;
import ballerina/log;

@final string NAME = "NAME";
@final string AGE = "AGE";

@http:ServiceConfig {
    basePath: "/chat"
}
service<http:Service> chatAppUpgrader bind { port: 9090 } {

    // Upgrade from HTTP to WebSocket and define the service the WebSocket client needs to connect to.
    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/{name}",
            upgradeService: chatApp
        }
    }
    upgrader(endpoint caller, http:Request req, string name) {
        endpoint http:WebSocketListener wsEp;
        map<string> queryParams = req.getQueryParams();
        // Cancel handshake by sending a 400 status code if the age parameter is missing in the request.
        if (!queryParams.hasKey("age")){
            var err = caller->cancelWebSocketUpgrade(400, "Age is required");
            if (err is error) {
                log:printError("Error cancelling handshake", err = err);
            }
            done;
        }
        map<string> headers;
        wsEp = caller->acceptWebSocketUpgrade(headers);
        wsEp.attributes[NAME] = name;
        wsEp.attributes[AGE] = queryParams["age"];
        string msg =
            "Hi " + name + "! You have succesfully connected to the chat";
        var err = wsEp->pushText(msg);
        if (err is error) {
            log:printError("Error sending message", err = err);
        }
    }
}

// Stores the connection IDs of users who join the chat.
map<http:WebSocketListener> connectionsMap;

service<http:WebSocketService> chatApp {

    // Store the attributes of the user, such as username and age, once the user connects to the chat client, and
    // broadcast that the user has joined the chat.
    onOpen(endpoint caller) {
        string msg;
        msg = getAttributeStr(caller, NAME) + " with age "
                    + getAttributeStr(caller, AGE) + " connected to chat";
        broadcast(msg);
        connectionsMap[caller.id] = caller;
    }

    // Broadcast the messages sent by a user.
    onText(endpoint caller, string text) {
        string msg = getAttributeStr(caller, NAME) + ": " + text;
        log:printInfo(msg);
        broadcast(msg);
    }

    // Broadcast that a user has left the chat once a user leaves the chat.
    onClose(endpoint caller, int statusCode, string reason) {
        _ = connectionsMap.remove(caller.id);
        string msg = getAttributeStr(caller, NAME) + " left the chat";
        broadcast(msg);
    }
}

function broadcast(string text) {
    endpoint http:WebSocketListener ep;
    foreach id, con in connectionsMap {
        ep = con;
        var err = ep->pushText(text);
        if (err is error) {
            log:printError("Error sending message", err = err);
        }
    }
}

function getAttributeStr(http:WebSocketListener ep, string key)
             returns (string) {
    var name = <string>ep.attributes[key];
    return name;
}
