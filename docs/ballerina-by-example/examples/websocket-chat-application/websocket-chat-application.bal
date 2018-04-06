import ballerina/io;
import ballerina/http;

@final string NAME = "NAME";
@final string AGE = "AGE";
endpoint http:WebSocketEndpoint ep {
    port:9090
};


@http:ServiceConfig {
    basePath: "/chat"
}
service<http:Service> ChatAppUpgrader bind ep {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/{name}",
            upgradeService: typeof chatApp
        }
    }
    upgrader(endpoint ep, http:Request req, string name) {
        endpoint http:WebSocketEndpoint wsEp;
        map<string> headers;
        wsEp = ep -> upgradeToWebSocket(headers);
        wsEp.attributes[NAME] = name;
        wsEp.attributes[AGE] = req.getQueryParams()["age"];
    }

}

// TODO: This map should go to service level after null pointer issue is fixed.
map<http:WebSocketEndpoint> consMap;

service<http:WebSocketService> chatApp {


    onOpen (endpoint conn) {
        string msg = string `{{getAttributeStr(conn, NAME)}} with age {{getAttributeStr(conn, AGE)}} connected to chat`;
        broadcast(consMap, msg);
        consMap[conn.id] = conn;
    }

    onText (endpoint conn, string text) {
        string msg = string `{{getAttributeStr(conn, NAME)}}: {{text}}`;
        io:println(msg);
        broadcast(consMap, msg);
    }

    onClose (endpoint conn, int statusCode, string reason) {
        _ = consMap.remove(conn.id);
        string msg = string `{{getAttributeStr(conn, NAME)}} left the chat`;
        broadcast(consMap, msg);
    }
}

function broadcast (map<http:WebSocketEndpoint> consMap, string text) {
    endpoint http:WebSocketEndpoint ep;
    foreach id, con in consMap {
        ep = con;
        _ = ep -> pushText(text);
    }
}

function getAttributeStr(http:WebSocketEndpoint ep, string key) returns (string) {
    var name = <string> ep.attributes[key];
    return name;
}
