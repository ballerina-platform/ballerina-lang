package ballerina.net.http;

///////////////////////////
/// WebSocket Connector ///
///////////////////////////

@Description {value:"Represents a WebSocket connector in ballerina. This include all connector oriented operations."}
@Field {value: "attributes: Custom user attributes"}
@Field {value: "id: The ID of the WebSocket connection"}
@Field {value: "negotiatedSubProtocol: The negotiated sub protocol of the connection"}
@Field {value: "isSecure: the connection is secure"}
@Field {value: "isOpen: whether the connection is open"}
@Field {value: "upgradeHeaders: a map of all the upgrade headers of the connection"}
public struct WebSocketConnector {
    //TODO:Make these read only
    //TODO:Move these to endpoint
    map attributes;
    string id;
    string negotiatedSubProtocol;
    boolean isSecure;
    boolean isOpen;
    map<string> upgradeHeaders;
}

//@Description {value:"Gets the ID of the WebSocket connection"}
//@Return {value:"ID of the connection"}
//public native function <WebSocketConnector wsConnector> getID() (string);

//@Description {value:"Gets the negotiated sub protocol of the connection"}
//@Return {value:"Negotiated sub protocol"}
//public native function <WebSocketConnector wsConnector> getNegotiatedSubProtocol() (string);
//
//@Description {value:"Checks whether the connection is secure or not"}
//@Return {value:"True if the connection is secure"}
//public native function <WebSocketConnector wsConnector> isSecure() (boolean);
//
//@Description {value:"Checks whether the connection is still open or not."}
//@Return {value:"True if the connection is open"}
//public native function <WebSocketConnector wsConnector> isOpen() (boolean);
//
//@Description {value:"Gets a map of all the upgrade headers of the connection"}
//@Return {value:"Map of all the headers received in the connection upgrade"}
//public native function <WebSocketConnector wsConnector> getUpgradeHeaders() (map);

//TODO: To work on later
//@Description {value:"Gets a value of a header"}
//@Param {value:"key: Key of the header for which the value should be retrieved"}
//@Return {value:"Value of the header if it exists, else it is null"}
//public native function <WebSocketConnector wsConnector> getUpgradeHeader(string key) (string);

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

@Description { value:"Sends a upgrade request with custom headers" }
@Param {value:"headers: a map of custom headers for handshake."}
public native function <WebSocketConnector conn> upgradeToWebSocket(map headers);

@Description { value:"Cancels the handshake" }
@Param {value:"statusCode: Status code for closing the connection"}
@Param {value:"reason: Reason for closing the connection"}
public native function <WebSocketConnector conn> cancelUpgradeToWebSocket(int status, string reason);

//TODO: Check on this if it should come in the request
//@Description {value:"Gets the query parameters from the Connection as a map"}
//@Return {value:"The map of query params" }
//public native function <WebSocketConnector ep> getQueryParams () returns (map);
