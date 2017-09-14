package ballerina.net.ws;

import ballerina.doc;

@doc:Description {value:"Represent WebSocket text frame in Ballerina"}
struct TextFrame {
    string text;
    boolean isFinalFragment;
}

@doc:Description {value:"Represent WebSocket binary frame in Ballerina"}
struct BinaryFrame {
    blob data;
    boolean isFinalFragment;
}

@doc:Description {value:"Represent WebSocket close frame in Ballerina"}
struct CloseFrame {
    int statusCode;
    string reason;
}

@doc:Description {value:"Represent WebSocket ping frame in Ballerina"}
struct PingFrame {
    blob data;
}

@doc:Description {value:"Represent WebSocket pong frame in Ballerina"}
struct PongFrame {
    blob data;
}

@doc:Description {value:"Represent the details needed before the Handshake is done"}
struct HandshakeConnection {
    string connectionID;
    boolean isSecure;
    map<string> upgradeHeaders;
}
@doc:Description {value:"Cancel the handshake"}
@doc:Param {value:"statusCode: Status code for closing the connection"}
@doc:Param {value:"reason: Reason for closing the connection"}
native function cancelHandshake(HandshakeConnection handshakeConn, int statusCode, string reason);

@doc:Description {value:"Represent WebSocket connection in ballerina. This include all connection oriented operations"}
struct Connection {
    map attributes;
}
@doc:Description {value:"Get the ID of the WebSocket connection"}
@doc:Return {value:"string: ID of the connection"}
native function getID(Connection conn) (string);

@doc:Description {value:"Get the Negotiated sub protocol for given connection"}
@doc:Return {value:"string: Negotiated sub protocol"}
native function getNegotiatedSubProtocol(Connection conn) (string);

@doc:Description {value:"Check whether the connection is secured or not"}
@doc:Return {value:"boolean: true if the connection is secured"}
native function isSecure(Connection conn) (boolean);

@doc:Description {value:"Check whether the connection is still open or not."}
native function isOpen(Connection conn) (boolean);

@doc:Description {value:"Get a map of all the upgrade headers of the connection"}
@doc:Return {value:"map<string>: Map of all the headers received in the connection upgrade"}
native function getUpgradeHeaders(Connection conn) (map<string>);

@doc:Description {value:"Get a value of a header"}
@doc:Param {value:"key: Key of the header which the value should be retrieved"}
@doc:Return {value:"string: Value of the key if exists else null"}
native function getUpgradeHeader(Connection conn, string key) (string);

@doc:Description {value:"Get parent connection if exisits"}
@doc:Param {value:"conn: connection which the parent connection should be retrieved"}
@doc:Return {value:"Connection: The parent connection if exisits else null"}
native function getParentConnection(Connection conn) (Connection);

@doc:Description {value:"Push text to the connection"}
@doc:Param {value:"text: Text which should be sent"}
native function pushText(Connection conn, string text);

@doc:Description {value:"Push binary data to the connection"}
@doc:Param {value:"data: Binary data which should be sent"}
native function pushBinary(Connection conn, blob data);

@doc:Description {value:"Close the connection"}
@doc:Param {value:"statusCode: Status code for closing the connection"}
@doc:Param {value:"reason: Reason for closing the connection"}
native function closeConnection(Connection conn, int statusCode, string reason);


@doc:Description {value:"Configuration struct for WebSocket client connection"}
struct ClientConnectorConfig {
    string [] subProtocols;
    string parentConnectionID;
    map<string> customHeaders;
    int idleTimeoutInSeconds = -1;
}

@doc:Description {value:"WebSocket client connector for connecting to WebSocket backend"}
@doc:Param {value:"url: WebSocket url for the backend"}
@doc:Param {value:"callbackService: Callback service to listen to the incoming messages from the backend"}
connector ClientConnector(string url, string callbackService) {

    @doc:Description {value:"Connect to remote endpoint"}
    @doc:Return {value:"Connection: New WebSocket connection for the connected backend"}
    native action connect(ClientConnectorConfig config) (Connection);

    @doc:Description {value:"Connect to remote endpoint with default configuration"}
    native action connectWithDefault() (Connection);
}