package ballerina.net.ws;

import ballerina.doc;

@doc:Description {value:"Represent WebSocket text frame in Ballerina"}
public struct TextFrame {
    string text;
    boolean isFinalFragment;
}

@doc:Description {value:"Represent WebSocket binary frame in Ballerina"}
public struct BinaryFrame {
    blob data;
    boolean isFinalFragment;
}

@doc:Description {value:"Represent WebSocket close frame in Ballerina"}
public struct CloseFrame {
    int statusCode;
    string reason;
}

@doc:Description {value:"Represent WebSocket ping frame in Ballerina"}
public struct PingFrame {
    blob data;
}

@doc:Description {value:"Represent WebSocket pong frame in Ballerina"}
public struct PongFrame {
    blob data;
}

@doc:Description {value:"Represent the details needed before the Handshake is done"}
public struct HandshakeConnection {
    string connectionID;
    boolean isSecure;
    map<string> upgradeHeaders;
}
@doc:Description {value:"Cancel the handshake"}
@doc:Param {value:"statusCode: Status code for closing the connection"}
@doc:Param {value:"reason: Reason for closing the connection"}
public native function <HandshakeConnection conn> cancelHandshake(int statusCode, string reason);

@doc:Description {value:"Represent WebSocket connection in ballerina. This include all connection oriented operations"}
public struct Connection {
    map attributes;
}
@doc:Description {value:"Get the ID of the WebSocket connection"}
@doc:Return {value:"string: ID of the connection"}
public native function <Connection conn> getID() (string);

@doc:Description {value:"Get the Negotiated sub protocol for given connection"}
@doc:Return {value:"string: Negotiated sub protocol"}
public native function <Connection conn> getNegotiatedSubProtocol() (string);

@doc:Description {value:"Check whether the connection is secured or not"}
@doc:Return {value:"boolean: true if the connection is secured"}
public native function <Connection conn> isSecure() (boolean);

@doc:Description {value:"Check whether the connection is still open or not."}
public native function <Connection conn> isOpen() (boolean);

@doc:Description {value:"Get a map of all the upgrade headers of the connection"}
@doc:Return {value:"map<string>: Map of all the headers received in the connection upgrade"}
public native function <Connection conn> getUpgradeHeaders() (map);

@doc:Description {value:"Get a value of a header"}
@doc:Param {value:"key: Key of the header which the value should be retrieved"}
@doc:Return {value:"string: Value of the key if exists else null"}
public native function <Connection conn> getUpgradeHeader(string key) (string);

@doc:Description {value:"Get parent connection if exisits"}
@doc:Param {value:"conn: connection which the parent connection should be retrieved"}
@doc:Return {value:"Connection: The parent connection if exisits else null"}
public native function <Connection conn> getParentConnection() (Connection);

@doc:Description {value:"Push text to the connection"}
@doc:Param {value:"text: Text which should be sent"}
public native function <Connection conn> pushText(string text);

@doc:Description {value:"Push binary data to the connection"}
@doc:Param {value:"data: Binary data which should be sent"}
public native function <Connection conn> pushBinary(blob data);

@doc:Description {value:"Ping the connection"}
@doc:Param {value:"data: Binary data which should be sent"}
public native function <Connection conn> ping(blob data);

@doc:Description {value:"Send pong message to the connection"}
@doc:Param {value:"data: Binary data which should be sent"}
public native function <Connection conn> pong(blob data);

@doc:Description {value:"Close the connection"}
@doc:Param {value:"statusCode: Status code for closing the connection"}
@doc:Param {value:"reason: Reason for closing the connection"}
public native function <Connection conn> closeConnection(int statusCode, string reason);


@doc:Description {value:"Configuration struct for WebSocket client connection"}
public struct ClientConnectorConfig {
    string [] subProtocols;
    string parentConnectionID;
    map<string> customHeaders;
    int idleTimeoutInSeconds = -1;
}

@doc:Description {value:"WebSocket client connector for connecting to WebSocket backend"}
@doc:Param {value:"url: WebSocket url for the backend"}
@doc:Param {value:"callbackService: Callback service to listen to the incoming messages from the backend"}
public connector ClientConnector(string url, string callbackService) {

    @doc:Description {value:"Connect to remote endpoint"}
    @doc:Return {value:"Connection: New WebSocket connection for the connected backend"}
    native action connect(ClientConnectorConfig config) (Connection);

    @doc:Description {value:"Connect to remote endpoint with default configuration"}
    native action connectWithDefault() (Connection);
}