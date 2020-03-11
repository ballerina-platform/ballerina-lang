import ballerina/http;
import ballerina/log;

final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";

@http:WebSocketServiceConfig {
    path: "/failover/ws"
}
service failoverProxyService on new http:Listener(9090) {

    // This resource gets invoked when a new client connects.
    // Since messages to the server are not read by the service until
    // the execution of the `onOpen` resource finishes,
    // operations, which should happen before reading messages should be done
    // in the `onOpen` resource.
    resource function onOpen(http:WebSocketCaller caller) {

        // Defines the webSocket failover client.
        http:WebSocketFailoverClient wsClientEp = new (
        {
            callbackService: FailoverClientService,
            // Defines a set of targets.
            targetUrls: [ "ws://localhost:9095/failover/ws", "ws://localhost:9096/failover/ws",
            "ws://localhost:9094/failover/ws"],
            // Failover interval in milliseconds.
            failoverIntervalInMillis: 2000,
            // When creating client endpoint, if `readyOnConnect` flag is set to
            // `false` client endpoint does not start reading frames automatically.
            readyOnConnect: false
        });

        // Associate connections before starting to read messages.
        wsClientEp.setAttribute(ASSOCIATED_CONNECTION, caller);
        caller.setAttribute(ASSOCIATED_CONNECTION, wsClientEp);

        // Once the client is ready to receive frames the remote function `ready`
        // of the client need to be called separately.
        var err = wsClientEp->ready();
        if (err is http:WebSocketError) {
            log:printError("Error calling ready on client", err);
        }
    }

    // This resource gets invoked upon receiving a new text frame from a client.
    resource function onText(http:WebSocketCaller caller, string text, boolean finalFrame) {

        http:WebSocketFailoverClient clientEp = getAssociatedClientEndpoint(caller);
        var err = clientEp->pushText(text, finalFrame);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when sending text message", err);
        }
    }

    // This resource gets invoked when an error occurs in the connection.
    resource function onError(http:WebSocketCaller caller, error err) {

        http:WebSocketFailoverClient clientEp = getAssociatedClientEndpoint(caller);
        var e = clientEp->close(statusCode = 1011, reason = "Unexpected condition");
        if (e is http:WebSocketError) {
            log:printError("Error occurred when closing the connection", e);
        }
        log:printError("Unexpected error hence closing the connection", err);
    }

    // This resource gets invoked when a client connection is closed from the client side.
    resource function onClose(http:WebSocketCaller caller, int statusCode,
                                 string reason) {

        http:WebSocketFailoverClient clientEp = getAssociatedClientEndpoint(caller);
        var err = clientEp->close(statusCode = statusCode, reason = reason);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when closing the connection", err);
        }
    }
}

// Client service to receive frames from the remote server.
service failoverClientService = @http:WebSocketServiceConfig {} service {

    // This resource gets invoked upon receiving a new text frame from the remote backend.
    resource function onText(http:WebSocketFailoverClient caller, string text,
                                boolean finalFrame) {

        http:WebSocketCaller serverEp = getAssociatedServerEndpoint(caller);
        var err = serverEp->pushText(text, finalFrame);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when sending text message", err);
        }
    }

    // This resource gets invoked when an error occurs in the connection.
    resource function onError(http:WebSocketFailoverClient caller, error err) {

        http:WebSocketCaller serverEp = getAssociatedServerEndpoint(caller);
        var e = serverEp->close(statusCode = 1011,
        reason = "Unexpected condition");
        if (e is http:WebSocketError) {
            log:printError("Error occurred when closing the connection",
            err = e);
        }
        log:printError("Unexpected error hense closing the connection", err);
    }

    // This resource gets invoked when a client connection is closed by the remote backend.
    resource function onClose(http:WebSocketFailoverClient caller, int statusCode,
                                 string reason) {

        http:WebSocketCaller serverEp = getAssociatedServerEndpoint(caller);
        var err = serverEp->close(statusCode = statusCode, reason = reason);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when closing the connection", err);
        }
    }
};

// Function to retrieve associated client for a particular caller.
function getAssociatedClientEndpoint(http:WebSocketCaller ep) returns (http:WebSocketFailoverClient) {
    http:WebSocketFailoverClient wsClient = <http:WebSocketFailoverClient>ep.
    getAttribute(ASSOCIATED_CONNECTION);
    return wsClient;
}

// Function to retrieve the associated caller for a client.
function getAssociatedServerEndpoint(http:WebSocketFailoverClient ep) returns (http:WebSocketCaller) {
    http:WebSocketCaller wsEndpoint =<http:WebSocketCaller>ep.
    getAttribute(ASSOCIATED_CONNECTION);
    return wsEndpoint;
}
