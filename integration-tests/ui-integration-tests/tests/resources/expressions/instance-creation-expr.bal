function testMessagePayload(string payload) (message) {
    message msg;
    msg = new message;
    ballerina.lang.messages:setStringPayload(msg, payload);
    return msg;
}
