import ballerina/io;
import ballerina/http;

@final string REMOTE_BACKEND_URL = "ws://localhost:15500/websocket";

endpoint http:Listener ep {
    port:9090
};

service<http:Service> proxy bind ep {

    @http:ResourceConfig {
        webSocketUpgrade:{
            upgradePath:"/ws"
        }
    }
    websocketProxy(endpoint httpEp, http:Request req) {
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/hello"
    }
    sayHello(endpoint conn, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Successful");
        _ = conn -> respond(res);
    }


    sayHi(endpoint conn, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Successful");
        _ = conn -> respond(res);
    }
}