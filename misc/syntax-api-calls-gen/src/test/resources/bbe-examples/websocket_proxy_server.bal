import ballerina/http;
import ballerina/log;
import ballerina/websocket;

// The Url of the remote backend.
final string REMOTE_BACKEND = "ws://echo.websocket.org";

service /proxy/ws on new websocket:Listener(9090) {
    resource function get .(http:Request req) returns
                         websocket:Service|websocket:UpgradeError {
        return service object websocket:Service {
            websocket:AsyncClient? targetEp = ();
            // This resource gets invoked when a new client connects.
            // Since messages to the server are not read by the service until
            // the execution of the `onOpen` resource finishes, operations
            // which should happen before reading messages should be done
            // in the `onOpen` resource.
            remote function onOpen(websocket:Caller caller) returns
                            websocket:Error? {
                websocket:AsyncClient wsClientEp = check new (
                    REMOTE_BACKEND, new ClientService(caller));
                self.targetEp = wsClientEp;
            }

            //This resource gets invoked upon receiving a new text message
            // from a client.
            remote function onTextMessage(websocket:Caller caller,
                                string text) {
                websocket:AsyncClient clientEp =
                        <websocket:AsyncClient>self.targetEp;
                var err = clientEp->writeTextMessage(text);
                if (err is websocket:Error) {
                    log:printError("Error occurred when sending text message",
                             err = err);
                }
            }

            //This resource gets invoked upon receiving a new binary
            // message from a client.
            remote function onBinaryMessage(websocket:Caller caller,
                                byte[] data) {
                websocket:AsyncClient clientEp =
                        <websocket:AsyncClient>self.targetEp;
                var err = clientEp->writeBinaryMessage(data);
                if (err is websocket:Error) {
                    log:printError(
                        "Error occurred when sending binary message",
                        err = err);
                }
            }

            //This resource gets invoked when an error occurs
            // in the connection.
            remote function onError(websocket:Caller caller, error err) {
                websocket:AsyncClient clientEp =
                         <websocket:AsyncClient>self.targetEp;
                var e = clientEp->close(statusCode = 1011,
                                reason = "Unexpected condition");
                if (e is websocket:Error) {
                    log:printError(
                        "Error occurred when closing the connection",
                        err = e);
                }
                log:printError(
                    "Unexpected error hence closing the connection",
                    err = err);
            }

            //This resource gets invoked when a client connection is closed
            // from the client side.
            remote function onClose(websocket:Caller caller, int statusCode,
                                        string reason) {
                websocket:AsyncClient clientEp =
                         <websocket:AsyncClient>self.targetEp;
                var err = clientEp->close(statusCode = statusCode,
                           reason = reason);
                if (err is websocket:Error) {
                    log:printError(
                        "Error occurred when closing the connection",
                        err = err);
                }
            }
        };
    }
}

//Client service to receive messages from the remote server.
service class ClientService {
    *websocket:Service;
    websocket:Caller sourceEp;
    public function init(websocket:Caller sourceEp) {
        self.sourceEp = sourceEp;
    }
    //This resource gets invoked upon receiving a new text messages from
    // the remote backend.
    remote function onTextMessage(websocket:Caller caller, string text) {
        var err = self.sourceEp->writeTextMessage(text);
        if (err is websocket:Error) {
            log:printError("Error occurred when sending text message",
                err = err);
        }
    }

    //This resource gets invoked upon receiving a new binary messages from
    // the remote backend.
    remote function onBinaryMessage(websocket:Caller caller, byte[] data) {
        var err = self.sourceEp->writeBinaryMessage(data);
        if (err is websocket:Error) {
            log:printError("Error occurred when sending binary message",
                err = err);
        }
    }

    //This resource gets invoked when an error occurs in the connection.
    remote function onError(websocket:Caller caller, error err) {
        var e = self.sourceEp->close(statusCode = 1011,
                        reason = "Unexpected condition");
        if (e is websocket:Error) {
            log:printError("Error occurred when closing the connection",
                            err = e);
        }
        log:printError("Unexpected error hense closing the connection",
            err = err);
    }

    //This resource gets invoked when a client connection is closed by
    // the remote backend.
    remote function onClose(websocket:Caller caller, int statusCode,
                                string reason) {
        var err = self.sourceEp->close(statusCode = statusCode,
                         reason = reason);
        if (err is websocket:Error) {
            log:printError("Error occurred when closing the connection",
                err = err);
        }
    }
}
