package ballerina.net.http;

///////////////////////////
/// WebSocket Connector ///
///////////////////////////
@Description {value:"Represents a WebSocket connector in ballerina. This include all connector oriented operations."}
@Param {value: "attributes: Custom user attributes"}
public connector WebSocketConnector (map attributes){
    @Description {value:"Gets the ID of the WebSocket connection"}
    @Param {value:"conn: A Connection struct"}
    @Return {value:"ID of the connection"}
    native action getID() (string);

    @Description {value:"Gets the negotiated sub protocol of the connection"}
    @Param {value:"conn: A Connection struct"}
    @Return {value:"Negotiated sub protocol"}
    native action getNegotiatedSubProtocol() (string);

    @Description {value:"Checks whether the connection is secure or not"}
    @Param {value:"conn: A Connection struct"}
    @Return {value:"True if the connection is secure"}
    native action isSecure() (boolean);

    @Description {value:"Checks whether the connection is still open or not."}
    @Param {value:"conn: A Connection struct"}
    @Return {value:"True if the connection is open"}
    native action isOpen() (boolean);

    @Description {value:"Gets a map of all the upgrade headers of the connection"}
    @Param {value:"conn: A Connection struct"}
    @Return {value:"Map of all the headers received in the connection upgrade"}
    native action getUpgradeHeaders() (map);

    @Description {value:"Gets a value of a header"}
    @Param {value:"conn: A Connection struct"}
    @Param {value:"key: Key of the header for which the value should be retrieved"}
    @Return {value:"Value of the header if it exists, else it is null"}
    native action getUpgradeHeader(string key) (string);

    @Description {value:"Push text to the connection"}
    @Param {value:"conn: A Connection struct"}
    @Param {value:"text: Text to be sent"}
    native action pushText(string text);

    @Description {value:"Push binary data to the connection"}
    @Param {value:"conn: A Connection struct"}
    @Param {value:"data: Binary data to be sent"}
    native action pushBinary(blob data);

    @Description {value:"Ping the connection"}
    @Param {value:"conn: A Connection struct"}
    @Param {value:"data: Binary data to be sent"}
    native action ping(blob data);

    @Description {value:"Send pong message to the connection"}
    @Param {value:"conn: A Connection struct"}
    @Param {value:"data: Binary data to be sent"}
    native action pong(blob data);

    @Description {value:"Close the connection"}
    @Param {value:"conn: A Connection struct"}
    @Param {value:"statusCode: Status code for closing the connection"}
    @Param {value:"reason: Reason for closing the connection"}
    native action closeConnection(int statusCode, string reason);

    @Description {value:"Gets the query parameters from the Connection as a map"}
    @Param {value:"req: The Connection struct" }
    @Return {value:"The map of query params" }
    native action getQueryParams () (map);
}