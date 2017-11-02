package ballerina.net.ws;

@Description {value:"Represent WebSocket text frame in Ballerina"}
public struct TextFrame {
    string text;
    boolean isFinalFragment;
}

@Description {value:"Represent WebSocket binary frame in Ballerina"}
public struct BinaryFrame {
    blob data;
    boolean isFinalFragment;
}

@Description {value:"Represent WebSocket close frame in Ballerina"}
public struct CloseFrame {
    int statusCode;
    string reason;
}

@Description {value:"Represent WebSocket ping frame in Ballerina"}
public struct PingFrame {
    blob data;
}

@Description {value:"Represent WebSocket pong frame in Ballerina"}
public struct PongFrame {
    blob data;
}

@Description {value:"Represent the details needed before the Handshake is done"}
public struct HandshakeConnection {
    string connectionID;
    boolean isSecure;
    map<string> upgradeHeaders;
}
@Description {value:"Cancel the handshake"}
@Param {value:"statusCode: Status code for closing the connection"}
@Param {value:"reason: Reason for closing the connection"}
public native function <HandshakeConnection conn> cancelHandshake(int statusCode, string reason);

@Description {value:"Represent WebSocket connection in ballerina. This include all connection oriented operations"}
public struct Connection {
    map attributes;
}
@Description {value:"Get the ID of the WebSocket connection"}
@Return {value:"string: ID of the connection"}
public native function <Connection conn> getID() (string);

@Description {value:"Get the Negotiated sub protocol for given connection"}
@Return {value:"string: Negotiated sub protocol"}
public native function <Connection conn> getNegotiatedSubProtocol() (string);

@Description {value:"Check whether the connection is secured or not"}
@Return {value:"boolean: true if the connection is secured"}
public native function <Connection conn> isSecure() (boolean);

@Description {value:"Check whether the connection is still open or not."}
public native function <Connection conn> isOpen() (boolean);

@Description {value:"Get a map of all the upgrade headers of the connection"}
@Return {value:"map<string>: Map of all the headers received in the connection upgrade"}
public native function <Connection conn> getUpgradeHeaders() (map);

@Description {value:"Get a value of a header"}
@Param {value:"key: Key of the header which the value should be retrieved"}
@Return {value:"string: Value of the key if exists else null"}
public native function <Connection conn> getUpgradeHeader(string key) (string);

@Description {value:"Get parent connection if exisits"}
@Param {value:"conn: connection which the parent connection should be retrieved"}
@Return {value:"Connection: The parent connection if exisits else null"}
public native function <Connection conn> getParentConnection() (Connection);

@Description {value:"Push text to the connection"}
@Param {value:"text: Text which should be sent"}
public native function <Connection conn> pushText(string text);

@Description {value:"Push binary data to the connection"}
@Param {value:"data: Binary data which should be sent"}
public native function <Connection conn> pushBinary(blob data);

@Description {value:"Ping the connection"}
@Param {value:"data: Binary data which should be sent"}
public native function <Connection conn> ping(blob data);

@Description {value:"Send pong message to the connection"}
@Param {value:"data: Binary data which should be sent"}
public native function <Connection conn> pong(blob data);

@Description {value:"Close the connection"}
@Param {value:"statusCode: Status code for closing the connection"}
@Param {value:"reason: Reason for closing the connection"}
public native function <Connection conn> closeConnection(int statusCode, string reason);


@Description {value:"Configuration struct for WebSocket client connection"}
public struct ClientConnectorConfig {
    string [] subProtocols;
    string parentConnectionID;
    map<string> customHeaders;
    int idleTimeoutInSeconds = -1;
}

@Description {value:"WebSocket client connector for connecting to WebSocket backend"}
@Param {value:"url: WebSocket url for the backend"}
@Param {value:"callbackService: Callback service to listen to the incoming messages from the backend"}
public connector ClientConnector(string url, string callbackService) {

    @Description {value:"Connect to remote endpoint"}
    @Return {value:"Connection: New WebSocket connection for the connected backend"}
    native action connect(ClientConnectorConfig config) (Connection);

    @Description {value:"Connect to remote endpoint with default configuration"}
    native action connectWithDefault() (Connection);
}