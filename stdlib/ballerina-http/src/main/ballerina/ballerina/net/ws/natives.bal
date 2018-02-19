package ballerina.net.ws;

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

@Description {value:"Gets the query parameters from the HandshakeConnection as a map"}
@Param {value:"req: The HandshakeConnection struct" }
@Return {value:"The map of query params" }
documentation {
Gets the query parameters from the HandshakeConnection as a map.
- #conn The HandshakeConnection struct.
- #m The map of query params.
}
public native function <HandshakeConnection conn> getQueryParams () (map m);

@Description {value:"Cancels the handshake"}
@Param {value:"conn: A HandshakeConnection struct"}
@Param {value:"statusCode: Status code for closing the connection"}
@Param {value:"reason: Reason for closing the connection"}
documentation {
Cancels the handshake.
- #conn The HandshakeConnection struct.
- #statusCode Status code for closing the connection.
- #reason Reason for closing the connection.
}
public native function <HandshakeConnection conn> cancelHandshake(int statusCode, string reason);

@Description {value:"Represents a WebSocket connection in ballerina. This include all connection oriented operations."}
@Field {value: "attributes: Custom user attributes"}
public struct Connection {
    map attributes;
}

@Description {value:"Gets the ID of the WebSocket connection"}
@Param {value:"conn: A Connection struct"}
@Return {value:"ID of the connection"}
documentation {
Gets the ID of the WebSocket connection.
- #conn A Connection struct.
- #id ID of the connection.
}
public native function <Connection conn> getID() (string id);

@Description {value:"Gets the negotiated sub protocol of the connection"}
@Param {value:"conn: A Connection struct"}
@Return {value:"Negotiated sub protocol"}
documentation {
Gets the negotiated sub protocol of the connection.
- #conn A Connection struct.
- #id ID of the connection.
}
public native function <Connection conn> getNegotiatedSubProtocol() (string id);

@Description {value:"Checks whether the connection is secure or not"}
@Param {value:"conn: A Connection struct"}
@Return {value:"True if the connection is secure"}
documentation {
Checks whether the connection is secure or not.
- #conn A Connection struct.
- #isSecure True if the connection is secure.
}
public native function <Connection conn> isSecure() (boolean isSecure);

@Description {value:"Checks whether the connection is still open or not."}
@Param {value:"conn: A Connection struct"}
@Return {value:"True if the connection is open"}
documentation {
Checks whether the connection is still open or not.
- #conn A Connection struct.
- #isOpen True if the connection is open.
}
public native function <Connection conn> isOpen() (boolean isOpen);

@Description {value:"Gets a map of all the upgrade headers of the connection"}
@Param {value:"conn: A Connection struct"}
@Return {value:"Map of all the headers received in the connection upgrade"}
documentation {
Gets a map of all the upgrade headers of the connection.
- #conn A Connection struct.
- #m Map of all the headers received in the connection upgrade.
}
public native function <Connection conn> getUpgradeHeaders() (map m);

@Description {value:"Gets a value of a header"}
@Param {value:"conn: A Connection struct"}
@Param {value:"key: Key of the header for which the value should be retrieved"}
@Return {value:"Value of the header if it exists, else it is null"}
documentation {
Gets a value of a header.
- #conn A Connection struct.
- #key Key of the header for which the value should be retrieved.
- #value Value of the header if it exists, else it is null.
}
public native function <Connection conn> getUpgradeHeader(string key) (string value);

@Description {value:"Gets the parent connection if there is one"}
@Param {value:"conn: Connection for which the parent connection should be retrieved"}
@Return {value:"The parent connection if it exists, else it is null"}
documentation {
Gets the parent connection if there is one.
- #conn Connection for which the parent connection should be retrieved.
- #parentConn The parent connection if it exists, else it is null.
}
public native function <Connection conn> getParentConnection() (Connection parentConn);

@Description {value:"Push text to the connection"}
@Param {value:"conn: A Connection struct"}
@Param {value:"text: Text to be sent"}
documentation {
Push text to the connection.
- #conn A Connection struct.
- #text Text to be sent.
}
public native function <Connection conn> pushText(string text);

@Description {value:"Push binary data to the connection"}
@Param {value:"conn: A Connection struct"}
@Param {value:"data: Binary data to be sent"}
documentation {
Push binary data to the connection.
- #conn A Connection struct.
- #data Binary data to be sent.
}
public native function <Connection conn> pushBinary(blob data);

@Description {value:"Ping the connection"}
@Param {value:"conn: A Connection struct"}
@Param {value:"data: Binary data to be sent"}
documentation {
Ping the connection.
- #conn A Connection struct.
- #data Binary data to be sent.
}
public native function <Connection conn> ping(blob data);

@Description {value:"Send pong message to the connection"}
@Param {value:"conn: A Connection struct"}
@Param {value:"data: Binary data to be sent"}
documentation {
Send pong message to the connection.
- #conn A Connection struct.
- #data Binary data to be sent.
}
public native function <Connection conn> pong(blob data);

@Description {value:"Close the connection"}
@Param {value:"conn: A Connection struct"}
@Param {value:"statusCode: Status code for closing the connection"}
@Param {value:"reason: Reason for closing the connection"}
documentation {
Close the connection.
- #conn A Connection struct.
- #statusCode Status code for closing the connection.
- #reason Reason for closing the connection.
}
public native function <Connection conn> closeConnection(int statusCode, string reason);

@Description {value:"Gets the query parameters from the Connection as a map"}
@Param {value:"req: The Connection struct" }
@Return {value:"The map of query params" }
documentation {
Gets the query parameters from the Connection as a map.
- #conn The Connection struct.
- #m The map of query params.
}
public native function <Connection conn> getQueryParams () (map m);

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
    error cause;
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