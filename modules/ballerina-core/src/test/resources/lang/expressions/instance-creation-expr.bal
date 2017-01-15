function testMessagePayload(string payload) (message) {
    message msg;
    msg = new message;
    ballerina.lang.message:setStringPayload(msg, payload);
    return msg;
}
