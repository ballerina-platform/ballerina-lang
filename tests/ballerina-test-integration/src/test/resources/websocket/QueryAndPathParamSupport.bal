import ballerina/io;
import ballerina/http;

@final string PATH1 = "PATH1";
@final string PATH2 = "PATH2";
@final string QUERY1 = "QUERY1";
@final string QUERY2 = "QUERY2";

endpoint http:Listener ep {
    port:9090
};

service <http:Service> simple bind ep {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/{path1}/{path2}",
            upgradeService: simpleProxy
        }
    }
    websocketProxy (endpoint httpEp, http:Request req, string path1, string path2) {
        endpoint http:WebSocketListener wsServiceEp;
        wsServiceEp = httpEp -> acceptWebSocketUpgrade({"some-header":"some-header-value"});
        wsServiceEp.attributes[PATH1] = path1;
        wsServiceEp.attributes[PATH2] = path2;
        wsServiceEp.attributes[QUERY1] = req.getQueryParams()["q1"];
        wsServiceEp.attributes[QUERY2] = req.getQueryParams()["q2"];
    }
}

service <http:WebSocketService> simpleProxy {

    onOpen(endpoint wsEp) {
        string path1 = <string> wsEp.attributes[PATH1];
        string path2 = <string> wsEp.attributes[PATH2];
        string query1 = <string> wsEp.attributes[QUERY1];
        string query2 = <string> wsEp.attributes[QUERY2];

        string msg = string `path-params: {{path1}}, {{path2}}; query-params: {{query1}}, {{query2}}`;
        wsEp -> pushText(msg) but {error e => io:println("Error sending message. " + e.message)};
    }
}
