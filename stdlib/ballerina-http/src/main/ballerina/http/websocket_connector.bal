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
public type WebSocketConnector object {

    @Description {value:"Push text to the connection"}
    @Param {value:"text: Text to be sent"}
    public native function pushText (string text) returns (WebSocketConnectorError|null);

    @Description {value:"Push binary data to the connection"}
    @Param {value:"data: Binary data to be sent"}
    public native function pushBinary (blob data) returns (WebSocketConnectorError|null);

    @Description {value:"Ping the connection"}
    @Param {value:"data: Binary data to be sent"}
    public native function ping (blob data);

    @Description {value:"Send pong message to the connection"}
    @Param {value:"data: Binary data to be sent"}
    public native function pong (blob data);

    @Description {value:"Close the connection"}
    @Param {value:"statusCode: Status code for closing the connection"}
    @Param {value:"reason: Reason for closing the connection"}
    public native function close (int statusCode, string reason) returns (WebSocketConnectorError|null);
}

@Description {value:"WebSocketConnectorError struct represents an error occured during WebSocket message transfers"}
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error(s) that caused HttpConnectorError to get thrown"}
@Field {value:"code: An error code that differenciates different errors"}
public type WebSocketConnectorError object{
    public {
        string message;
        error[] cause;
    }
}
