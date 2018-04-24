
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
    @Param {value:"final: True if this is a final frame of a long message"}
    public native function pushText(string text, boolean final = true) returns error|();

    @Description {value:"Push binary data to the connection"}
    @Param {value:"data: Binary data to be sent"}
    @Param {value:"final: True if this is a final frame of a long message"}
    public native function pushBinary(blob data, boolean final = true) returns error|();

    @Description {value:"Ping the connection"}
    @Param {value:"data: Binary data to be sent"}
    public native function ping(blob data) returns error|();

    @Description {value:"Send pong message to the connection"}
    @Param {value:"data: Binary data to be sent"}
    public native function pong(blob data) returns error|();

    @Description {value:"Close the connection"}
    @Param {value:"statusCode: Status code for closing the connection"}
    @Param {value:"reason: Reason for closing the connection"}
    public native function close(int statusCode, string reason) returns error|();

    public native function ready() returns error|();

};

