package ballerina.net.http;


@Description {value:"Represents a WebSocket text frame in Ballerina."}
@Field {value: "text: Text in the text frame"}
@Field {value: "isFinalFragment: Check whether this is the final frame. True if the frame is final frame."}
public struct TextFrame {
    string text;
    boolean isFinalFragment;
}

@Description {value:"Represents a WebSocket binary frame in Ballerina."}
@Field {value: "data: Binary data of the frame"}
@Field {value: "isFinalFragment: Check whether this is the final frame. True if the frame is final frame."}
public struct BinaryFrame {
    blob data;
    boolean isFinalFragment;
}


@Description {value:"Represents a WebSocket close frame in Ballerina."}
@Field {value: "statusCode: Status code for the reason of the closure of the connection"}
@Field {value: "reason: Reason to close the connection"}
public struct CloseFrame {
    int statusCode;
    string reason;
}

@Description {value:"Represents a WebSocket ping frame in Ballerina."}
@Field {value: "data: Data of the frame"}
public struct PingFrame {
    blob data;
}

@Description {value:"Represents a WebSocket pong frame in Ballerina."}
@Field {value: "data: Data of the frame"}
public struct PongFrame {
    blob data;
}

@Description {value: "Error struct for WebSocket connection errors"}
@Field {value:"message:  An error message explaining the error"}
@Field {value:"cause: The error that caused HttpConnectorError to be returned"}
public struct WebSocketConnectorError {
    string message;
    error cause;
}