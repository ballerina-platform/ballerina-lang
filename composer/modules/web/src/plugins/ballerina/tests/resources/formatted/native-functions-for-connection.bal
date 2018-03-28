import ballerina/net.ws;

function testGetID (ws:Connection conn) (string) {
    return ws:getID(conn);
}

function testGetNegotiatedSubProtocols (ws:Connection conn) (string) {
    return ws:getNegotiatedSubProtocol(conn);
}

function testIsSecure (ws:Connection conn) (boolean) {
    return ws:isSecure(conn);
}

function testIsOpen (ws:Connection conn) (boolean) {
    return ws:isOpen(conn);
}

function testGetUpgradeHeader (ws:Connection conn, string key) (string) {
    return ws:getUpgradeHeader(conn, key);
}

function testGetUpgradeHeaders (ws:Connection conn) (map) {
    return ws:getUpgradeHeaders(conn);
}

function testGetParentConnection (ws:Connection conn) (ws:Connection) {
    return ws:getParentConnection(conn);
}

function testPushText (ws:Connection conn, string text) {
    ws:pushText(conn, text);
}

function testPushBinary (ws:Connection conn, blob data) {
    ws:pushBinary(conn, data);
}

function testCloseConnection (ws:Connection conn, int statusCode, string reason) {
    ws:closeConnection(conn, statusCode, reason);
}