import ballerina/http;
import ballerina/log;

@final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";
@final string REMOTE_BACKEND = "wss://echo.websocket.org";

@http:WebSocketServiceConfig {
    path: "/proxy/ws"
}
service<http:WebSocketService> SimpleProxyService bind { port: 9090 } {

    //This resource triggered when a new client is connected.
    //Since messages from server side are not read by service until `onOpen` resource exeucution finishes,
    //operations which should happen before reading messages should be done in `onOpen` resource.
    onOpen(endpoint caller) {

        endpoint http:WebSocketClient wsClientEp {
            url: REMOTE_BACKEND,
            callbackService: ClientService,
            readyOnConnect: false
        };
        //Associate connections before reading messages from both sides
        wsClientEp.attributes[ASSOCIATED_CONNECTION] = caller;
        caller.attributes[ASSOCIATED_CONNECTION] = wsClientEp;
        //When creating client endpoint, if `readyOnClient` flag is set to false client endpoint does not start reading frames automatically.
        //So `ready` action of endpoint should be called separately when ready to accept messsages.
        wsClientEp->ready()
        but { error e => log:printError(
                             "Error calling ready on client", err = e) };
    }

    //This resource is triggered when a new text frame is received from a client.
    onText(endpoint caller, string text, boolean finalFrame) {

        endpoint http:WebSocketClient clientEp =
        getAssociatedClientEndpoint(caller);
        clientEp->pushText(text, final = finalFrame)
        but { error e => log:printError(
                             "Error occurred when sending text message", err = e) };
    }

    //This resource is triggered when a new binary frame is received from a client.
    onBinary(endpoint caller, blob data, boolean finalFrame) {

        endpoint http:WebSocketClient clientEp =
        getAssociatedClientEndpoint(caller);
        clientEp->pushBinary(data, final = finalFrame)
        but { error e => log:printError(
                             "Error occurred when sending binary message", err = e) };
    }

    //This resource is triggered when an error occurs in the connection.
    onError(endpoint caller, error err) {

        endpoint http:WebSocketClient clientEp =
        getAssociatedClientEndpoint(caller);
        clientEp->close(1011, "Unexpected condition")
        but { error e => log:printError(
                             "Error occurred when closing the connection", err = e) };
        _ = caller.attributes.remove(ASSOCIATED_CONNECTION);
        log:printError("Unexpected error hense closing the connection",
            err = err);
    }

    //This resource is triggered when a client connection is closed from the client side.
    onClose(endpoint caller, int statusCode, string reason) {

        endpoint http:WebSocketClient clientEp =
        getAssociatedClientEndpoint(caller);
        clientEp->close(statusCode, reason)
        but { error e => log:printError(
                             "Error occurred when closing the connection", err = e) };
        _ = caller.attributes.remove(ASSOCIATED_CONNECTION);
    }
}

//Client service to receive frames from the remote server.
service<http:WebSocketClientService> ClientService {

    //This resource is triggered when a new text frame is received from the remote backend.
    onText(endpoint caller, string text, boolean finalFrame) {

        endpoint http:WebSocketListener serverEp =
        getAssociatedServerEndpoint(caller);
        serverEp->pushText(text, final = finalFrame)
        but { error e => log:printError(
                             "Error occurred when sending text message", err = e) };
    }

    //This resource is triggered when a new binary frame is received from the remote backend.
    onBinary(endpoint caller, blob data, boolean finalFrame) {

        endpoint http:WebSocketListener serverEp =
        getAssociatedServerEndpoint(caller);
        serverEp->pushBinary(data, final = finalFrame)
        but { error e => log:printError(
                             "Error occurred when sending binary message", err = e) };
    }

    //This resource is triggered when an error occurs in the connection.
    onError(endpoint caller, error err) {

        endpoint http:WebSocketListener serverEp =
        getAssociatedServerEndpoint(caller);
        serverEp->close(1011, "Unexpected condition")
        but { error e => log:printError(
                             "Error occurred when closing the connection", err = e) };
        _ = caller.attributes.remove(ASSOCIATED_CONNECTION);
        log:printError("Unexpected error hense closing the connection",
            err = err);
    }

    //This resource is triggered when a client connection is closed by the remote backend.
    onClose(endpoint caller, int statusCode, string reason) {

        endpoint http:WebSocketListener serverEp =
        getAssociatedServerEndpoint(caller);
        serverEp->close(statusCode, reason)
        but { error e => log:printError(
                             "Error occurred when closing the connection", err = e) };
        _ = caller.attributes.remove(ASSOCIATED_CONNECTION);
    }
}

function getAssociatedClientEndpoint(http:WebSocketListener ep)
             returns (http:WebSocketClient) {
    http:WebSocketClient client =
    check <http:WebSocketClient>ep.attributes[ASSOCIATED_CONNECTION];
    return client;
}

function getAssociatedServerEndpoint(http:WebSocketClient ep)
             returns (http:WebSocketListener) {
    http:WebSocketListener wsEndpoint =
    check <http:WebSocketListener>ep.attributes[ASSOCIATED_CONNECTION];
    return wsEndpoint;
}
