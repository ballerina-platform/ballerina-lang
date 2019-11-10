import ballerina/io;
import ballerina/http;

@http:WebSocketServiceConfig {
    path: "/websocket",
    subProtocols: ["xml", "json"],
    idleTimeoutInSeconds: 120
}
service basic on new http:Listener(21033) {
    int i = 0;
    resource function onOpen(http:WebSocketCaller caller) {
        if (i == 1) {
            runtime:sleep(360000);
        }
        i++;
    }
    resource function onText(http:WebSocketCaller caller, string text,
                                boolean finalFrame) {
        checkpanic caller->pushText(text);
    }
}
