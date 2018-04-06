package ballerina.http;

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

@Description {value:"Represent the details needed before the Handshake is done."}
@Field {value: "connectionID: ID of the connection"}
@Field {value: "isSecure: True if the connection is secured"}
@Field {value: "upgradeHeaders: Received headers in the connection upgrade"}
public struct HandshakeConnection {
    string connectionID;
    boolean isSecure;
    map<string> upgradeHeaders;
}

@Description {value:"Cancels the handshake"}
@Param {value:"conn: A HandshakeConnection struct"}
@Param {value:"statusCode: Status code for closing the connection"}
@Param {value:"reason: Reason for closing the connection"}
public native function <HandshakeConnection conn> cancelHandshake(int statusCode, string reason);

@Description {value:"Represents a WebSocket connection in ballerina. This include all connection oriented operations."}
@Field {value: "attributes: Custom user attributes"}
public struct Connection {
    map attributes;
}

@Description {value:"Gets the ID of the WebSocket connection"}
@Param {value:"conn: A Connection struct"}
@Return {value:"ID of the connection"}
public native function <Connection conn> getID() (string);

@Description {value:"Gets the negotiated sub protocol of the connection"}
@Param {value:"conn: A Connection struct"}
@Return {value:"Negotiated sub protocol"}
public native function <Connection conn> getNegotiatedSubProtocol() (string);

@Description {value:"Checks whether the connection is secure or not"}
@Param {value:"conn: A Connection struct"}
@Return {value:"True if the connection is secure"}
public native function <Connection conn> isSecure() (boolean);

@Description {value:"Checks whether the connection is still open or not."}
@Param {value:"conn: A Connection struct"}
@Return {value:"True if the connection is open"}
public native function <Connection conn> isOpen() (boolean);

@Description {value:"Gets a map of all the upgrade headers of the connection"}
@Param {value:"conn: A Connection struct"}
@Return {value:"Map of all the headers received in the connection upgrade"}
public native function <Connection conn> getUpgradeHeaders() (map);

@Description {value:"Gets a value of a header"}
@Param {value:"conn: A Connection struct"}
@Param {value:"key: Key of the header for which the value should be retrieved"}
@Return {value:"Value of the header if it exists, else it is null"}
public native function <Connection conn> getUpgradeHeader(string key) (string);

@Description {value:"Gets the parent connection if there is one"}
@Param {value:"conn: Connection for which the parent connection should be retrieved"}
@Return {value:"The parent connection if it exists, else it is null"}
public native function <Connection conn> getParentConnection() (Connection);

@Description {value:"Push text to the connection"}
@Param {value:"conn: A Connection struct"}
@Param {value:"text: Text to be sent"}
public native function <Connection conn> pushText(string text);

@Description {value:"Push binary data to the connection"}
@Param {value:"conn: A Connection struct"}
@Param {value:"data: Binary data to be sent"}
public native function <Connection conn> pushBinary(blob data);

@Description {value:"Ping the connection"}
@Param {value:"conn: A Connection struct"}
@Param {value:"data: Binary data to be sent"}
public native function <Connection conn> ping(blob data);

@Description {value:"Send pong message to the connection"}
@Param {value:"conn: A Connection struct"}
@Param {value:"data: Binary data to be sent"}
public native function <Connection conn> pong(blob data);

@Description {value:"Close the connection"}
@Param {value:"conn: A Connection struct"}
@Param {value:"statusCode: Status code for closing the connection"}
@Param {value:"reason: Reason for closing the connection"}
public native function <Connection conn> closeConnection(int statusCode, string reason);


@Description {value:"Configuration struct for WebSocket client connection"}
@Field {value: "subProtocols: Negotiable sub protocols for the client"}
@Field {value: "parentConnectionID: Connection ID of the parent connection to which it should be bound to when connecting"}
@Field {value: "customHeaders: Custom headers which should be sent to the server"}
@Field {value: "idleTimeoutInSeconds: Idle timeout of the client. Upon timeout, onIdleTimeout resource in the client service will be triggered (if there is one defined)."}
public struct ClientConnectorConfig {
    string [] subProtocols;
    string parentConnectionID;
    map<string> customHeaders;
    int idleTimeoutInSeconds = -1;
}

@Description {value: "Error struct for WebSocket connection errors"}
@Field {value:"msg:  An error message explaining the error"}
@Field {value:"cause: The error that caused HttpConnectorError to be returned"}
@Field {value:"stackTrace: Represents the invocation stack if WsConnectorError is thrown"}
public struct WsConnectorError {
    string msg;
    error[] cause;
    StackFrame[] stackTrace;
}

@Description {value:"WebSocket client connector for connecting to a WebSocket backend"}
@Param {value:"url: WebSocket URL for the backend"}
@Param {value:"callbackService: Callback service to listen to the incoming messages from the backend"}
public connector WsClient(string url, string callbackService) {

    @Description {value:"Connect to remote endpoint"}
    @Param {value:"config: ClientConnectorConfig for the connection"}
    @Return {value:"New WebSocket connection for the connected backend"}
    native action connect(ClientConnectorConfig config) (Connection, WsConnectorError);

    @Description {value:"Connect to remote endpoint with default configuration"}
    @Return {value:"New WebSocket connection for the connected backend"}
    native action connectWithDefault() (Connection, WsConnectorError);
}