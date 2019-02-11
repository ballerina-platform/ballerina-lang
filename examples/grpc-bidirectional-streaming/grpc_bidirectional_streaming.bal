// This is the server implementation for the bidirectional streaming scenario.
import ballerina/grpc;
import ballerina/log;

@grpc:ServiceConfig {
    name: "chat",
    clientStreaming: true,
    serverStreaming: true
}
service Chat on new grpc:Listener(9090) {
    map<grpc:Caller> consMap = {};

    //This `resource` is triggered when a new caller connection is initialized.
    resource function onOpen(grpc:Caller caller) {
        log:printInfo(string `{{caller.getId()}} connected to chat`);
        self.consMap[<string>caller.getId()] = caller;
    }

    //This `resource` is triggered when the caller sends a request message to the `service`.
    resource function onMessage(grpc:Caller caller, ChatMessage chatMsg) {
        grpc:Caller ep;
        string msg = string `{{chatMsg.name}}: {{chatMsg.message}}`;
        log:printInfo("Server received message: " + msg);
        foreach var con in self.consMap {
            string callerId;
            (callerId, ep) = con;
            error? err = ep->send(msg);
            if (err is error) {
                log:printError("Error from Connector: " + err.reason() + " - "
                            + <string>err.detail().message);
            } else {
                log:printInfo("Server message to caller " + callerId + " sent successfully.");
            }
        }
    }

    //This `resource` is triggered when the server receives an error message from the caller.
    resource function onError(grpc:Caller caller, error err) {
        log:printError("Error from Connector: " + err.reason() + " - "
                + <string>err.detail().message);
    }

    //This `resource` is triggered when the caller sends a notification to the server to indicate that it has finished sending messages.
    resource function onComplete(grpc:Caller caller) {
        grpc:Caller ep;
        string msg = string `{{caller.getId()}} left the chat`;
        log:printInfo(msg);
        var v = self.consMap.remove(<string>caller.getId());
        foreach var con in self.consMap {
            string callerId;
            (callerId, ep) = con;
            error? err = ep->send(msg);
            if (err is error) {
                log:printError("Error from Connector: " + err.reason() + " - "
                        + <string>err.detail().message);
            } else {
                log:printInfo("Server message to caller " + callerId + " sent successfully.");
            }
        }
    }
}
