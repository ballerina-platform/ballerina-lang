@test:Config {}
function ${testServiceFunctionName} () {
    http:WebSocketClient ${endpointName} = new(
        ${serviceUriStrName},
        config = {callbackService: ${callbackServiceName}, readyOnConnect: true}
    );
    // Send a message
    checkpanic ${endpointName}->pushText("hey");
    // Receive message via channel
    string wsReply = <- ${wsReplyChannel};
    // Test reply
    test:assertEquals(wsReply, "hey", msg = "Received message should be equal to the expected message");
}

service ${callbackServiceName} = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketClient ${callbackServiceName}Ep, string text) {
        // Send message via channel
        text -> ${wsReplyChannel};
    }
};
