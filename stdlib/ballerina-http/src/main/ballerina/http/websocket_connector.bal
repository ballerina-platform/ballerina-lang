package ballerina.http;

///////////////////////////
/// WebSocket Connector ///
///////////////////////////

@Description {value:"Represents a WebSocket connector in ballerina. This include all connector oriented operations."}
@Field {value:"attributes: Custom user attributes"}
@Field {value:"id: The ID of the WebSocket connection"}
@Field {value:"negotiatedSubProtocol: The negotiated sub protocol of the connection"}
@Field {value:"isSecure: the connection is secure"}
@Field {value:"isOpen: whether the connection is open"}
@Field {value:"upgradeHeaders: a map of all the upgrade headers of the connection"}
public struct WebSocketConnector {
}

@Description {value:"Push text to the connection"}
@Param {value:"text: Text to be sent"}
public native function <WebSocketConnector wsConnector> pushText (string text) returns (WebSocketConnectorError|null);

@Description {value:"Push binary data to the connection"}
@Param {value:"data: Binary data to be sent"}
public native function <WebSocketConnector wsConnector> pushBinary (blob data) returns (WebSocketConnectorError|null);

@Description {value:"Ping the connection"}
@Param {value:"data: Binary data to be sent"}
public native function <WebSocketConnector wsConnector> ping (blob data);

@Description {value:"Send pong message to the connection"}
@Param {value:"data: Binary data to be sent"}
public native function <WebSocketConnector wsConnector> pong (blob data);

@Description {value:"Close the connection"}
@Param {value:"statusCode: Status code for closing the connection"}
@Param {value:"reason: Reason for closing the connection"}
public native function <WebSocketConnector wsConnector> close (int statusCode, string reason) returns (WebSocketConnectorError|null);

@Description {value:"Sends a upgrade request with custom headers"}
@Param {value:"headers: a map of custom headers for handshake."}
public native function <WebSocketConnector conn> upgradeToWebSocket (map headers) returns WebSocketEndpoint;

@Description {value:"Cancels the handshake"}
@Param {value:"statusCode: Status code for closing the connection"}
@Param {value:"reason: Reason for closing the connection"}
public native function <WebSocketConnector conn> cancelUpgradeToWebSocket (int status, string reason);

@Description {value:"WebSocketConnectorError struct represents an error occured during WebSocket message transfers"}
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error(s) that caused HttpConnectorError to get thrown"}
@Field {value:"code: An error code that differenciates different errors"}
public struct WebSocketConnectorError {
    string message;
    error[] cause;
}
