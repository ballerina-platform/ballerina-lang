import ballerina/http;
import ballerina/log;

final string ASSOCIATED_CONNECTION = "ASSOCIATED_CONNECTION";

// The URL of the remote backend.
final string REMOTE_BACKEND = "ws://localhost:9095/retry/ws";

@http:WebSocketServiceConfig {
    path: "/retry/ws"
}
service RetryProxyService on new http:Listener(9090) {

    // This resource gets invoked when a new client connects.
    // Since messages to the server are not read by the service until the execution of the `onOpen` resource finishes,
    // operations, which should happen before reading messages should be done in the `onOpen` resource.
    resource function onOpen(http:WebSocketCaller caller) {

        // Defines the webSocket client endpoint.
        http:WebSocketClient wsClientEp = new(
            REMOTE_BACKEND,
            {callbackService: RetryClientService,
            // When creating client endpoint, if `readyOnConnect` flag is set to
            // `false`, client endpoint does not start reading frames automatically.
            readyOnConnect: false,
            // Retry configuration options.
            retryConfig: {
                // The number of milliseconds to delay before attempting to reconnect.
                intervalInMillis: 3000,
                // The maximum number of retry attempts.
                // If the count is zero, the client will retry indefinitely.
                maxCount: 20,
                // The rate of increase of the reconnect delay.
                backOffFactor: 2.0,
                // Upper limit of the retry interval in milliseconds. If
                // `intervalInMillis` into `backOffFactor` value exceeded
                // `maxWaitIntervalInMillis` interval value, then
                // `maxWaitIntervalInMillis` will be considered as the retry interval.
                maxWaitIntervalInMillis: 20000
            }
        });

        //Associate connections before starting to read messages.
        wsClientEp.setAttribute(ASSOCIATED_CONNECTION, caller);
        caller.setAttribute(ASSOCIATED_CONNECTION, wsClientEp);

        // Once the client is ready to receive frames, the remote function `ready`
        // of the client needs to be called separately.
        var err = wsClientEp->ready();
        if (err is http:WebSocketError) {
            log:printError("Error calling ready on client", err);
        }
    }

    //This resource gets invoked upon receiving a new text frame from a client.
    resource function onText(http:WebSocketCaller caller, string text,
    boolean finalFrame) {

        http:WebSocketClient clientEp =
        getAssociatedClientEndpoint(caller);
        var err = clientEp->pushText(text, finalFrame);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when sending text message", err);
        }
    }

    //This resource gets invoked when an error occurs in the connection.
    resource function onError(http:WebSocketCaller caller, error err) {

        http:WebSocketClient clientEp =
        getAssociatedClientEndpoint(caller);
        var e = clientEp->close(statusCode = 1011, reason = "Unexpected condition");
        if (e is http:WebSocketError) {
            log:printError("Error occurred when closing the connection", e);
        }
        _ = caller.removeAttribute(ASSOCIATED_CONNECTION);
        log:printError("Unexpected error hence closing the connection", err);
    }

    //This resource gets invoked when a client connection is closed from the client side.
    resource function onClose(http:WebSocketCaller caller, int statusCode,
    string reason) {

        http:WebSocketClient clientEp = getAssociatedClientEndpoint(caller);
        var err = clientEp->close(statusCode = statusCode, reason = reason);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when closing the connection", err);
        }
        _ = caller.removeAttribute(ASSOCIATED_CONNECTION);
    }
}

//Client service to receive frames from the remote server.
service RetryClientService = @http:WebSocketServiceConfig {} service {

    //This resource gets invoked upon receiving a new text frame from the remote backend.
    resource function onText(http:WebSocketClient caller, string text, boolean finalFrame) {

        http:WebSocketCaller serverEp = getAssociatedServerEndpoint(caller);
        var err = serverEp->pushText(text, finalFrame);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when sending text message", err);
        }
    }

    //This resource gets invoked when an error occurs in the connection.
    resource function onError(http:WebSocketClient caller, error err) {

        http:WebSocketCaller serverEp = getAssociatedServerEndpoint(caller);
        var e = serverEp->close(statusCode = 1011, reason = "Unexpected condition");
        if (e is http:WebSocketError) {
            log:printError("Error occurred when closing the connection",
            err = e);
        }
        _ = caller.removeAttribute(ASSOCIATED_CONNECTION);
        log:printError("Unexpected error hense closing the connection", err);
    }

    //This resource gets invoked when a client connection is closed by the remote backend.
    resource function onClose(http:WebSocketClient caller, int statusCode,
    string reason) {

        http:WebSocketCaller serverEp = getAssociatedServerEndpoint(caller);
        var err = serverEp->close(statusCode = statusCode, reason = reason);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when closing the connection", err);
        }
        _ = caller.removeAttribute(ASSOCIATED_CONNECTION);
    }
};

// Function to retrieve the associated client of a particular caller.
function getAssociatedClientEndpoint(http:WebSocketCaller ep) returns (http:WebSocketClient) {
    http:WebSocketClient wsClient = <http:WebSocketClient>ep.getAttribute(ASSOCIATED_CONNECTION);
    return wsClient;
}

// Function to retrieve the associated caller of a client.
function getAssociatedServerEndpoint(http:WebSocketClient ep) returns (http:WebSocketCaller) {
    http:WebSocketCaller wsEndpoint = <http:WebSocketCaller>ep.getAttribute(ASSOCIATED_CONNECTION);
    return wsEndpoint;
}
