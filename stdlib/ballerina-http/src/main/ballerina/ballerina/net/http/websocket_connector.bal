package ballerina.net.http;

///////////////////////////
/// WebSocket Connector ///
///////////////////////////

@Description {value:"Represents a WebSocket connector in ballerina. This include all connector oriented operations."}
public struct WebSocketConnector {
    map attributes;
}

//TODO:This throws errors. Fix it.
//public function <WebSocketConnector conn> WebSocketConnector(map attributes) {
//    conn.attributes = attributes;
//}

public function <WebSocketConnector conn> WebSocketConnector() {
}

//TODO: Following functions throws compilations errors. Fix it.
@Description {value:"Gets the ID of the WebSocket connection"}
@Return {value:"ID of the connection"}
public native function <WebSocketConnector wsConnector> getID() (string);

@Description {value:"Gets the negotiated sub protocol of the connection"}
@Return {value:"Negotiated sub protocol"}
public native function <WebSocketConnector wsConnector> getNegotiatedSubProtocol() (string);

@Description {value:"Checks whether the connection is secure or not"}
@Return {value:"True if the connection is secure"}
public native function <WebSocketConnector wsConnector> isSecure() (boolean);

@Description {value:"Checks whether the connection is still open or not."}
@Return {value:"True if the connection is open"}
public native function <WebSocketConnector wsConnector> isOpen() (boolean);

@Description {value:"Gets a map of all the upgrade headers of the connection"}
@Return {value:"Map of all the headers received in the connection upgrade"}
public native function <WebSocketConnector wsConnector> getUpgradeHeaders() (map);

@Description {value:"Gets a value of a header"}
@Param {value:"key: Key of the header for which the value should be retrieved"}
@Return {value:"Value of the header if it exists, else it is null"}
public native function <WebSocketConnector wsConnector> getUpgradeHeader(string key) (string);

@Description {value:"Push text to the connection"}
@Param {value:"text: Text to be sent"}
public native function <WebSocketConnector wsConnector> pushText(string text);

@Description {value:"Push binary data to the connection"}
@Param {value:"data: Binary data to be sent"}
public native function <WebSocketConnector wsConnector> pushBinary(blob data);

@Description {value:"Ping the connection"}
@Param {value:"data: Binary data to be sent"}
public native function <WebSocketConnector wsConnector> ping(blob data);

@Description {value:"Send pong message to the connection"}
@Param {value:"data: Binary data to be sent"}
public native function <WebSocketConnector wsConnector> pong(blob data);

@Description {value:"Close the connection"}
@Param {value:"statusCode: Status code for closing the connection"}
@Param {value:"reason: Reason for closing the connection"}
public native function <WebSocketConnector wsConnector> closeConnection(int statusCode, string reason);

@Description {value:"Gets the query parameters from the Connection as a map"}
@Return {value:"The map of query params" }
public native function <WebSocketConnector ep> getQueryParams () (map);
