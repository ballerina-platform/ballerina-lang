import ballerina/net.ws;

function testGetID(ws:Connection conn) returns (string) {
    return conn.getID();
}

function testGetNegotiatedSubProtocols(ws:Connection conn) returns (string)  {
    return conn.getNegotiatedSubProtocol();
}

function testIsSecure(ws:Connection conn) returns (boolean) {
    return conn.isSecure();
}

function testIsOpen(ws:Connection conn) returns (boolean) {
    return conn.isOpen();
}

function testGetUpgradeHeader(ws:Connection conn, string key) returns (string) {
    return conn.getUpgradeHeader(key);
}

function testGetUpgradeHeaders (ws:Connection  conn) returns (map) {
    return conn.getUpgradeHeaders();
}

function testGetParentConnection(ws:Connection conn) returns (ws:Connection) {
    return conn.getParentConnection();
}

function testGetQueryParams(ws:Connection conn) returns (map) {
    return conn.getQueryParams();
}

function testPushText(ws:Connection conn, string text) {
    conn.pushText(text);
}

function testPushBinary(ws:Connection conn, blob data) {
    conn.pushBinary(data);
}

function testCloseConnection(ws:Connection conn, int statusCode, string reason) {
    conn.closeConnection(statusCode, reason);
}

function testPing(ws:Connection conn, blob b) {
    conn.ping(b);
}

function testPong(ws:Connection conn, blob b) {
    conn.pong(b);
}