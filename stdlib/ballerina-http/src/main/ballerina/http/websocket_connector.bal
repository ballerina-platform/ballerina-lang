
///////////////////////////
/// WebSocket Connector ///
///////////////////////////

@Description {value:"Represents a WebSocket connector in ballerina. This include all connector oriented operations."}
public type WebSocketConnector object {
    private {
        boolean isReady = false;
    }

    @Description {value:"Push text to the connection"}
    @Param {value:"text: Text to be sent"}
    public native function pushText(string text) returns WebSocketConnectorError|();

    @Description {value:"Push binary data to the connection"}
    @Param {value:"data: Binary data to be sent"}
    public native function pushBinary(blob data) returns WebSocketConnectorError|();

    @Description {value:"Ping the connection"}
    @Param {value:"data: Binary data to be sent"}
    public native function ping(blob data) returns WebSocketConnectorError|();

    @Description {value:"Send pong message to the connection"}
    @Param {value:"data: Binary data to be sent"}
    public native function pong(blob data) returns WebSocketConnectorError|();

    @Description {value:"Close the connection"}
    @Param {value:"statusCode: Status code for closing the connection"}
    @Param {value:"reason: Reason for closing the connection"}
    public native function close(int statusCode, string reason) returns WebSocketConnectorError|();

    public native function ready() returns WebSocketConnectorError|();

};

@Description {value:"WebSocketConnectorError struct represents an error occured during WebSocket message transfers"}
public type WebSocketConnectorError object{
    public {
        // An error message explaining about the error
        string message;
        //The error(s) that caused HttpConnectorError to get thrown
        error? cause;
    }
};
