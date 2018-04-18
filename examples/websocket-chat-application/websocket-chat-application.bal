import ballerina/log;
import ballerina/http;

@final string NAME = "NAME";
@final string AGE = "AGE";
endpoint http:WebSocketListener ep {
    port:9090
};

@http:ServiceConfig {
    basePath: "/chat"
}
service<http:Service> ChatAppUpgrader bind ep {

    //Upgrade from HTTP to WebSocket and define the service the WebSocket client needs to connect to.
    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/{name}",
            upgradeService: chatApp
        }
    }
    upgrader(endpoint ep, http:Request req, string name) {
        endpoint http:WebSocketListener wsEp;
        map<string> headers;
        wsEp = ep -> acceptWebSocketUpgrade(headers);
        wsEp.attributes[NAME] = name;
        wsEp.attributes[AGE] = req.getQueryParams()["age"];
        string msg = "Hi " + name + "! You have succesfully connected to the chat";
        wsEp -> pushText(msg) but {error e => log:printErrorCause("Error sending message", e)};
    }

}

// TODO: This map should go to service level after null pointer issue is fixed.
map<http:WebSocketListener> consMap;

service<http:WebSocketService> chatApp {

    //Store the attributes of the user, such as username and age, once the user connects to the chat client, and
    //broadcast that the user has joined the chat.
    onOpen (endpoint conn) {
        string msg = string `{{getAttributeStr(conn, NAME)}} with age {{getAttributeStr(conn, AGE)}} connected to chat`;
        broadcast(consMap, msg);
        consMap[conn.id] = conn;
    }

    //Broadcast the messages sent by a user.
    onText (endpoint conn, string text) {
        string msg = string `{{getAttributeStr(conn, NAME)}}: {{text}}`;
        log:printInfo(msg);
        broadcast(consMap, msg);
    }

    //Broadcast that a user has left the chat once a user leaves the chat client.
    onClose (endpoint conn, int statusCode, string reason) {
        _ = consMap.remove(conn.id);
        string msg = string `{{getAttributeStr(conn, NAME)}} left the chat`;
        broadcast(consMap, msg);
    }
}

function broadcast (map<http:WebSocketListener> consMap, string text) {
    endpoint http:WebSocketListener ep;
    foreach id, con in consMap {
        ep = con;
        ep -> pushText(text) but {error e => log:printErrorCause("Error sending message", e)};
    }
}

function getAttributeStr(http:WebSocketListener ep, string key) returns (string) {
    var name = <string> ep.attributes[key];
    return name;
}
